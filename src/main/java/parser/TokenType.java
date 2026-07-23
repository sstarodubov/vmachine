package parser;

public enum TokenType {
    Number, EOF, String, Semicolon, OpenedCurlyBrace, ClosedCurlyBrace,
    AdditiveOperator, MultiplicativeOperator, OpenParenthesis, CloseParenthesis,
    Identifier, SimpleAssignment, ComplexAssignment, Let, Comma, If, Else,
    RelationalOperator
}
