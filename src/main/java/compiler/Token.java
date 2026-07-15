package compiler;

public record Token(
        TokenType type,
        String lexeme
) {

    public boolean isSpecial() {
        return this.type() == TokenType.STRING && this.lexeme().startsWith(".");
    }
}
