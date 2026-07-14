package machine.opcodes.operand;

public record MemoryVar(int addr) implements Operand {

    @Override
    public int value() {
        return addr;
    }
}
