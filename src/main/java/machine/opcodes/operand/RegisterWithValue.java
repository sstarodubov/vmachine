package machine.opcodes.operand;

public record RegisterWithValue(int id, int value) implements Operand {
}
