package parser;

import parser.node.*;

import java.util.ArrayList;
import java.util.Collections;

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
       return buildProgram();
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
        : statementList
        ;
     */
    AstNode buildProgram() {
        return new Program(buildStatementList());
    }
    /*
      statement
      : expressionStatement
      ;
     */
    AstNode buildStatement() {
        return buildExpressionStatement();
    }

    /*
        statementList
        : statement
        |  statementList statement -> statement, statement, ...
        ;
     */
    AstNode buildStatementList() {
        final var statementList = new ArrayList<AstNode>();
        statementList.add(buildStatement());
        while (lookahead.type() != TokenType.EOF) {
            statementList.add(buildStatement());
        }
        return new StatementList(Collections.unmodifiableList(statementList));
    }

    /*
        expressionStatement
        : expression ';'
        ;
     */
    AstNode buildExpressionStatement() {
        final var expression = buildExpression();
        eat(TokenType.Semicolon);
        return new ExpressionStatement(expression);
    }

    /*
        expression
        : literal
        ;
     */
    AstNode buildExpression() {
        return buildLiteral();
    }
    /*
       literal
       : stringLiteral
       | numericalLiteral
       ;
     */
    AstNode buildLiteral() {
        return switch (lookahead.type()) {
            case TokenType.Number -> buildNumericalLiteral();
            case TokenType.String -> buildStringLiteral();
            default -> throw new UnsupportedOperationException();
        };
    }

    /*
        stringLiteral
        : STRING
        ;
     */
    AstNode buildStringLiteral() {
        final var token = eat(TokenType.String);
        return new StringLiteral(token.value().substring(1, token.value().length() - 1));
    }

    /*
       numericalLiteral
       : NUMBER
       ;
     */
    AstNode buildNumericalLiteral() {
        final Token token = eat(TokenType.Number);
        return new NumericLiteral(Integer.parseInt(token.value()));
    }
}

