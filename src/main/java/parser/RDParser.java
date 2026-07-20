package parser;

import parser.node.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;

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
        return new Program(buildStatementList(TokenType.EOF));
    }
    /*
      statement
      : expressionStatement
      | blockStatement
      | emptyStatement
      ;
     */
    AstNode buildStatement() {
        return switch (lookahead.type()) {
            case TokenType.OpenedCurlyBrace -> buildBlockStatement();
            case TokenType.Semicolon -> buildEmptyStatement();
            default -> buildExpressionStatement();
        };
    }

    /*
        emptyStatement
        : ';'
        ;
     */
    AstNode buildEmptyStatement() {
        eat(TokenType.Semicolon);
        return new EmptyStatement();
    }
    /*
        blockStatement
        : '{' statementList '}'
        ;
     */
    AstNode buildBlockStatement() {
        eat(TokenType.OpenedCurlyBrace);
        final AstNode expStatement = lookahead.type() != TokenType.ClosedCurlyBrace
                ? buildStatementList(TokenType.ClosedCurlyBrace)
                : new StatementList(Collections.emptyList());
        eat(TokenType.ClosedCurlyBrace);
        return new BlockStatement(expStatement);
    }

    /*
        statementList
        : statement
        |  statementList statement -> statement, statement, ...
        ;
     */
    AstNode buildStatementList(final TokenType stopLookahead) {
        final var statementList = new ArrayList<AstNode>();
        statementList.add(buildStatement());
        while (lookahead.type() != TokenType.EOF && lookahead.type() != stopLookahead) {
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
        additiveExpression
        : multiplicativeExpression
        | additiveExpression ADDITIVE_OPERATOR multiplicativeExpression
        ;
     */
    AstNode buildAdditiveExpression() {
        return genericBinaryExpression(this::buildMultiplicativeExpression, TokenType.AdditiveOperator);
    }

     /*
        multiplicativeExpression
        : primaryExpression
        | multiplicativeExpression MULTIPLICATIVE_OPERATOR primaryExpression
        ;
     */
    AstNode buildMultiplicativeExpression() {
        return genericBinaryExpression(this::buildPrimaryExpression, TokenType.MultiplicativeOperator);
    }
    private AstNode genericBinaryExpression(final Supplier<AstNode> mathOp, final TokenType type) {
        var left = mathOp.get();
        while (lookahead.type() == type) {
            final var operator = eat(type);
            final var right = mathOp.get();

            left = new BinaryExpression(operator.value(), left, right);
        }

        return left;
    }

    /*
       primaryExpression
       : literal
       | parenthesizedExpression
       ;
     */
    AstNode buildPrimaryExpression() {
        return switch (lookahead.type()) {
            case OpenParenthesis -> parenthesizedExpression();
            default -> buildLiteral();
        };
    }

    /*
        parenthesizedExpression
        : ( expression )
        ;
     */
    AstNode parenthesizedExpression() {
        eat(TokenType.OpenParenthesis);
        final var exp = buildExpression();
        eat(TokenType.CloseParenthesis);
        return exp;
    }

    /*
        expression
        : additiveExpression
        ;
     */
    AstNode buildExpression() {
        return buildAdditiveExpression();
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

