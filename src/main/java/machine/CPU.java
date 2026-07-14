package machine;

import machine.opcodes.Opcode;
import machine.opcodes.operand.*;
import machine.opcodes.operand.Number;
import machine.opcodes.operand.transfers.DoubleTransfer;
import machine.opcodes.operand.transfers.SingleTransfer;
import machine.opcodes.operand.transfers.Transfer;

import java.nio.ByteBuffer;
import java.util.function.*;

import static machine.utils.Assertions.require;

public final class CPU {

    private final Memory memory;
    private final SysCallTable sysCallTable;
    public final RegStorage regStorage;

    public CPU() {
        this.memory = new Memory();
        this.sysCallTable = new SysCallTable();
        this.regStorage = new RegStorage();
    }

    public int statusCode = 0;

    private byte readTextByte() {
        final int ip = regStorage.readEip();
        regStorage.incEip();
        return memory.readTextByte(ip);
    }

    private short readTextShort() {
        final int ip = regStorage.readEip();
        regStorage.addEip(2);
        return memory.readTextShort(ip);
    }

    private int readTextInt() {
        final int val = memory.readTextInt(regStorage.readEip());
        regStorage.addEip(4);
        return val;
    }


    public int run(final ByteBuffer code) {
        memory.loadCode(code);
        regStorage.writeEip(code.getInt(0));

        /*
            Command Structure:
            [Opcode] [тип операнда, операнд]
         */
        Opcode curOpcode;
        byte curByte;
        while (regStorage.readEip() < memory.textSegmentSize()) {
            curByte = readTextByte();
            curOpcode = Opcode.fromByte(curByte);
            require(curOpcode != null, "unknown opcode: %d".formatted(curByte));
            switch (curOpcode) {
                case LEAL -> {
                    final Operand first = readOperand();
                    final int source = switch (first) {
                        case MemoryVar(int addr) -> addr;
                        default -> throw new UnsupportedOperationException();
                    };
                    final Operand second = readOperand();
                    final int dest = switch (second) {
                        case Register(int id) -> id;
                        default -> throw new UnsupportedOperationException();
                    };

                    regStorage.writeInt(dest, source);
                }
                case SARL -> doSh((data, sh) -> data >> sh, sh -> 0x1 << Math.max(sh - 1, 0));
                case SHRL -> doSh((data, sh) -> data >>> sh, sh -> 1 << Math.max(sh - 1, 0));
                case SHLL -> doSh((data, sh) -> data << sh, sh -> 0x80_00_00_00 >>> Math.max(sh - 1, 0));
                case NEGL -> doBitwise1(a -> a * -1);
                case NOTL -> doBitwise1(a -> ~a);
                case XORL -> doBitwise2((a, b) -> a ^ b);
                case ORL -> doBitwise2((a, b) -> a | b);
                case ANDL -> doBitwise2((a, b) -> a & b);
                case CMOVNCL -> doMoveIf(() -> !regStorage.readCF());
                case CMOVCL -> doMoveIf(regStorage::readCF);
                case MOVL -> doMoveIf(() -> true);
                case CMOVEL -> doMoveIf(regStorage::readZF);
                case CMOVNEL -> doMoveIf(() -> !regStorage.readZF());
                case SYSCALL -> {
                    final int syscallId = regStorage.readEax();
                    sysCallTable.executeOn(this, syscallId);
                }
                case NOP -> {
                    // skip
                }
                case ADDL -> {
                    final SingleTransfer t = prepareMathOpTransfer(Long::sum);
                    final int data = t.from().value();
                    final int register = t.to().value();
                    regStorage.writeInt(register, data);
                }
                case SUBL -> {
                    final SingleTransfer t = prepareMathOpTransfer((a, b) -> b - a);
                    final int data = t.from().value();
                    final int register = t.to().value();
                    regStorage.writeInt(register, data);
                }
                case INQL -> {
                    final SingleTransfer t = prepareIncrementTransfer(a -> a + 1);
                    final int data = t.from().value();
                    final int register = t.to().value();
                    regStorage.writeInt(register, data);
                }
                case DECL -> {
                    final SingleTransfer t = prepareIncrementTransfer(a -> a - 1);
                    final int data = t.from().value();
                    final int register = t.to().value();
                    regStorage.writeInt(register, data);
                }
                case MULL -> {
                    final Transfer t = prepareMullTransfer();
                    switch (t) {
                        case SingleTransfer(Operand num, Operand eax) -> {
                            final int data = num.value();
                            final int id = eax.value();
                            regStorage.writeInt(id, data);
                            regStorage.writeInt(RegStorage.edx, 0);
                        }
                        case DoubleTransfer(SingleTransfer edx, SingleTransfer eax) -> {
                            final int edxVal = edx.from().value();
                            final int edxId = edx.to().value();
                            regStorage.writeInt(edxId, edxVal);

                            final int eaxVal = eax.from().value();
                            final int eaxId = eax.to().value();
                            regStorage.writeInt(eaxId, eaxVal);
                        }
                    }
                }
                case LOOPL -> {
                    final Operand operand = readOperand();
                    regStorage.writeEcx(regStorage.readEcx() - 1);
                    if (regStorage.readEcx() != 0) {
                        doJump(operand);
                    }
                }
                case JMP -> {
                    final Operand operand = readOperand();
                    doJump(operand);
                }
                case JC -> {
                    final Operand operand = readOperand();
                    if (regStorage.readCF()) {
                        doJump(operand);
                    }
                }
                case JZ -> {
                    final Operand operand = readOperand();
                    if (regStorage.readZF()) {
                        doJump(operand);
                    }
                }
                case JNZ -> {
                    final Operand operand = readOperand();
                    if (!regStorage.readZF()) {
                        doJump(operand);
                    }
                }
                case CMPL -> {
                    final Operand op1 = readOperand();
                    final Operand op2 = readOperand();

                    final long val1 = switch (op1) {
                        case Number(int num) -> num;
                        case Register(int id) -> regStorage.readInt(id);
                        default -> throw new UnsupportedOperationException();
                    };
                    final long val2 = switch (op2) {
                        case Number(int num) -> num;
                        case Register(int id) -> regStorage.readInt(id);
                        default -> throw new UnsupportedOperationException();
                    };

                    final long diff = val2 - val1;
                    if (diff == 0) {
                        regStorage.setZF();
                    } else {
                        regStorage.clearOF();
                    }

                    if (diff < 0) {
                        regStorage.setSF();
                    } else {
                        regStorage.clearCF();
                    }

                    if (val2 < val1) {
                        regStorage.setCF();
                    } else {
                        regStorage.clearCF();
                    }

                    if (diff > Integer.MAX_VALUE || diff < Integer.MIN_VALUE) {
                        regStorage.setOF();
                    } else {
                        regStorage.clearOF();
                    }
                }
                case JE -> {
                    final var oper = readOperand();
                    if (regStorage.readZF()) {
                        doJump(oper);
                    }
                }
                case JNE -> {
                    final var oper = readOperand();
                    if (!regStorage.readZF()) {
                        doJump(oper);
                    }
                }
                case JG -> {
                    final var oper = readOperand();
                    if (regStorage.readCF() == regStorage.readOF() && !regStorage.readZF()) {
                        doJump(oper);
                    }
                }
                case JGE -> {
                    final var oper = readOperand();
                    if (regStorage.readCF() == regStorage.readOF()) {
                        doJump(oper);
                    }
                }
                case JL -> {
                    final var oper = readOperand();
                    if (regStorage.readCF() != regStorage.readOF()) {
                        doJump(oper);
                    }
                }
                case JLE -> {
                    final var oper = readOperand();
                    if (regStorage.readCF() != regStorage.readOF() && regStorage.readZF()) {
                        doJump(oper);
                    }
                }
            }
        }

        return statusCode;
    }

