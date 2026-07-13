package machine.opcodes.operand;

public sealed interface Operand permits DeclaredVar, Number, Asterix, Register, MemoryAddr {

    default int value() {
        throw new UnsupportedOperationException();
    }

}
