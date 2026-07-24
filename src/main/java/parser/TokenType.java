package parser;

public enum TokenType {
    Number, EOF, String, Semicolon, OpenedCurlyBrace, ClosedCurlyBrace,
    AdditiveOperator, MultiplicativeOperator, OpenParenthesis, CloseParenthesis,
    Identifier, SimpleAssignment, ComplexAssignment, Let, Comma, If, Else,
    RelationalOperator, EqualityOperator, True, False,Null, LogicalAnd, LogicalOr,
    LogicalNot, WhileLoop
}
