package machine;

import machine.utils.Pair;

import java.nio.ByteBuffer;

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
            [Opcode] [количество операндов] [...типы операндов] [...операнды]
         */
        Opcode curOpcode;
        byte curByte;
        while (regStorage.readEip() < memory.textSegmentSize()) {
            curByte = readTextByte();
            curOpcode = Opcode.fromByte(curByte);
            require(curOpcode != null, "unknown opcode: %d".formatted(curByte));
            switch (curOpcode) {
                case Opcode.MOVL -> {
                    final Pair<Integer, Integer>  pair = doMov(4);
                    final int idx = pair.left();
                    final int source = pair.right();
                    regStorage.writeInt(idx, source);
                }
                case MOVW -> {
                    final Pair<Integer, Integer>  pair = doMov(2);
                    final int idx = pair.left(), source = pair.right();
                    regStorage.writeShort(idx, (short) source);
                }
                case MOVB -> {
                    final Pair<Integer, Integer>  pair = doMov(1);
                    final int idx = pair.left(), source = pair.right();
                    regStorage.writeByte(idx, (byte) source);
                }
                case Opcode.SYSCALL -> {
                    final int syscallId = regStorage.readEax();
                    sysCallTable.executeOn(this, syscallId);
                }
                case Opcode.NOP -> {}
            }
        }


        return statusCode;
    }

    Pair<Integer, Integer> doMov(final int size) {
        final OperandType type1 = OperandType.fromByte(readTextByte());
        final int val1 = switch (size) {
            case 1 -> readTextByte();
            case 2 -> readTextShort();
            case 4 -> readTextInt();
            default -> throw new IllegalStateException("unexpected size: %d".formatted(size));
        };
        final OperandType type2 = OperandType.fromByte(readTextByte());
        final int regIdx = readTextByte();

        final int source = switch (type1) {
            case NUMBER -> val1;
            case REGISTER -> regStorage.readInt(val1);
        };

        require(type2 == OperandType.REGISTER, "mov. 2's operand must be register");

        return new Pair<>(regIdx, source);
    }
}
