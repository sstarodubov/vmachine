package machine.opcodes.operand;

public sealed interface Operand permits Variable, Number, Asterix, Register, MemoryAddr {

    default int value() {
        throw new UnsupportedOperationException();
    }

}
