package compiler;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;


class TokenizerTest {

    Tokenizer tokenizer;


    @BeforeEach
    void be() {
        this.tokenizer = new Tokenizer("""
                # first program
                .globl _start
                
                .section .text
                _start:
                    movq $60, %rax
                    movq $22, %rdi
                    syscall
                """);
    }


    @Test
    void test() {
        // Act
        List<Token> tokens = new ArrayList<>();
        Token token;
        while ((token = tokenizer.nextToken()).type() != TokenType.EOF) {
            tokens.add(token);
        }

        // Assert - проверяем количество
        Assertions.assertEquals(30, tokens.size(), "Expected 30 tokens");

        // Assert - проверяем каждый токен
        assertToken(tokens, 0, TokenType.HASHTAG, "#");
        assertToken(tokens, 1, TokenType.STRING, "first");
        assertToken(tokens, 2, TokenType.STRING, "program");
        assertToken(tokens, 3, TokenType.EOL, "\n");
        assertToken(tokens, 4, TokenType.STRING, ".globl");
        assertToken(tokens, 5, TokenType.STRING, "_start");
        assertToken(tokens, 6, TokenType.EOL, "\n");
        assertToken(tokens, 7, TokenType.EOL, "\n");
        assertToken(tokens, 8, TokenType.STRING, ".section");
        assertToken(tokens, 9, TokenType.STRING, ".text");
        assertToken(tokens, 10, TokenType.EOL, "\n");
        assertToken(tokens, 11, TokenType.STRING, "_start");
        assertToken(tokens, 12, TokenType.COLON, ":");
        assertToken(tokens, 13, TokenType.EOL, "\n");
        assertToken(tokens, 14, TokenType.STRING, "movq");
        assertToken(tokens, 15, TokenType.DOLLAR, "$");
        assertToken(tokens, 16, TokenType.NUMBER, "60");
        assertToken(tokens, 17, TokenType.COMMA, ",");
        assertToken(tokens, 18, TokenType.PERCENT, "%");
        assertToken(tokens, 19, TokenType.STRING, "rax");
        assertToken(tokens, 20, TokenType.EOL, "\n");
        assertToken(tokens, 21, TokenType.STRING, "movq");
        assertToken(tokens, 22, TokenType.DOLLAR, "$");
        assertToken(tokens, 23, TokenType.NUMBER, "22");
        assertToken(tokens, 24, TokenType.COMMA, ",");
        assertToken(tokens, 25, TokenType.PERCENT, "%");
        assertToken(tokens, 26, TokenType.STRING, "rdi");
        assertToken(tokens, 27, TokenType.EOL, "\n");
        assertToken(tokens, 28, TokenType.STRING, "syscall");
        assertToken(tokens, 29, TokenType.EOL, "\n");

        System.out.println("✓ All 33 tokens verified successfully");
    }

    private void assertToken(List<Token> tokens, int index, TokenType expectedType, String expectedLexeme) {
        Token token = tokens.get(index);
        Assertions.assertEquals(expectedType, token.type(),
                "Token[%d] type mismatch".formatted(index));
        Assertions.assertEquals(expectedLexeme, token.lexeme(),
                "Token[%d] lexeme mismatch".formatted(index));
    }
}
