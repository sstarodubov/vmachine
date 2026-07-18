package parser;

public class Tokenizer {


    int cursor = 0;
    final String _string;

    public Tokenizer(final String _string) {
        this._string = _string;
    }

    boolean hasMoreTokens() {
        return cursor < _string.length();
    }

    void skipWhiteSpaces() {
        while (hasMoreTokens() && Character.isWhitespace(_string.charAt(cursor))) {
            cursor++;
        }
    }

    Token extractString(final char qType) {
        cursor++;
        final var sb = new StringBuilder();
        while (hasMoreTokens() && _string.charAt(cursor) != qType) {
            sb.append(_string.charAt(cursor));
            cursor++;
        }
        cursor++;

        return new Token(TokenType.String, sb.toString());
    }

    public Token getNextToken() {
        skipWhiteSpaces();
        if (!hasMoreTokens()) {
            return new Token(TokenType.EOF, "");
        }

        return switch (_string.charAt(cursor)) {
            case char q when q == '"' || q == '\'' -> extractString(q);
            case char digit when Character.isDigit(digit) -> {
                final var sb = new StringBuilder();
                while (hasMoreTokens() && Character.isDigit(_string.charAt(cursor))) {
                    sb.append(_string.charAt(cursor));
                    cursor++;
                }
                yield new Token(TokenType.Number, sb.toString());
            }

            default -> throw new UnsupportedOperationException();
        };
    }
}
