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


    @Test
    void test8() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                    {
                        100;
                        "hello";
                    }
                """);
        var body = (StatementList) ast.body();
        var statementList = (StatementList) body;
        assertEquals(1, statementList.statements().size());
        var bs = (BlockStatement) statementList.statements().getLast();
        var list = (StatementList) bs.body();
        assertEquals(2, list.statements().size());
        var first = ((ExpressionStatement) list.statements().getFirst()).expression();
        var last = ((ExpressionStatement) list.statements().getLast()).expression();
        assertEquals(100, ((NumericLiteral) first).value());
        assertEquals("hello", ((StringLiteral) last).value());
    }

    @Test
    void test9() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                    { }
                """);
        var body = (StatementList) ast.body();
        var statementList = (StatementList) body;
        assertEquals(1, statementList.statements().size());
        var bs = (BlockStatement) statementList.statements().getLast();
        var list = (StatementList) bs.body();
        assertEquals(0, list.statements().size());
    }

    @Test
    void test10() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                    {
                        100;
                        {
                          "hello";
                        }
                    }
                """);
        var body = (StatementList) ast.body();
        var statementList = (StatementList) body;
        assertEquals(1, statementList.statements().size());
        var bs = (BlockStatement) statementList.statements().getLast();
        var list = (StatementList) bs.body();
        assertEquals(2, list.statements().size());
        var first = ((ExpressionStatement) list.statements().getFirst()).expression();
        var last = ((StringLiteral)((ExpressionStatement)((StatementList)((BlockStatement) list.statements().getLast()).body()).statements().getLast()).expression()).value();

        assertEquals(100, ((NumericLiteral) first).value());
        assertEquals("hello", last);
    }


    @Test
    void test11() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                    ;
                """);
        final var emptyStatement = ast.body()
                .as(StatementList.class).statements().getFirst()
                .as(EmptyStatement.class);
        assertNotNull(emptyStatement);
    }
}