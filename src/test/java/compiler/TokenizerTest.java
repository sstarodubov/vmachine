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
        Assertions.assertEquals(33, tokens.size(), "Expected 33 tokens");

        // Assert - проверяем каждый токен
        assertToken(tokens, 0, TokenType.HASHTAG, "#");
        assertToken(tokens, 1, TokenType.STRING, "first");
        assertToken(tokens, 2, TokenType.STRING, "program");
        assertToken(tokens, 3, TokenType.EOL, "\n");
        assertToken(tokens, 4, TokenType.DOT, ".");
        assertToken(tokens, 5, TokenType.STRING, "globl");
        assertToken(tokens, 6, TokenType.STRING, "_start");
        assertToken(tokens, 7, TokenType.EOL, "\n");
        assertToken(tokens, 8, TokenType.EOL, "\n");
        assertToken(tokens, 9, TokenType.DOT, ".");
        assertToken(tokens, 10, TokenType.STRING, "section");
        assertToken(tokens, 11, TokenType.DOT, ".");
        assertToken(tokens, 12, TokenType.STRING, "text");
        assertToken(tokens, 13, TokenType.EOL, "\n");
        assertToken(tokens, 14, TokenType.STRING, "_start");
        assertToken(tokens, 15, TokenType.COLON, ":");
        assertToken(tokens, 16, TokenType.EOL, "\n");
        assertToken(tokens, 17, TokenType.STRING, "movq");
        assertToken(tokens, 18, TokenType.DOLLAR, "$");
        assertToken(tokens, 19, TokenType.NUMBER, "60");
        assertToken(tokens, 20, TokenType.COMMA, ",");
        assertToken(tokens, 21, TokenType.PERCENT, "%");
        assertToken(tokens, 22, TokenType.STRING, "rax");
        assertToken(tokens, 23, TokenType.EOL, "\n");
        assertToken(tokens, 24, TokenType.STRING, "movq");
        assertToken(tokens, 25, TokenType.DOLLAR, "$");
        assertToken(tokens, 26, TokenType.NUMBER, "22");
        assertToken(tokens, 27, TokenType.COMMA, ",");
        assertToken(tokens, 28, TokenType.PERCENT, "%");
        assertToken(tokens, 29, TokenType.STRING, "rdi");
        assertToken(tokens, 30, TokenType.EOL, "\n");
        assertToken(tokens, 31, TokenType.STRING, "syscall");
        assertToken(tokens, 32, TokenType.EOL, "\n");

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