    private void doJump(final Operand operand) {
        switch (operand) {
            case MemoryAddr(int addr) -> regStorage.writeEip(addr);
            case Register(int addr) -> regStorage.writeEip(addr);
            case Asterix a -> {
                final int addr = resolve(a);
                regStorage.writeEip(addr);
            }
            default -> throw new UnsupportedOperationException();
        }
    }

    private int resolve(final Operand op) {
        return switch (op) {
            case Asterix(Operand p) -> resolve(p);
            case Register(int id) -> regStorage.readInt(id);
            case MemoryAddr(int addr) -> memory.readTextInt(addr);
            default -> throw new UnsupportedOperationException();
        };
    }

    private Transfer prepareMullTransfer() {
        //EDX:EAX = EAX × operand32
        final long firstVal = regStorage.readInt(RegStorage.eax);
        final Operand operand = readOperand();
        require(operand instanceof Register, "must be register");
        final long secondVal = resolve(operand);
        final long result = secondVal * firstVal;
        setFlags(result);

        if (regStorage.readOF()) {
            final var longBuff = ByteBuffer.allocate(8);
            longBuff.putLong(result);

            return new DoubleTransfer(
                    new SingleTransfer(new Number(longBuff.getInt(0)), new Register(RegStorage.edx)),
                    new SingleTransfer(new Number(longBuff.getInt(4)), new Register(RegStorage.eax))
            );
        }
        return new SingleTransfer(
                new Number((int) result), new Register(RegStorage.eax)
        );
    }

    private SingleTransfer prepareIncrementTransfer(final Function<Long, Long> fn) {
        final Operand operand = readOperand();
        final long result = fn.apply((long) resolve(operand));
        setFlags(result);
        require(operand instanceof Register, "must be register with value");
        return new SingleTransfer(new Number((int) result), operand);
    }

