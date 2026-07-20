package parser.node;

public interface AstNode {

    default <T> T as(Class<T> type) {
        return type.cast(this);
    }
}
