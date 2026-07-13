package machine.opcodes.operand;

public record Variable(int addr) implements Operand {

    @Override
    public int value() {
        return addr;
    }
}
