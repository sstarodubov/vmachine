package parser.node;

import java.util.List;

public record VariableStatement(List<VariableDeclaration> declarations) implements AstNode {
}
