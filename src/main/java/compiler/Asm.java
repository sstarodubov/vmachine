package compiler;

import compiler.operand.Number;
import compiler.operand.Operand;
import compiler.operand.Register;
import machine.opcodes.Opcode;
import machine.OperandType;
import machine.RegStorage;
import machine.utils.IntegerUtils;

import java.nio.ByteBuffer;
import java.util.*;

import static compiler.Asm.ProcessedSection.INIT;
import static machine.utils.Assertions.require;

public final class Asm {
    enum ProcessedSection {
        BSS, TEXT, DATA, INIT
    }

    private ProcessedSection processedSection = INIT;
    private final Tokenizer tokenizer;
    private Token curToken = null;
    private final ByteBuffer codeBuff;
    private final List<String> globals = new ArrayList<>();
    private final Map<String, Integer> labels = new HashMap<>();

    int codePos = 32  /*32 bytes for header*/;
    int textSegStart = -1;
    int textSegEnd = -1;
    int dataSegStart = -1;
    int dataSegEnd = -1;

    public Asm(final String program) {
        this.tokenizer = new Tokenizer(program);
        this.codeBuff = ByteBuffer.allocate(64);
    }

    private void consume(final TokenType expectedType) {
        require(curToken.type() == expectedType, "unexpected token: %s. expected: %s".formatted(curToken, expectedType));
        curToken = tokenizer.nextToken();
    }

    private void consume() {
        curToken = tokenizer.nextToken();
    }

    public ByteBuffer compile() {
        curToken = tokenizer.nextToken();
        while (curToken.type() != TokenType.EOF) {
            switch (curToken.type()) {
                case HASHTAG -> compileComment();
                case DOT -> compileDot();
                case EOL -> consume(TokenType.EOL);
                case STRING -> compileString();
                default -> throw new IllegalStateException("unexpected token type: %s".formatted(curToken.type()));
            }
        }
        closeProcessingSections();
        postHandle();
        return codeBuff;
    }

    private void postHandle() {
        fillHeader();
    }

    private void fillHeader() {
        /* header structure:
           0: address of main function
           4: text segment start
           8: text segment end
           12: data segment start
           16: data segment end
         */

        //main function
        require(globals.size() == 1, "only one main function is allowed");
        final String mainFn = globals.getFirst();
        final Integer addrMainFn = labels.get(mainFn);
        require(addrMainFn != null, "address of main function must exists");
        writeToCodeBuff(0, addrMainFn, 4);

        // text segment
        writeToCodeBuff(4, textSegStart, 4);
        writeToCodeBuff(8, textSegEnd, 4);

        // data segment
        writeToCodeBuff(12, dataSegStart, 4);
        writeToCodeBuff(16, dataSegEnd, 4);

        codeBuff.limit(codePos);
    }

    private void writeToCodeBuff(final int pos, final int data, final int size) {
        switch (size) {
            case 4 -> codeBuff.putInt(pos, data);
            case 2 -> codeBuff.putShort(pos, (short) data);
            case 1 -> codeBuff.put(pos, (byte) data);
            default -> throw new IllegalStateException("unsupported num size: %d".formatted(size));
        }
    }

    private void appendToCodeBuff(int num, int size) {
        switch (size) {
            case 4 -> codeBuff.putInt(codePos, num);
            case 2 -> codeBuff.putShort(codePos, (short) num);
            case 1 -> codeBuff.put(codePos, (byte) num);
            default -> throw new IllegalStateException("unsupported num size: %d".formatted(size));
        }
        codePos += size;
    }

