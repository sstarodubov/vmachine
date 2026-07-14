package machine;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum OperandType {

    NUMBER((byte) 101), REGISTER((byte) 102), ASTERIX((byte) 103), DIRECT_ADDR((byte) 104), VARIABLE((byte) 105),
    INDIRECT_ADDR((byte) 106);


    public final byte code;

    OperandType(byte i) {
        this.code = i;
    }

    private final static Map<Byte, OperandType> operands = Arrays.stream(OperandType.values())
            .collect(Collectors.toMap(op -> op.code, op -> op));

    public static OperandType fromByte(final byte b) {
        final var result = operands.get(b);
        if (result == null) {
            throw new RuntimeException("unknown operand type: %d".formatted(b));
        }
        return result;
    }
}
