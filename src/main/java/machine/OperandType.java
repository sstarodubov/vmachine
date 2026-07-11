package machine;

public enum OperandType {

    NUMBER((byte) 101), REGISTER((byte) 102), ASTERIX((byte) 103), MEMORY_ADDR((byte) 104);


    public final byte code;

    OperandType(byte i) {
        this.code = i;
    }


    public static OperandType fromByte(final byte b) {
        return switch (b) {
            case 101 -> NUMBER;
            case 102 -> REGISTER;
            case 103 -> ASTERIX;
            case 104 -> MEMORY_ADDR;
            default -> throw new RuntimeException("unknown operand type: %d".formatted(b));
        };
    }
}
