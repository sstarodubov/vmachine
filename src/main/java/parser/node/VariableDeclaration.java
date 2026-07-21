package parser.node;

public record VariableDeclaration(Identifier id, AstNode init) implements AstNode {
}
