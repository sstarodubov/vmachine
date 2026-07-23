package parser.node;

public record LogicalExpression(String value, AstNode left, AstNode right) implements AstNode {
}
