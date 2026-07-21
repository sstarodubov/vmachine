package parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    record Rule(Pattern regexp, TokenType type) {}

    final Rule[] patternRules  = new Rule[] {
            new Rule(Pattern.compile("^\\blet\\b"), TokenType.Let),
            new Rule(Pattern.compile("^\\s+"),null), // white spaces
            new Rule(Pattern.compile("(^\"[^\"]*\")|(^'[^']*')"), TokenType.String), // strings
            new Rule(Pattern.compile("^\\d+"), TokenType.Number), // number
            new Rule(Pattern.compile("^/\\*[\\s\\S]*?\\*/"), null), //   /**/comments
            new Rule(Pattern.compile("^//.*"), null), // //comments
            new Rule(Pattern.compile("^;"), TokenType.Semicolon ), // semicolon
            new Rule(Pattern.compile("^\\{"), TokenType.OpenedCurlyBrace),
            new Rule(Pattern.compile("^}"), TokenType.ClosedCurlyBrace),
            new Rule(Pattern.compile("^[+\\-]"), TokenType.AdditiveOperator),
            new Rule(Pattern.compile("^[*/]"), TokenType.MultiplicativeOperator),
            new Rule(Pattern.compile("^\\("), TokenType.OpenParenthesis),
            new Rule(Pattern.compile("^\\)"), TokenType.CloseParenthesis),
            new Rule(Pattern.compile("^\\w+"), TokenType.Identifier),
            new Rule(Pattern.compile("^="), TokenType.SimpleAssignment),
            new Rule(Pattern.compile("^\\[\\*\\+-/]="), TokenType.ComplexAssignment),
            new Rule(Pattern.compile("^,"), TokenType.Comma)
    };

    int cursor = 0;
    final String _string;

    public Tokenizer(final String _string) {
        this._string = _string;
    }

    boolean hasMoreTokens() {
        return cursor < _string.length();
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
        if (!hasMoreTokens()) {
            return new Token(TokenType.EOF, "");
        }
        final var string = _string.substring(cursor);
        for (Rule rule : patternRules) {
            final Matcher matcher = rule.regexp().matcher(string);
            if (matcher.find()) {
                final var found = matcher.group();
                cursor += found.length();
                if (rule.type() == null) {
                    return getNextToken();
                }
                return new Token(rule.type(), found);
            }
        }

        throw new UnsupportedOperationException("unsupported token");
    }
}
