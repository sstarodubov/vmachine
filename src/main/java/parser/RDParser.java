package parser;

import parser.node.AstNode;
import parser.node.NumericLiteralNode;
import parser.node.ProgramNode;
import parser.node.StringLiteralNode;

/*
    курс по парсингу
 */

public final class RDParser {
    String _string;
    Tokenizer tokenizer;
    Token lookahead;

    public AstNode parse(final String s) {
       this._string = s;
       this.tokenizer = new Tokenizer(s);

       lookahead = tokenizer.getNextToken();
       return program();
    }

    Token eat(TokenType expectedType) {
        final var cur = lookahead;
        if (cur.type() != expectedType) {
            throw new IllegalStateException("unexpected token: %s. expect: %s".formatted(cur, expectedType));
        }
        lookahead = tokenizer.getNextToken();

        return cur;
    }

    /*
        program
        : literal
        ;
     */
    AstNode program() {
        return new ProgramNode(literal());
    }
    /*
       literal
       : stringLiteral
       | numericalLiteral
       ;
     */

    AstNode literal() {
        return switch (lookahead.type()) {
            case TokenType.Number -> numericalLiteral();
            case TokenType.String -> stringLiteral();
            default -> throw new UnsupportedOperationException();
        };
    }

    /*
        stringLiteral
        : STRING
        ;
     */
    AstNode stringLiteral() {
        final var token = eat(TokenType.String);
        return new StringLiteralNode(token.value());
    }

    /*
       numericalLiteral
       : NUMBER
       ;
     */
    AstNode numericalLiteral() {
        final Token token = eat(TokenType.Number);
        return new NumericLiteralNode(Integer.parseInt(token.value()));
    }
}

