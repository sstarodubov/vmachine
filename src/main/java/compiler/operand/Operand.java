package compiler.operand;

public sealed interface Operand permits IndirectAddr, Var, Number, Register, Label, Asterix {
}
