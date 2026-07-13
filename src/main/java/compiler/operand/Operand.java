package compiler.operand;

public sealed interface Operand permits Var, Number, Register, Label, Asterix {
}
