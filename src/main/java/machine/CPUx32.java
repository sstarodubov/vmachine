package machine;

import lombok.Builder;
import machine.utils.Assertions;

import static machine.utils.Assertions.require;

@Builder
public class CPUx32 {

    private final Memory memory;
    private final SysCallTable sysCallTable;
    public final Registers registers;

    public int statusCode = 0;


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
        Opcode curOpcode;
        byte curByte;
        while (registers.readEip() < memory.textSegmentSize()) {
            curByte = readTextByte();
            curOpcode = Opcode.fromByte(curByte);
            require(curOpcode != null, "unknown opcode: %d".formatted(curByte));
            switch (curOpcode) {
                case Opcode.MOVL -> {
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
                case Opcode.SYSCALL -> {
                    final int syscallId = registers.readEax();
                    sysCallTable.executeOn(this, syscallId);
                }
                case Opcode.NOP -> {}
            }
        }


        return statusCode;
    }
}
