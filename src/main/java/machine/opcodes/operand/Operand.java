package machine.opcodes.operand;

public sealed interface Operand permits Register, Number {

    int value();

}
