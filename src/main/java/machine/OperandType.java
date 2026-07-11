package machine;

public enum OperandType {

    NUMBER((byte) 101), REGISTER((byte) 102), POINTER((byte) 103);


    public final byte code;

    OperandType(byte i) {
        this.code = i;
    }


    public static OperandType fromByte(final byte b) {
        return switch (b) {
            case 101 -> NUMBER;
            case 102 -> REGISTER;
            case 103 -> POINTER;
            //case 0x3 -> MEMORY;
            default -> throw new RuntimeException("unknown operand type: %d".formatted(b));
        };
    }
}
