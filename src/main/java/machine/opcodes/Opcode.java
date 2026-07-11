package machine.opcodes;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


public enum Opcode {

    NOP((byte) 0, "nop"),
    SYSCALL((byte) 0x1, "syscall"),
    MOVL((byte) 0x2, "movl"),
    ADDL((byte) 0x3, "addl"),
    SUBL((byte) 0x4, "subl"),
    INQL((byte) 0x5, "incl"),
    DECL((byte) 0x6, "decl"),
    MULL((byte) 0x7, "mull"),
    JMP((byte) 0x8, "jmp"),
    JC((byte) 0x9, "jc");



    public final byte code;
    public final String name;

    Opcode(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    private static final Opcode[] opcodes = Opcode.values();
    private static final Map<String, Opcode> opcodeNames = Arrays.stream(opcodes)
            .collect(Collectors.toMap(opcode -> opcode.name, opcode -> opcode));

    public static Opcode fromByte(final byte val) {
        if (val < 0 | val >= opcodes.length) {
            return null;
        }
        return opcodes[val];
    }

    public static Opcode fromString(final String val) {
        return opcodeNames.get(val);
    }
}
