package machine;

import machine.opcodes.Opcode;
import machine.opcodes.operand.Number;
import machine.opcodes.operand.Operand;
import machine.opcodes.operand.Register;
import machine.opcodes.operand.RegisterWithValue;

import java.nio.ByteBuffer;
import java.util.function.BiFunction;

import static machine.utils.Assertions.require;

public final class CPU {
    enum AccessMode {
       ONLY_ADDRESS, READ_VALUE
    }

    record Transfer(
            Operand from, // откуда положить
            Operand to // куда положить
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
                    final int register = ((Register) t.to()).id();
                    regStorage.writeInt(register, t.from().value());
                }
                case MOVW -> {
                    final Transfer t = prepareMovTransfer(2);
                    final int register = ((Register) t.to()).id();
                    regStorage.writeShort(register, (short) t.from().value());
                }
                case MOVB -> {
                    final Transfer t = prepareMovTransfer(1);
                    final int register = ((Register) t.to()).id();
                    regStorage.writeByte(register, (byte) t.from().value());
                }
                case Opcode.SYSCALL -> {
                    final int syscallId = regStorage.readEax();
                    sysCallTable.executeOn(this, syscallId);
                }
                case Opcode.NOP -> {
                    // skip
                }
                case Opcode.ADDL ->  {
                    final Transfer t = prepareMathOpTransfer(4, Integer::sum);
                    final int data = t.from().value();
                    final int register = ((RegisterWithValue) t.to()).id();
                    regStorage.writeInt(register, data);
                }
            }
        }

        return statusCode;
    }

    Operand readOperand(final int size, final AccessMode mode) {
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
            case REGISTER -> {
                final int registerId = readTextByte();
                yield switch (size) {
                    case 4 -> switch (mode) {
                        case ONLY_ADDRESS -> new Register(registerId);
                        case READ_VALUE -> new RegisterWithValue(registerId, regStorage.readInt(registerId));
                    };
                    case 2 -> switch (mode) {
                        case READ_VALUE -> new RegisterWithValue(registerId, regStorage.readShort(registerId));
                        case ONLY_ADDRESS -> new Register(registerId);
                    };
                    case 1 -> switch (mode) {
                        case READ_VALUE -> new RegisterWithValue(registerId, regStorage.readByte(registerId));
                        case ONLY_ADDRESS -> new Register(registerId);
                    };
                    default -> throw new IllegalStateException("unexpected register size: %d".formatted(size));
                };
            }
        };
    }

    Transfer prepareMathOpTransfer(final int size, final BiFunction<Integer, Integer, Integer> fn) {
        final Operand first = readOperand(size, AccessMode.READ_VALUE);
        final Operand second = readOperand(size, AccessMode.READ_VALUE);

        require(second instanceof RegisterWithValue, "addl. second operand must be register");

        final int sum = fn.apply(first.value(), second.value());
        return new Transfer(new Number(sum), second);
    }

    Transfer prepareMovTransfer(final int size) {
        final Operand first = readOperand(size, AccessMode.READ_VALUE);
        final Operand second = readOperand(size, AccessMode.ONLY_ADDRESS);

        require(second instanceof Register, "mov. 2's operand must be register");

        return new Transfer(first, second);
    }
}
