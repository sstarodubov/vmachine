package compiler;

public class Tokenizer {
    private final String input;
    int pos;

    public Tokenizer(String input) {
        this.input = input;
        this.pos = 0;
    }

    void skipWhiteSpace() {
        while (pos < input.length() && input.charAt(pos) == ' ') {
            pos++;
        }
    }

    public Token nextToken() {
        skipWhiteSpace();
        if (pos >= input.length()) {
            return new Token(TokenType.EOF, "");
        }

        final char cur = input.charAt(pos);

        return switch (cur) {
            case '#' -> {
                pos++;
                yield  new Token(TokenType.HASHTAG, "#");
            }
            case '.' -> {
                pos++;
                yield new Token(TokenType.DOT, ".");
            }
            case '$' -> {
                pos++;
                yield new Token(TokenType.DOLLAR, "$");
            }
            case '%' -> {
                pos++;
                yield new Token(TokenType.PERCENT, "%");
            }
            case ',' -> {
                pos++;
                yield new Token(TokenType.COMMA, ",");
            }
            case ':' -> {
                pos++;
                yield new Token(TokenType.COLON, ":");
            }
            case '\n' -> {
                pos++;
                yield new Token(TokenType.EOL, "\n");
            }
            case char num when Character.isDigit(num) -> readNum();
            default -> readString();
        };
    }

    private Token readNum() {
        final var lexeme = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            lexeme.append(input.charAt(pos++));
        }
        //parse hex
        if (input.charAt(pos) == 'x') {
            do {
                lexeme.append(input.charAt(pos++));
            } while (pos < input.length() && Character.isDigit(input.charAt(pos)));
        }
        return new Token(TokenType.NUMBER, lexeme.toString());
    }

    private Token readString() {
        final var lexeme = new StringBuilder();
        while (pos < input.length() && !Character.isWhitespace(input.charAt(pos)) && input.charAt(pos) != ':'
                && input.charAt(pos) != ','
        ) {
           lexeme.append(input.charAt(pos++));
        }

        return new Token(TokenType.STRING, lexeme.toString());
    }
}
