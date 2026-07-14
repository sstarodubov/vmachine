package machine.opcodes.operand;

public sealed interface Operand permits MemoryCell, Number, Asterix, Register, MemoryAddr {

    default int value() {
        throw new UnsupportedOperationException();
    }

}
