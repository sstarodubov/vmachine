package parser.node;

public record StringLiteralNode(String value) implements AstNode {

    @Override
    public String toString() {
        return """
                {
                    "type" : "StringLiteral",
                    "value" : "%s" 
                } 
                """.formatted(value);
    }
}
