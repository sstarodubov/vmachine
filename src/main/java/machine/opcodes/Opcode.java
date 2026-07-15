package machine.opcodes;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


public enum Opcode {

    NOP((byte) 0, "nop"),
    SYSCALL((byte) 1, "syscall"),
    MOVL((byte) 2, "movl"),
    ADDL((byte) 3, "addl"),
    SUBL((byte) 4, "subl"),
    INQL((byte) 5, "incl"),
    DECL((byte) 6, "decl"),
    MULL((byte) 7, "mull"),
    JMP((byte) 8, "jmp"),
    JC((byte) 9, "jc"),
    JZ((byte) 10, "jz"),
    JNZ((byte) 11, "jnz"),
    CMPL((byte) 12, "cmpl"),
    JE((byte) 13, "je"),
    JNE((byte) 14, "jne"),
    JG((byte) 15, "jg"),
    JGE((byte) 16, "jge"),
    JL((byte) 17, "jl"),
    JLE((byte) 18, "jle"),
    CMOVNCL((byte) 19, "cmovncl"),
    CMOVCL((byte) 20, "cmovcl"),
    CMOVNEL((byte) 21, "cmovnel"),
    CMOVEL((byte) 22, "cmovel"),
    LOOPL((byte) 23, "loopl"),
    ANDL((byte) 24, "andl"),
    ORL((byte) 25, "orl"),
    XORL((byte) 26, "xorl"),
    NOTL((byte) 27, "notl"),
    NEGL((byte) 28, "negl"),
    SHLL((byte) 29, "shll"),
    SHRL((byte) 30, "shrl"),
    SARL((byte) 31, "sarl"),
    LEAL((byte) 32, "leal"),
    PUSHL((byte) 33, "pushl"),
    POPL((byte) 34, "popl");

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
