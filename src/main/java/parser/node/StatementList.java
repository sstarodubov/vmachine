package parser.node;

import java.util.List;

public record StatementList(List<AstNode> statements) implements AstNode {
}
