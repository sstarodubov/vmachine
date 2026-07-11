package machine.opcodes.operand;

public sealed interface Operand permits Number, Asterix, Register, MemoryAddr {

    default int value() {
        throw new UnsupportedOperationException();
    }

}
