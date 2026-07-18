package parser.node;

public record NumericLiteralNode(int value) implements AstNode {

    @Override
    public String toString() {
        return """
                {
                    "type": "NumericalLiterNode",
                    "value" : %d 
                } 
                """.formatted(value);
    }
}
