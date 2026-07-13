package compiler;

import compiler.operand.*;
import compiler.operand.Number;
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
    private final Map<String, Integer> labelsToAddress = new HashMap<>();
    private final Map<String, List<Integer>> labelsPosition = new HashMap<>();

    int codePos = 32  /*32 bytes for header*/;
    int textSegStart = -1;
    int textSegEnd = -1;
    int dataSegStart = -1;
    int dataSegEnd = -1;

    public Asm(final String program) {
        this.tokenizer = new Tokenizer(program);
        this.codeBuff = ByteBuffer.allocate(128);
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

        // проставляем реальные адреса вместо меток
        for (final var entry : labelsPosition.entrySet()) {
            for (final int position : entry.getValue()) {
                 codeBuff.putInt(position, labelsToAddress.get(entry.getKey()));
            }
        }
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
        final Integer addrMainFn = labelsToAddress.get(mainFn);
        require(addrMainFn != null, "address of main function must exists");
        writeToCodeBuff(0, addrMainFn);

        // text segment
        writeToCodeBuff(4, textSegStart);
        writeToCodeBuff(8, textSegEnd);

        // data segment
        writeToCodeBuff(12, dataSegStart);
        writeToCodeBuff(16, dataSegEnd);

        codeBuff.limit(codePos);
    }

    private void writeToCodeBuff(final int pos, final int data) {
            codeBuff.putInt(pos, data);
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

    private Operand compileOperands1() {
        return switch (curToken.type()) {
            case PERCENT ->  {
                //case %reg
                consume(TokenType.PERCENT);
                require(curToken.type() == TokenType.STRING, "after percent must be name of register, but: %s".formatted(curToken));
                final String regName = curToken.lexeme();
                final int regId = RegStorage.registerIdFromName(regName);
                require(regId != -1, "register must be supported: %s".formatted(regName));
                appendToCodeBuff(OperandType.REGISTER.code, 1);
                appendToCodeBuff(regId, 1);
                consume(TokenType.STRING);
                yield new Register(regName, regId);
            }
            case ASTERIX -> {
                consume(TokenType.ASTERIX);
                appendToCodeBuff(OperandType.ASTERIX.code, 1);
                compileOperands1();
                yield new Asterix();
            }
            case DOLLAR -> {
                consume(TokenType.DOLLAR);
                yield switch (curToken.type()) {
                    // $number
                    case NUMBER -> {
                        final int num = IntegerUtils.parseInt(curToken.lexeme());
                        appendToCodeBuff(OperandType.NUMBER.code, 1);
                        appendToCodeBuff(num, 4);
                        consume(TokenType.NUMBER);
                        yield new Number(num);
                    }
                    // case $label
                    case STRING -> {
                        handleLabel();
                        yield new Label();
                    }
                    default -> throw new UnsupportedOperationException("must be number or string. %s".formatted(curToken));
                };
            }
            case STRING -> {
                //label
                handleLabel();
                yield new Label();
            }
            default -> throw new IllegalStateException("Unexpected token value " + curToken.type());
        };
    }

    private void handleLabel() {
        appendToCodeBuff(OperandType.MEMORY_ADDR.code, 1); // указываем что это direct
        addLabelPosToFillLater(curToken.lexeme(), codePos); //сохраняем место куда потом нужно будет проставить физический адресс метки
        appendToCodeBuff(-1, 4); // это место пока заполянем числом -1
        consume(TokenType.STRING);
    }

    private void addLabelPosToFillLater(final String label, final int idx) {
       final var list = labelsPosition.getOrDefault(label, new ArrayList<>());
       list.add(idx);
       labelsPosition.put(label, list);
    }

    private void compileOperands2() {
        compileOperands1();

        consume(TokenType.COMMA);

        final Operand second = compileOperands1();
        require(second instanceof Register, "second operator must be register");
    }


    private void compileString() {
        switch (Opcode.fromString(curToken.lexeme())) {
            case MOVL -> {
                appendToCodeBuff(Opcode.MOVL.code, 1);
                consume(TokenType.STRING);
                compileOperands2();
            }
            case CMOVCL -> {
                appendToCodeBuff(Opcode.CMOVCL.code, 1);
                consume(TokenType.STRING);
                compileOperands2();
            }
            case CMOVNCL -> {
                appendToCodeBuff(Opcode.CMOVNCL.code, 1);
                consume(TokenType.STRING);
                compileOperands2();
            }
            case CMOVEL -> {
                appendToCodeBuff(Opcode.CMOVEL.code, 1);
                consume(TokenType.STRING);
                compileOperands2();
            }
            case CMOVNEL -> {
                appendToCodeBuff(Opcode.CMOVNEL.code, 1);
                consume(TokenType.STRING);
                compileOperands2();
            }
            case SYSCALL -> {
                appendToCodeBuff(Opcode.SYSCALL.code, 1);
                consume(TokenType.STRING);
            }
            case NOP ->
                    consume(TokenType.STRING);
            case ADDL -> {
                appendToCodeBuff(Opcode.ADDL.code, 1);
                consume(TokenType.STRING);
                compileOperands2();
            }
            case SUBL -> {
                appendToCodeBuff(Opcode.SUBL.code, 1);
                consume(TokenType.STRING);
                compileOperands2();
            }
            case null -> {
                // it is label
                final var labelName = curToken.lexeme();
                consume(TokenType.STRING);
                consume(TokenType.COLON);
                labelsToAddress.put(labelName, codePos);
            }
            case INQL -> {
                appendToCodeBuff(Opcode.INQL.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case DECL -> {
                appendToCodeBuff(Opcode.DECL.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case MULL -> {
                appendToCodeBuff(Opcode.MULL.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case JMP -> {
                appendToCodeBuff(Opcode.JMP.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case JC -> {
                appendToCodeBuff(Opcode.JC.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case LOOPL -> {
                appendToCodeBuff(Opcode.LOOPL.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case JZ -> {
                appendToCodeBuff(Opcode.JZ.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case JNZ -> {
                appendToCodeBuff(Opcode.JNZ.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case CMPL -> {
                appendToCodeBuff(Opcode.CMPL.code, 1);
                consume(TokenType.STRING);
                compileOperands2();
            }
            case JE -> {
                appendToCodeBuff(Opcode.JE.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case JNE -> {
                appendToCodeBuff(Opcode.JNE.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case JG -> {
                appendToCodeBuff(Opcode.JG.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case JGE -> {
                appendToCodeBuff(Opcode.JGE.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case JL -> {
                appendToCodeBuff(Opcode.JL.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case JLE -> {
                appendToCodeBuff(Opcode.JLE.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case ANDL -> {
                appendToCodeBuff(Opcode.ANDL.code, 1);
                consume(TokenType.STRING);
                compileOperands2();
            }
            case ORL -> {
                appendToCodeBuff(Opcode.ORL.code, 1);
                consume(TokenType.STRING);
                compileOperands2();
            }
            case XORL -> {
                appendToCodeBuff(Opcode.XORL.code, 1);
                consume(TokenType.STRING);
                compileOperands2();
            }
            case NOTL -> {
                appendToCodeBuff(Opcode.NOTL.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
            }
            case NEGL -> {
                appendToCodeBuff(Opcode.NEGL.code, 1);
                consume(TokenType.STRING);
                compileOperands1();
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
            case "text" -> {
                require(textSegStart == -1, "section text must be one");
                textSegStart = codePos;
                closeProcessingSections();
                processedSection = ProcessedSection.TEXT;
                consume(TokenType.STRING);
            }
            case "data" -> {
                require(dataSegStart == -1, "section data must be one");
                dataSegStart = codePos;
                closeProcessingSections();
                processedSection = ProcessedSection.DATA;
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
