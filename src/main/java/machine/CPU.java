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
                    final SingleTransfer t = prepareMovTransfer(4);
                    final int register = ((Register) t.to()).id();
                    regStorage.writeInt(register, resolve(t.from(), 4));
                }
                case Opcode.MOVW -> {
                    final SingleTransfer t = prepareMovTransfer(2);
                    final int register = ((Register) t.to()).id();
                    regStorage.writeShort(register, (short) resolve(t.from(), 2));
                }
                case Opcode.MOVB -> {
                    final SingleTransfer t = prepareMovTransfer(1);
                    final int register = ((Register) t.to()).id();
                    regStorage.writeByte(register, (byte) resolve(t.from(), 1));
                }
                case Opcode.SYSCALL -> {
                    final int syscallId = regStorage.readEax();
                    sysCallTable.executeOn(this, syscallId);
                }
                case Opcode.NOP -> {
                    // skip
                }
                case Opcode.ADDL -> {
                    final SingleTransfer t = prepareMathOpTransfer(4, Integer::sum);
                    final int data = resolve(t.from(), 4);
                    final int register = ((Register) t.to()).id();
                    regStorage.writeInt(register, data);
                }
                case Opcode.SUBL -> {
                    final SingleTransfer t = prepareMathOpTransfer(4, (a, b) -> b - a);
                    final int data = resolve(t.from(), 4);
                    final int register = ((Register) t.to()).id();
                    regStorage.writeInt(register, data);
                }
                case Opcode.INQL -> {
                    final SingleTransfer t = prepareIncrementTransfer(4, a -> a + 1);
                    final int data = resolve(t.from(), 4);
                    final int register = ((Register) t.to()).id();
                    regStorage.writeInt(register, data);
                }
                case Opcode.DECL -> {
                    final SingleTransfer t = prepareIncrementTransfer(4, a -> a - 1);
                    final int data = resolve(t.from(), 4);
                    final int register = ((Register) t.to()).id();
                    regStorage.writeInt(register, data);
                }
                case MULL -> {
                    final Transfer t = prepareMullTransfer();
                    switch (t) {
                        case SingleTransfer(Operand num, Operand eax) -> {
                            final int data = resolve(num, 4);
                            final int id = ((Register) eax).id();
                            regStorage.writeInt(id, data);
                            regStorage.writeInt(RegStorage.edx, 0);
                        }
                        case DoubleTransfer(SingleTransfer edx, SingleTransfer eax) -> {
                            final int edxVal = resolve(edx.from(), 4);
                            final int edxId = ((Register) edx.to()).id();
                            regStorage.writeInt(edxId, edxVal);

                            final int eaxVal = resolve(eax.from(), 4);
                            final int eaxId = ((Register) eax.to()).id();
                            regStorage.writeInt(eaxId, eaxVal);
                        }
                    }
                }
                case JMP -> {
                     final Operand operand = readOperand(4);
                     switch (operand) {
                         case Number(int address) -> regStorage.writeEip(address);
                         default -> throw new UnsupportedOperationException("jmp. unknown operand: %s".formatted(operand));
                     }
                }
            }
        }

        return statusCode;
    }

    private int resolve(final Operand op, final int size) {
       return switch (op) {
           case Number(int num) -> num;
           case Pointer(Operand p) -> resolve(p, size);
           case Register(int id) -> switch (size) {
               case 4 -> regStorage.readInt(id);
               case 2 -> regStorage.readShort(id);
               case 1 -> regStorage.readByte(id);
               default -> throw new UnsupportedOperationException("unknown size: %d".formatted(size));
           };
       };
    }

    private Transfer prepareMullTransfer() {
        //EDX:EAX = EAX × operand32
        final long firstVal = regStorage.readInt(RegStorage.eax);
        final Operand operand = readOperand(4);
        require(operand instanceof Register, "must be register");
        final long secondVal = resolve(operand, 4);
        final long result = secondVal * firstVal;

        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            regStorage.setCF();
            regStorage.setOF();
        } else {
            regStorage.clearCF();
            regStorage.clearOF();
        }

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

    private SingleTransfer prepareIncrementTransfer(final int size, final Function<Integer, Integer> fn) {
        final Operand operand = readOperand(size);
        final int result = fn.apply(resolve(operand, size));
        require(operand instanceof Register, "must be register with value");
        return new SingleTransfer(new Number(result), operand);
    }

    Operand readOperand(final int size) {
        // first operand. (number | register)
        // get value
        final OperandType type1 = OperandType.fromByte(readTextByte());
        return switch (type1) {
            case NUMBER -> switch (size) {
                case 1 -> new Number(readTextByte());
                case 2 -> new Number(readTextShort());
                case 4 -> new Number(readTextInt());
                default -> throw new IllegalStateException("unexpected size: %d".formatted(size));
            };
            case REGISTER -> new Register(readTextByte());
            case POINTER -> new Pointer(readOperand(size));
        };
    }

    SingleTransfer prepareMathOpTransfer(final int size, final BiFunction<Integer, Integer, Integer> fn) {
        final Operand first = readOperand(size);
        final Operand second = readOperand(size);

        require(second instanceof Register, "addl. second operand must be register");

        final int result = fn.apply(resolve(first, 4), resolve(second, 4));
        return new SingleTransfer(new Number(result), second);
    }

    SingleTransfer prepareMovTransfer(final int size) {
        final Operand first = readOperand(size);
        final Operand second = readOperand(size);

        require(second instanceof Register, "mov. 2's operand must be register");

        return new SingleTransfer(first, second);
    }
}
