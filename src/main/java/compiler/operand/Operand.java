package compiler.operand;

public sealed interface Operand permits IndirectAddr, VarOperand, Number,
                                        Register, Label, Asterix,Symbol {
}
