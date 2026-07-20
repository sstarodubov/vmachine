package parser.node;

public record BinaryExpression(String operator, AstNode left, AstNode right) implements AstNode {
}