    Operand readOperand() {
        final OperandType type1 = OperandType.fromByte(readTextByte());
        return switch (type1) {
            case NUMBER -> new Number(readTextInt());
            case REGISTER -> new Register(readTextByte());
            case ASTERIX -> new Asterix(readOperand());
            case DIRECT_ADDR -> new MemoryAddr(readTextInt());
            case VARIABLE -> new MemoryVar(readTextInt());
            case INDIRECT_ADDR -> {
                final int value = readTextInt();
                final byte baseRegId = readTextByte();
                final byte idxRegId = readTextByte();
                final int multiplier = readTextInt();
                final int addr = value +
                        (baseRegId == -1 ? 0 :regStorage.readInt(baseRegId)) +
                        ((idxRegId == -1 ? 0 : regStorage.readInt(idxRegId)) * multiplier);
                yield new MemoryVar(addr);
            }
        };
    }

    void setFlags(final long result) {
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            regStorage.setCF();
            regStorage.setOF();
        } else {
            regStorage.clearCF();
            regStorage.clearOF();
        }

        if (result == 0) {
            regStorage.setZF();
        } else {
            regStorage.clearZF();
        }
    }

    SingleTransfer prepareMathOpTransfer(final BiFunction<Long, Long, Long> fn) {
        final Operand first = readOperand();
        final Operand second = readOperand();

        final long arg1 = switch (first) {
            case Number(int num) -> num;
            case Register(int id) -> regStorage.readInt(id);
            case MemoryVar(int addr) -> memory.readTextInt(addr);
            default -> throw new UnsupportedOperationException();
        };

        require(second instanceof Register, "addl. second operand must be register");
        final long arg2 = regStorage.readInt(second.value());
        final long result = fn.apply(arg1, arg2);
        setFlags(result);
        return new SingleTransfer(new Number((int) result), second);
    }

    SingleTransfer prepareMovTransfer() {
        final Operand first = readOperand();
        final Operand second = readOperand();

        require(second instanceof Register || second instanceof MemoryVar, "mov. 2's operand must be register");

        return new SingleTransfer(first, second);
    }

    void doMoveIf(final Supplier<Boolean> moveCondition) {
        final SingleTransfer t = prepareMovTransfer();
        final int data = switch (t.from()) {
            case Number(int num) -> num;
            case Register(int id) -> regStorage.readInt(id);
            case MemoryAddr(int addr) -> addr;
            case MemoryVar(int addr) -> memory.readTextInt(addr);
            default -> throw new IllegalStateException("Unexpected value: " + t.from());
        };
        if (moveCondition.get()) {
            switch (t.to()) {
                case Register(int id) -> regStorage.writeInt(id, data);
                case MemoryVar(int addr) -> memory.writeInt(addr, data);
                default -> throw new UnsupportedOperationException();
            }
        }
    }

    void doBitwise1(final Function<Integer, Integer> bitwiseOp) {
        final Operand first = readOperand();

        final int val1 = switch (first) {
            case Register(int id) -> regStorage.readInt(id);
            default -> throw new UnsupportedOperationException();
        };

        final int result = bitwiseOp.apply(val1);
        bitwiseFlags(result);

        regStorage.writeInt(first.value(), result);
    }

    void bitwiseFlags(int result) {
        regStorage.clearCF();
        regStorage.clearOF();
        if (result == 0) {
            regStorage.setZF();
        } else {
            regStorage.clearZF();
        }
        if (result < 0) {
            regStorage.setSF();
        } else {
            regStorage.clearSF();
        }
    }

    void doBitwise2(final BiFunction<Integer, Integer, Integer> bitwiseOp) {
        final Operand first = readOperand();
        final Operand sec = readOperand();

        final int val1 = switch (first) {
            case Number(int n) -> n;
            case Register(int id) -> regStorage.readInt(id);
            default -> throw new UnsupportedOperationException();
        };
        final int val2 = switch (sec) {
            case Register(int id) -> regStorage.readInt(id);
            default -> throw new UnsupportedOperationException();
        };
        final int result = bitwiseOp.apply(val1, val2);
        bitwiseFlags(result);
        regStorage.writeInt(sec.value(), result);
    }

    void doSh(BiFunction<Integer, Integer, Integer> shFn, Function<Integer, Integer> maskFn) {
        final var opr1 = readOperand();
        final var opr2 = readOperand();
        final int sh = switch (opr1) {
            case Number(int num) -> num;
            default -> throw new UnsupportedOperationException();
        };
        final int register = switch (opr2) {
            case Register(int id) -> id;
            default -> throw new UnsupportedOperationException();
        };
        final int data = regStorage.readInt(register);
        final int result = shFn.apply(data, sh);
        final int mask = maskFn.apply(sh);
        final int cf = data & mask;
        if (cf == 0) {
            regStorage.clearCF();
        } else {
            regStorage.setCF();
        }
        regStorage.writeInt(register, result);
    }
}
