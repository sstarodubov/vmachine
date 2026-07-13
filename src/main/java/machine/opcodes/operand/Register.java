package machine.opcodes.operand;

public record Register(int id) implements Operand {

    public int value() {
        return id;
    }
}
