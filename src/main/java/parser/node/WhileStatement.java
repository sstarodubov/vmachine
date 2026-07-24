package parser.node;

public record WhileStatement(AstNode condition, AstNode body) implements AstNode {
}
