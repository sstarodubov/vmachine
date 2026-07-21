package parser;

import org.junit.jupiter.api.Test;
import parser.node.*;

import static org.junit.jupiter.api.Assertions.*;

class RDParserTest {

    // skip whitespaces
    String sw(String s) {
        var sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                continue;
            }
            sb.append(s.charAt(i));
        }

        return sb.toString();
    }

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

    @Test
    void test12() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                    1+3;
                """);
        var binExp = ast.body()
                .as(StatementList.class).statements().getFirst()
                .as(ExpressionStatement.class).expression()
                .as(BinaryExpression.class);
        assertEquals(1, binExp.left().as(NumericLiteral.class).value());
        assertEquals(3, binExp.right().as(NumericLiteral.class).value());
        assertEquals("+", binExp.operator());
    }

    @Test
    void test13() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                    1-2+3;
                """);
        var binExp = ast.body()
                .as(StatementList.class).statements().getFirst()
                .as(ExpressionStatement.class).expression()
                .as(BinaryExpression.class);
        assertEquals(1, binExp.left().as(BinaryExpression.class).left().as(NumericLiteral.class).value());
        assertEquals("-", binExp.left().as(BinaryExpression.class).operator());
        assertEquals(2, binExp.left().as(BinaryExpression.class).right().as(NumericLiteral.class).value());
        assertEquals(3, binExp.right().as(NumericLiteral.class).value());
        assertEquals("+", binExp.operator());
    }


    @Test
    void test14() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                    1 + 2 * 3;
                """);
        var binExp = ast.body()
                .as(StatementList.class).statements().getFirst()
                .as(ExpressionStatement.class).expression()
                .as(BinaryExpression.class);

        assertEquals(1, binExp.left().as(NumericLiteral.class).value());
        assertEquals("+", binExp.operator());

        assertEquals(2, binExp.right().as(BinaryExpression.class).left().as(NumericLiteral.class).value());
        assertEquals("*", binExp.right().as(BinaryExpression.class).operator());
        assertEquals(3, binExp.right().as(BinaryExpression.class).right().as(NumericLiteral.class).value());
    }


    @Test
    void test15() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                    (1 + 2) * 3;
                """);
        var binExp = ast.body()
                .as(StatementList.class).statements().getFirst()
                .as(ExpressionStatement.class).expression()
                .as(BinaryExpression.class);
        assertEquals(sw("""
                    BinaryExpression[
                                    operator=*,
                                    left=BinaryExpression[
                                                          operator=+,
                                                          left=NumericLiteral[value=1],
                                                          right=NumericLiteral[value=2]
                                                          ],
                                    right=NumericLiteral[value=3]
                                    ]"""),
                sw(binExp.toString()));
    }


    @Test
    void test16() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                    (1);
                """);
        var value = ast.body()
                .as(StatementList.class).statements().getFirst()
                .as(ExpressionStatement.class).expression()
                .as(NumericLiteral.class).value();
        assertEquals(1, value);
    }

    @Test
    void test17() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                   x = 41;
                """);
        final var assignment =ast.body().as(StatementList.class).statements().getFirst()
                        .as(ExpressionStatement.class).expression()
                        .as(AssignmentExpression.class);
        assertEquals("=", assignment.operator());
        assertEquals("x", assignment.left().as(Identifier.class).value());
        assertEquals(41, assignment.right().as(NumericLiteral.class).value());
    }


    @Test
    void test18() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                   x = y = 41;
                """);
        final var assignment =ast.body().as(StatementList.class).statements().getFirst()
                .as(ExpressionStatement.class).expression()
                .as(AssignmentExpression.class);
        assertEquals(sw("""
                AssignmentExpression[operator==, 
                                     left=Identifier[value=x],
                                     right=AssignmentExpression[operator==,
                                                                left=Identifier[value=y],
                                                                right=NumericLiteral[value=41]
                                                                ]
                                     ]
                """), sw(assignment.toString()));
    }


    @Test
    void test19() {
        final var p = new RDParser();
        final Program ast = (Program) p.parse("""
                   x = y + 41;
                """);
        final var assignment =ast.body().as(StatementList.class).statements().getFirst()
                .as(ExpressionStatement.class).expression()
                .as(AssignmentExpression.class);
        System.out.println(assignment);
        assertEquals(sw("""
                AssignmentExpression[operator==,
                                     left=Identifier[value=x],
                                     right=BinaryExpression[
                                                        operator=+,
                                                        left=Identifier[value=y],
                                                        right=NumericLiteral[value=41]
                                                        ]
                                     ]
                """), sw(assignment.toString()));
    }
}