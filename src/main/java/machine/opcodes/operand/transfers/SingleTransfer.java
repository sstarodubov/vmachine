package machine.opcodes.operand.transfers;

import machine.opcodes.operand.Operand;

public record SingleTransfer(
        Operand from, // откуда положить
        Operand to // куда положить
) implements Transfer {
}
