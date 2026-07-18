package parser.node;

public record ProgramNode(AstNode body) implements AstNode {

    @Override
    public String toString() {
        return """
                {
                    "type" : "Program",
                    "body" :  %s 
                } 
                """.formatted(body);
    }
}
