package machine.opcodes.operand;

public sealed interface Operand permits Register, Number, RegisterWithValue{

    default int value() {
        throw new UnsupportedOperationException();
    }

}
