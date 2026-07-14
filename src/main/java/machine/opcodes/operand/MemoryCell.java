package machine.opcodes.operand;

public record MemoryCell(int addr) implements Operand {

    @Override
    public int value() {
        return addr;
    }
}
