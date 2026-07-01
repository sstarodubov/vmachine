package compiler;

public record Token(
        TokenType type,
        String lexeme
) {
}
