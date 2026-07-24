package parser.node;

public record UnaryExpression(String operator, AstNode argument) implements AstNode {
}
