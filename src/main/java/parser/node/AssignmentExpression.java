package parser.node;

public record AssignmentExpression(String operator, AstNode left, AstNode right) implements AstNode {
}
