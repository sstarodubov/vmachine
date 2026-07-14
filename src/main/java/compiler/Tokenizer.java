package compiler;

import java.util.List;
import java.util.regex.Pattern;

public class Tokenizer {
    private final String input;
    int pos;

    private static final Pattern digitPattern = Pattern.compile("^[+-]?\\d+$");
    private final List<Character> hexChars = List.of(
            'a', 'b', 'c', 'd', 'e', 'f'
    );

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
            case '\'' -> {
                final var token = new Token(TokenType.SYMBOL, "" + input.charAt(++pos));
                pos+=2;
                yield token;
            }
            case '\"' -> {
                pos++;
                final var sb= new StringBuilder();
                while(pos < input.length() && input.charAt(pos) != '\"') {
                    sb.append(input.charAt(pos++));
                }
                pos++;
                yield new Token(TokenType.STR_LITERAL, sb.toString());
            }
            case '#' -> {
                pos++;
                yield new Token(TokenType.HASHTAG, "#");
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
            case '*' -> {
                pos++;
                yield new Token(TokenType.ASTERIX, "*");
            }
            case '(' -> {
                pos++;
                yield new Token(TokenType.OPEN_PARENTHESIS, "(");
            }
            case ')' -> {
                pos++;
                yield new Token(TokenType.CLOSE_PARENTHESIS, ")");
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
            } while (pos < input.length() &&
                    (Character.isDigit(input.charAt(pos)) || hexChars.contains(Character.toLowerCase(input.charAt(pos)))));
        }
        return new Token(TokenType.NUMBER, lexeme.toString());
    }

    private Token readString() {
        final var lexeme = new StringBuilder();
        while (pos < input.length() && !Character.isWhitespace(input.charAt(pos)) && input.charAt(pos) != ':'
                && input.charAt(pos) != ',' && input.charAt(pos) != ')' && input.charAt(pos) != '('
        ) {
            lexeme.append(input.charAt(pos++));
        }
        final var result = lexeme.toString();
        if ((result.startsWith("+") || result.startsWith("-")) && isSignInteger(result)) {
            return new Token(TokenType.NUMBER, result);
        }
        return new Token(TokenType.STRING, result);
    }

    private boolean isSignInteger(final String s) {
        if (s == null || s.trim().isEmpty()) return false;
        return digitPattern.asMatchPredicate().test(s);
    }
}
