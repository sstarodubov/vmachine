package parser;

import parser.node.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
      | variableStatement
      | ifStatement
      ;
     */
    AstNode buildStatement() {
        return switch (lookahead.type()) {
            case TokenType.If -> buildIfStatement();
            case TokenType.Let -> buildVariableStatement();
            case TokenType.OpenedCurlyBrace -> buildBlockStatement();
            case TokenType.Semicolon -> buildEmptyStatement();
            default -> buildExpressionStatement();
        };
    }

    /*
        ifStatement
        : 'if' parenthesizedExpression blockStatement
        | 'if' parenthesizedExpression blockStatement else blockStatement
        ;
     */
    AstNode buildIfStatement() {
        eat(TokenType.If);
        final var condition = buildParenthesizedExpression();
        final var ifStatement = buildBlockStatement();
        final AstNode elseStatement = switch (lookahead.type()) {
            case TokenType.Else -> {
               eat(TokenType.Else);
               yield buildBlockStatement();
            }
            default -> null;
        };

        return new IfExpression(condition, ifStatement, elseStatement);
    }
    /*
        variableStatement
        : 'let' variableDeclarationList ';'
        ;
     */
    AstNode buildVariableStatement() {
        eat(TokenType.Let);
        final var declarationList = buildVariableDeclarationList();
        eat(TokenType.Semicolon);
        return new VariableStatement(declarationList);
    }

    /*
        variableDeclarationList
        : variableDeclaration
        | variableDeclarationList ',' variableDeclaration
        ;
     */
    List<VariableDeclaration> buildVariableDeclarationList() {
       final var list = new ArrayList<VariableDeclaration>();

       list.add(buildVariableDeclaration());
       while (lookahead.type() == TokenType.Comma) {
            eat(TokenType.Comma);
            list.add(buildVariableDeclaration());
       }

       return list;
    }

    /*
        variableDeclaration
        : identifier optVariableInitializer
        ;
     */

    VariableDeclaration buildVariableDeclaration() {
        final var id = buildIdentifier();
        final AstNode init = (lookahead.type() != TokenType.Comma && lookahead.type() != TokenType.Semicolon)
                ? buildVariableInitializer()
                : null;

        return new VariableDeclaration(id, init);
    }
    /*
        variableInitializer
        : '=' assignmentExpression
        ;
     */
    AstNode buildVariableInitializer() {
        eat(TokenType.SimpleAssignment);
        return buildAssignmentExpression();
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
        relationalExpression
        : AdditiveExpression
        | AdditiveExpression RELATIONAL_OPERATOR RelationalExpression
     */
    AstNode buildRelationalExpresssion() {
        return genericBinaryExpression(this::buildAdditiveExpression, TokenType.RelationalOperator);
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
        equalityExpression
        : relationalExpression EQUALITY_OPERATOR equalityOperator
        | relationalExpression
        ;
     */
    AstNode buildEqualityExpression() {
       return genericBinaryExpression(this::buildRelationalExpresssion, TokenType.EqualityOperator);
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

    AstNode buildLogicalORExpression() {
       return genericLogicalExpression(this::buildLogicalANDExpression, TokenType.LogicalOr);
    }

    AstNode buildLogicalANDExpression() {
        return genericLogicalExpression(this::buildEqualityExpression, TokenType.LogicalAnd);
    }

    private AstNode genericLogicalExpression (final Supplier<AstNode> mathOp, final TokenType type) {
        var left = mathOp.get();
        while (lookahead.type() == type) {
            final var operator = eat(type);
            final var right = mathOp.get();

            left = new LogicalExpression(operator.value(), left, right);
        }

        return left;
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
       | leftSideExpression
       ;
     */
    AstNode buildPrimaryExpression() {
        return switch (lookahead.type()) {
            case Identifier -> buildLeftSideExpression();
            case OpenParenthesis -> buildParenthesizedExpression();
            default -> buildLiteral();
        };
    }

    /*
        parenthesizedExpression
        : ( expression )
        ;
     */
    AstNode buildParenthesizedExpression() {
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
        return buildAssignmentExpression();
    }

    boolean isAssignmentOperator() {
        return lookahead.type() == TokenType.SimpleAssignment || lookahead.type() == TokenType.ComplexAssignment;
    }
    /*
      leftSideExpression
      : identifier
      ;
     */

    AstNode buildLeftSideExpression() {
        return buildIdentifier();
    }

    /*
        identifier
        : IDENTIFIER
     */
    Identifier buildIdentifier() {
        final var token = eat(TokenType.Identifier);
        return new Identifier(token.value());
    }
    /*
            assignmentExpression
            :  logicalOrExpression
            | leftSideExpression assignmentOperator assignmentExpression
     */
    AstNode buildAssignmentExpression() {
       final var left = buildLogicalORExpression();
       if (!isAssignmentOperator()) {
           return left;
       }

       final var operator = lookahead.type() == TokenType.SimpleAssignment
                             ? eat(TokenType.SimpleAssignment)
                             : eat(TokenType.ComplexAssignment);

       final var right = buildAssignmentExpression();

       if (!(left instanceof Identifier)) {
           throw new UnsupportedOperationException("lvalue error: %s".formatted(left));
       }

       return new AssignmentExpression(operator.value(), left, right);
    }

    /*
       literal
       : stringLiteral
       | numericalLiteral
       | booleanLiteral
       | nullLiteral
       ;
     */
    AstNode buildLiteral() {
        return switch (lookahead.type()) {
            case TokenType.False, TokenType.True -> buildBooleanLiteral();
            case TokenType.Number -> buildNumericalLiteral();
            case TokenType.String -> buildStringLiteral();
            case TokenType.Null -> buildNullLiteral();
            default -> throw new UnsupportedOperationException();
        };
    }
    /*
      nullLiteral
      : 'null'
      ;
     */
    NullLiteral buildNullLiteral() {
        eat(TokenType.Null);
        return new NullLiteral();
    }

     /*
     booleanLiteral
     : 'true'
     | 'false'
     ;
      */
    BooleanLiteral buildBooleanLiteral() {
       final var value = switch (lookahead.type()) {
           case False -> {
               eat(TokenType.False);
               yield false;
           }
           case True -> {
               eat(TokenType.True);
               yield true;
           }
           default -> throw new UnsupportedOperationException("unexpected token: %s".formatted(lookahead));
       };

       return new BooleanLiteral(value);
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

