package parser;

import org.junit.jupiter.api.Test;
import parser.node.NumericLiteralNode;
import parser.node.ProgramNode;
import parser.node.StringLiteralNode;

import static org.junit.jupiter.api.Assertions.*;

class RDParserTest {

    @Test
    void test1() {
        final var p = new RDParser();
        final ProgramNode ast = (ProgramNode) p.parse("1");
        final NumericLiteralNode node = (NumericLiteralNode) ast.body();
        assertEquals(1, node.value());
    }

    @Test
    void test2() {
        final var p = new RDParser();
        final ProgramNode ast = (ProgramNode) p.parse("""
                "hello"
                """);
        final var node = (StringLiteralNode) ast.body();
        assertEquals("hello", node.value());
    }

    @Test
    void test3() {
        final var p = new RDParser();
        final ProgramNode ast = (ProgramNode) p.parse("""
                'hello'
                """);
        final var node = (StringLiteralNode) ast.body();
        assertEquals("hello", node.value());
    }

    @Test
    void test4() {
        final var p = new RDParser();
        final ProgramNode ast = (ProgramNode) p.parse("   1    ");
        final NumericLiteralNode node = (NumericLiteralNode) ast.body();
        assertEquals(1, node.value());
    }


    @Test
    void test5() {
        final var p = new RDParser();
        final ProgramNode ast = (ProgramNode) p.parse("""
                    //number:
                    100
                """);
        final NumericLiteralNode node = (NumericLiteralNode) ast.body();
        assertEquals(100, node.value());
    }


    @Test
    void test6() {
        final var p = new RDParser();
        final ProgramNode ast = (ProgramNode) p.parse("""
                    /* 
                      number
                    */
                    100
                """);
        final NumericLiteralNode node = (NumericLiteralNode) ast.body();
        assertEquals(100, node.value());
    }
}