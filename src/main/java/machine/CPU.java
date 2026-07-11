package machine;

import machine.opcodes.Opcode;
import machine.opcodes.operand.*;
import machine.opcodes.operand.Number;
import machine.opcodes.operand.transfers.DoubleTransfer;
import machine.opcodes.operand.transfers.SingleTransfer;
import machine.opcodes.operand.transfers.Transfer;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;
import java.util.function.Function;

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
                case Opcode.MOVL -> {
                    final SingleTransfer t = prepareMovTransfer();
                    final int data = switch (t.from()) {
                        case Number(int num) -> num;
                        case Register(int id) -> regStorage.readInt(id);
                        case MemoryAddr(int addr) -> addr;
                        default -> throw new IllegalStateException("Unexpected value: " + t.from());
                    };
                    final int register = t.to().value();
                    regStorage.writeInt(register, data);
                }
                case Opcode.SYSCALL -> {
                    final int syscallId = regStorage.readEax();
                    sysCallTable.executeOn(this, syscallId);
                }
                case Opcode.NOP -> {
                    // skip
                }
                case Opcode.ADDL -> {
                    final SingleTransfer t = prepareMathOpTransfer(Long::sum);
                    final int data = t.from().value();
                    final int register = t.to().value();
                    regStorage.writeInt(register, data);
                }
                case Opcode.SUBL -> {
                    final SingleTransfer t = prepareMathOpTransfer((a, b) -> b - a);
                    final int data = t.from().value();
                    final int register = t.to().value();
                    regStorage.writeInt(register, data);
                }
                case Opcode.INQL -> {
                    final SingleTransfer t = prepareIncrementTransfer(a -> a + 1);
                    final int data = t.from().value();
                    final int register = t.to().value();
                    regStorage.writeInt(register, data);
                }
                case Opcode.DECL -> {
                    final SingleTransfer t = prepareIncrementTransfer( a -> a - 1);
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
                case JMP -> {
                     final Operand operand = readOperand();
                     handleJmp(operand);
                }
                case JC -> {
                     final Operand operand = readOperand();
                     if (regStorage.readCF()) {
                        handleJmp(operand);
                     }
                }
                case JZ -> {
                    final Operand operand = readOperand();
                    if (regStorage.readZF()) {
                         handleJmp(operand);
                    }
                }
                case JNZ -> {
                    final Operand operand = readOperand();
                    if (!regStorage.readZF()) {
                         handleJmp(operand);
                    }
                }
            }
        }

        return statusCode;
    }

    private void handleJmp(final Operand operand) {
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
            case MEMORY_ADDR -> new MemoryAddr(readTextInt());
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

        require(second instanceof Register, "mov. 2's operand must be register");

        return new SingleTransfer(first, second);
    }
}