    private void compileOperands2(final int operSize) {
        final Operand firstOperand = switch (curToken.type()) {
            case PERCENT -> {
                // case %reg, %reg
                consume(TokenType.PERCENT);
                require(curToken.type() == TokenType.STRING, "after percent must be name of register, but: %s".formatted(curToken));
                final String sourceRegName = curToken.lexeme();
                final int sourceRegId = RegStorage.registerIdFromName(sourceRegName);
                appendToCodeBuff(OperandType.REGISTER.code, 1);
                appendToCodeBuff(sourceRegId, 1);
                consume(TokenType.STRING);

                yield new Register(sourceRegName);
            }

            case DOLLAR -> {
                // case $num, %reg
                consume(TokenType.DOLLAR);
                require(curToken.type() == TokenType.NUMBER, "after $ must be int, but %s".formatted(curToken));
                final int num = IntegerUtils.parseInt(curToken.lexeme());
                appendToCodeBuff(OperandType.NUMBER.code, 1);
                appendToCodeBuff(num, operSize);
                consume(TokenType.NUMBER);
                yield new Number(num);
            }
            default -> throw new IllegalStateException("unexpected mov operand: %s".formatted(curToken));
        };

        consume(TokenType.COMMA);

        consume(TokenType.PERCENT);
        require(curToken.type() == TokenType.STRING, "after percent must be name of register, but: %s".formatted(curToken));
        final int targetRegId = RegStorage.registerIdFromName(curToken.lexeme());
        switch (firstOperand) {
            case Number(int num) -> {
                require(RegStorage.isCompatibleSize(num, curToken.lexeme()), "register must have size %d".formatted(operSize));
                require(RegStorage.isCompatibleMovSemantic(operSize, curToken.lexeme()), "incorrect register id '%d' used with `%d' size".formatted(targetRegId, operSize));
            }
            case Register(String name) ->
                require(RegStorage.isEq(name, curToken.lexeme()), "incorrect register id '%d' used with `%d' size".formatted(targetRegId, operSize));
        }

        require(targetRegId != -1, "register id must be known");
        appendToCodeBuff(OperandType.REGISTER.code, 1);
        appendToCodeBuff(targetRegId, 1);
        consume(TokenType.STRING);
    }


    private void compileString() {
        switch (Opcode.fromString(curToken.lexeme())) {
            case MOVL -> {
                appendToCodeBuff(Opcode.MOVL.code, 1);
                consume(TokenType.STRING);
                compileOperands2( 4);
            }
            case SYSCALL -> {
                appendToCodeBuff(Opcode.SYSCALL.code, 1);
                consume(TokenType.STRING);
            }
            case NOP -> {
            }
            case MOVW -> {
                appendToCodeBuff(Opcode.MOVW.code, 1);
                consume(TokenType.STRING);
                compileOperands2( 2);
            }
            case MOVB -> {
                appendToCodeBuff(Opcode.MOVB.code, 1);
                consume(TokenType.STRING);
                compileOperands2( 1);
            }
            case ADDL -> {
                appendToCodeBuff(Opcode.ADDL.code, 1);
                consume(TokenType.STRING);
                compileOperands2(4);
            }
            case null -> {
                // it is label
                final var labelName = curToken.lexeme();
                consume(TokenType.STRING);
                consume(TokenType.COLON);
                labels.put(labelName, codePos);
            }

        }
    }

    private void compileDot() {
        consume(TokenType.DOT);
        require(curToken.type() == TokenType.STRING, "after '.' symbol must be string. But: %s".formatted(curToken));
        switch (curToken.lexeme()) {
            case "globl" -> {
                consume(TokenType.STRING);
                final String directive = curToken.lexeme();

                globals.add(directive);
                consume(TokenType.STRING);
            }
            case "section" -> {
                consume(TokenType.STRING);
                consume(TokenType.DOT);
                final String sectionName = curToken.lexeme();
                switch (sectionName) {
                    case "text" -> {
                        require(textSegStart == -1, "section text must be one");
                        textSegStart = codePos;
                        closeProcessingSections();
                        processedSection = ProcessedSection.TEXT;
                    }
                    case "data" -> {
                        require(dataSegStart == -1, "section data must be one");
                        dataSegStart = codePos;
                        closeProcessingSections();
                        processedSection = ProcessedSection.DATA;
                    }
                    default -> throw new IllegalStateException("unknown section: %s".formatted(sectionName));
                }
                consume(TokenType.STRING);
            }
            default -> throw new IllegalStateException("unknown string '%s' after dot".formatted(curToken.lexeme()));
        }
    }

    void closeProcessingSections() {
        switch (processedSection) {
            case TEXT -> textSegEnd = codePos - 1;
            case DATA -> dataSegEnd = codePos - 1;
            case INIT -> {
            }
            default -> throw new IllegalStateException("unexpected section to close: %s".formatted(processedSection));
        }
    }

    private void compileComment() {
        while (curToken.type() != TokenType.EOL) {
            consume();
        }
        consume(TokenType.EOL);
    }
}
