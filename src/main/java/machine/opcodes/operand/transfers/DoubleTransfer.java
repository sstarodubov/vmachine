package machine.opcodes.operand.transfers;


public record DoubleTransfer(
        SingleTransfer first, SingleTransfer second
) implements Transfer {
}
