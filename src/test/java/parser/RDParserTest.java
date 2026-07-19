package parser;

import org.junit.jupiter.api.Test;
import parser.node.*;

import static org.junit.jupiter.api.Assertions.*;

class RDParserTest {

    @Test
    void test1() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("1;");
        var body = (StatementList) ast.body();
        assertEquals(1, body.statements().size());
        var firstStatement = (NumericLiteral)((ExpressionStatement) body.statements().getFirst()).expression();
        assertEquals(1, firstStatement.value());
    }

    @Test
    void test2() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                "hello";
                """);
        var body = (StatementList) ast.body();
        assertEquals(1, body.statements().size());
        var firstStatement = (StringLiteral)((ExpressionStatement) body.statements().getFirst()).expression();
        assertEquals("hello", firstStatement.value());
    }

    @Test
    void test3() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                'hello';
                """);
        var body = (StatementList) ast.body();
        assertEquals(1, body.statements().size());
        var firstStatement = (StringLiteral)((ExpressionStatement) body.statements().getFirst()).expression();
        assertEquals("hello", firstStatement.value());
    }

    @Test
    void test4() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("   1    ;");

        var body = (StatementList) ast.body();
        assertEquals(1, body.statements().size());
        var firstStatement = (NumericLiteral)((ExpressionStatement) body.statements().getFirst()).expression();
        assertEquals(1, firstStatement.value());
    }


    @Test
    void test5() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                    //number:
                    100;
                """);


        var body = (StatementList) ast.body();
        assertEquals(1, body.statements().size());
        var firstStatement = (NumericLiteral)((ExpressionStatement) body.statements().getFirst()).expression();
        assertEquals(100, firstStatement.value());
    }


    @Test
    void test6() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                    /* 
                      number
                    */
                    100;
                """);

        var body = (StatementList) ast.body();
        assertEquals(1, body.statements().size());
        var firstStatement = (NumericLiteral)((ExpressionStatement) body.statements().getFirst()).expression();
        assertEquals(100, firstStatement.value());
    }

    @Test
    void test7() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                    100;
                    "hello";
                """);
        var body = (StatementList) ast.body();
        assertEquals(2, body.statements().size());
        var firstStatement = (NumericLiteral)((ExpressionStatement) body.statements().getFirst()).expression();
        assertEquals(100, firstStatement.value());
        var secondStatement = (StringLiteral)((ExpressionStatement) body.statements().getLast()).expression();
        assertEquals("hello", secondStatement.value());
    }
}