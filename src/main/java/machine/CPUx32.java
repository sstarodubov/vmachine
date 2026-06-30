package machine;

import lombok.Builder;
import lombok.Setter;

import static machine.utils.Assertions.require;

@Builder
public class CPUx32 {

    private final Memory memory;
    private final SysCallTable sysCallTable;
    public final Registers registers;

    @Setter
    int statusCode = 0;


    public int run() {
        return run(0);
    }

    private byte readTextByte() {
        final int ip = registers.readEip();
        registers.incEip();
        return memory.readTextByte(ip);
    }

    private int readTextInt() {
        final int val = memory.readTextInt(registers.readEip());
        registers.addEip(4);
        return val;
    }


    public int run(final int init) {
        registers.writeEip(init);

        /*
            Command Structure:
            [Opcode] [количество операндов] [...типы операндов] [...операнды]
         */
        byte curOpcode;
        while (registers.readEip() < memory.textSegmentSize()) {
            curOpcode = readTextByte();

            switch (curOpcode) {
                case Opcodes.movl -> {
                    final OperandType type1 = OperandType.fromByte(readTextByte());
                    final int val1 = readTextInt();
                    final OperandType type2 = OperandType.fromByte(readTextByte());
                    final int idx = readTextInt();

                    final int source = switch (type1) {
                        case NUMBER -> val1;
                        case REGISTER -> registers.readInt(val1);
                    };

                    require(type2 == OperandType.REGISTER, "movl. 2's operand must be register");
                    registers.writeInt(idx, source);
                }
                case Opcodes.syscall -> {
                    final int syscallId = registers.readEax();
                    sysCallTable.executeOn(this, syscallId);
                }

                default -> throw new IllegalStateException("unknown opcode: %d".formatted(curOpcode));
            }
        }


        return statusCode;
    }
}
