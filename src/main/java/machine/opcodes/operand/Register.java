package machine.opcodes.operand;

public record Register(int registerId, int value) implements Operand {
}
