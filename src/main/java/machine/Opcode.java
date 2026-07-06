package machine;

import machine.utils.Assertions;

import java.util.Arrays;
import java.util.List;

import static machine.utils.Assertions.require;

public enum Opcode {

    NOP((byte) 0, "nop"),
    SYSCALL((byte) 0x1, "syscall"),
    MOVL((byte) 0x2, "movl"),
    MOVW((byte) 0x3, "movw"),
    MOVB((byte) 0x4, "movb");

    public final byte code;
    public final String name;

    Opcode(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    private static List<String> opcodeNames = Arrays.stream(Opcode.values())
            .map(op -> op.name).toList();

    public static Opcode fromByte(final byte val) {
        return switch (val) {
            case 0x1 -> Opcode.SYSCALL;
            case 0x2 -> Opcode.MOVL;
            case 0 -> Opcode.NOP;
            case 0x3 -> Opcode.MOVW;
            case 0x4 -> Opcode.MOVB;
            default -> null;
        };
    }


    public static Opcode fromString(final String val) {
        return switch (val) {
            case "syscall" -> Opcode.SYSCALL;
            case "movl" -> Opcode.MOVL;
            case "movw" -> Opcode.MOVW;
            case "movb" -> Opcode.MOVB;
            case "nop" -> Opcode.NOP;
            default -> null;
        };
    }

    public static boolean isOpcode(final String name) {
        require(name != null, "name must be not null");
        return opcodeNames.contains(name);
    }

}
