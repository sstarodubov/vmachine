package machine;

import machine.opcodes.Opcode;
import machine.opcodes.Cell;
import machine.opcodes.RegisterCell;
import machine.utils.Pair;

import java.nio.ByteBuffer;

import static machine.OperandType.REGISTER;
import static machine.utils.Assertions.require;

public final class CPU {

    record Transfer(
            int data, // что положить
            Cell cell // в какую ячейку положить
    ) {
    }

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
                    final Transfer t = prepareMovTransfer(4);
                    switch (t.cell()) {
                        case RegisterCell(int registerId) -> regStorage.writeInt(registerId, t.data());
                        case null, default ->
                                throw new UnsupportedOperationException("unsupported cell destination: %s".formatted(t.cell()));
                    }
                }
                case MOVW -> {
                    final Transfer t = prepareMovTransfer(2);
                    switch (t.cell()) {
                        case RegisterCell(int registerId) -> regStorage.writeShort(registerId, (short) t.data());
                        case null, default ->
                                throw new UnsupportedOperationException("unsupported cell destination: %s".formatted(t.cell()));
                    }
                }
                case MOVB -> {
                    final Transfer t = prepareMovTransfer(1);
                    switch (t.cell()) {
                        case RegisterCell(int registerId) -> regStorage.writeByte(registerId, (byte) t.data());
                        case null, default ->
                                throw new UnsupportedOperationException("unsupported cell destination: %s".formatted(t.cell()));
                    }
                }
                case Opcode.SYSCALL -> {
                    final int syscallId = regStorage.readEax();
                    sysCallTable.executeOn(this, syscallId);
                }
                case Opcode.NOP -> {
                }
            }
        }


        return statusCode;
    }

    Transfer prepareMovTransfer(int size) {
        // first operand. (number | register)
        final OperandType type1 = OperandType.fromByte(readTextByte());
        final int data = switch (type1) {
            case NUMBER -> switch (size) {
                case 1 -> readTextByte();
                case 2 -> readTextShort();
                case 4 -> readTextInt();
                default -> throw new IllegalStateException("unexpected size: %d".formatted(size));
            };
            case REGISTER -> {
                final int registerId = readTextByte();
                yield switch (size) {
                    case 4 -> regStorage.readInt(registerId);
                    case 2 -> regStorage.readShort(registerId);
                    case 1 -> regStorage.readByte(registerId);
                    default -> throw new IllegalStateException("unexpected register size: %d".formatted(size));
                };
            }
        };

        // second operand: register only
        final OperandType type2 = OperandType.fromByte(readTextByte());
        final int dest = readTextByte();

        require(type2 == REGISTER, "mov. 2's operand must be register");

        return new Transfer(data, new RegisterCell(dest));
    }
}
