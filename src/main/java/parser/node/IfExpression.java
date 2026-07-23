package parser.node;


public record IfExpression(AstNode condition, AstNode ifStatement, AstNode elseStatement) implements AstNode {
}
