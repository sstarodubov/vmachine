package machine;

public enum OperandType {

    NUMBER((byte) 0x1), REGISTER((byte) 0x2);//, MEMORY((byte) 0x3);

    public final byte code;

    OperandType(byte i) {
        this.code = i;
    }


    public static OperandType fromByte(final byte b) {
        return switch (b) {
            case 0x1 -> NUMBER;
            case 0x2 -> REGISTER;
            //case 0x3 -> MEMORY;
            default -> throw new RuntimeException("unknown operand type: %d".formatted(b));
        };
    }
}
