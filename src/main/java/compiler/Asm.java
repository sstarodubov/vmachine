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
    private final Set<String> vars = new HashSet<>();

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

    /*
    VALUE(BASEREG, IDXREG, MULTIPLIER)

    Фактичеси адрес состовляется на основе 4 компонентов:
    VALUE: некоторое фиксированное значение - смещение.
    BASEREG: базовой регистр, который хранит базовый адрес
    IDXREG: индексный регистр
    MULTIPLIER: некоторое число-множитель, на который умножается значение в индексном регистре. Может принимать значения 1, 2, 4 или 8

    На основе этих компонентов адрес вычисляется следующим образом:

    address = VALUE + BASEREG + IDXREG * MULTIPLIE
     */
    private Operand createInDirectAddrOperand(final int value) {
        consume(TokenType.OPEN_PARENTHESIS);

        consume(TokenType.PERCENT);
        final int baseReg = RegStorage.registerIdFromName(curToken.lexeme());
        consume(TokenType.STRING);
        final int idxReg = curToken.type() == TokenType.COMMA ? createIdxReg() : -1;
        final int multiplier = curToken.type() == TokenType.COMMA ? createMultiplier() : 0;

        appendToCodeBuff(OperandType.INDIRECT_ADDR.code, 1);
        appendToCodeBuff(value , 4);
        appendToCodeBuff(baseReg, 1);
        appendToCodeBuff(idxReg, 1);
        appendToCodeBuff(multiplier, 4);
        consume(TokenType.CLOSE_PARENTHESIS);
        return new IndirectAddr();
    }

    private int createMultiplier() {
        consume(TokenType.COMMA);
        final int multiplier = IntegerUtils.parseInt(curToken.lexeme());
        consume(TokenType.NUMBER);
        return multiplier;
    }

    private int createIdxReg() {
        consume(TokenType.COMMA);
        consume(TokenType.PERCENT);
        final int register = RegStorage.registerIdFromName(curToken.lexeme());
        consume(TokenType.STRING);
        require(register != -1, "unknown register: " + curToken.lexeme());
        return register;
    }

    private Operand createRegisterOperand() {
        consume(TokenType.PERCENT);
        require(curToken.type() == TokenType.STRING, "after percent must be name of register, but: %s".formatted(curToken));
        final String regName = curToken.lexeme();
        final int regId = RegStorage.registerIdFromName(regName);
        require(regId != -1, "register must be supported: %s".formatted(regName));
        appendToCodeBuff(OperandType.REGISTER.code, 1);
        appendToCodeBuff(regId, 1);
        consume(TokenType.STRING);
        return new Register(regName, regId);
    }

    private Operand compileOperands1() {
        return switch (curToken.type()) {
            case OPEN_PARENTHESIS -> createInDirectAddrOperand(0);
            case NUMBER -> {
                final int value = IntegerUtils.parseInt(curToken.lexeme());
                consume(TokenType.NUMBER);
                yield createInDirectAddrOperand(value);
            }

            //case %reg
            case PERCENT -> createRegisterOperand();
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
                    case STRING -> createLabelOperand();
                    default ->
                            throw new UnsupportedOperationException("must be number or string. %s".formatted(curToken));
                };
            }
            case STRING -> vars.contains(curToken.lexeme()) ? createVarOperand()
                    : createLabelOperand();

            default -> throw new IllegalStateException("Unexpected token value " + curToken.type());
        };
    }

    private Operand createVarOperand() {
        appendToCodeBuff(OperandType.VARIABLE.code, 1); // указываем что это переменная
        addLabelPosToFillLater(curToken.lexeme(), codePos); //сохраняем место куда потом нужно будет проставить физический адресс переменной
        appendToCodeBuff(-1, 4); // это место пока заполянем числом -1
        consume(TokenType.STRING);
        return new Var();
    }

    private Operand createLabelOperand() {
        appendToCodeBuff(OperandType.DIRECT_ADDR.code, 1); // указываем что это direct
        addLabelPosToFillLater(curToken.lexeme(), codePos); //сохраняем место куда потом нужно будет проставить физический адресс метки
        appendToCodeBuff(-1, 4); // это место пока заполянем числом -1
        consume(TokenType.STRING);
        return new Label();
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
        require(second instanceof Register || second instanceof Var,
                "second operator must be register or variable");
    }


    private void compileString() {
        switch (Opcode.fromString(curToken.lexeme())) {
            case LEAL -> appendOpcode(Opcode.LEAL, 2);
            case MOVL -> appendOpcode(Opcode.MOVL, 2);
            case CMOVCL -> appendOpcode(Opcode.CMOVCL, 2);
            case CMOVNCL -> appendOpcode(Opcode.CMOVNCL, 2);
            case CMOVEL -> appendOpcode(Opcode.CMOVEL, 2);
            case CMOVNEL -> appendOpcode(Opcode.CMOVNEL, 2);
            case SYSCALL -> {
                appendToCodeBuff(Opcode.SYSCALL.code, 1);
                consume(TokenType.STRING);
            }
            case NOP -> consume(TokenType.STRING);
            case ADDL -> appendOpcode(Opcode.ADDL, 2);
            case SUBL -> appendOpcode(Opcode.SUBL, 2);
            case null -> {
                final var name = curToken.lexeme();
                consume(TokenType.STRING);
                consume(TokenType.COLON);
                labelsToAddress.put(name, codePos);

                if (curToken.type() == TokenType.DOT) {
                     declareVariable(name);
                }
            }
            case INQL -> appendOpcode(Opcode.INQL, 1);
            case DECL -> appendOpcode(Opcode.DECL, 1);
            case MULL -> appendOpcode(Opcode.MULL, 1);
            case JMP -> appendOpcode(Opcode.JMP, 1);
            case JC -> appendOpcode(Opcode.JC, 1);
            case LOOPL -> appendOpcode(Opcode.LOOPL, 1);
            case JZ -> appendOpcode(Opcode.JZ, 1);
            case JNZ -> appendOpcode(Opcode.JNZ, 1);
            case CMPL -> appendOpcode(Opcode.CMPL, 2);
            case JE -> appendOpcode(Opcode.JE, 1);
            case JNE -> appendOpcode(Opcode.JNE, 1);
            case JG -> appendOpcode(Opcode.JG, 1);
            case JGE -> appendOpcode(Opcode.JGE, 1);
            case JL -> appendOpcode(Opcode.JL, 1);
            case JLE -> appendOpcode(Opcode.JLE, 1);
            case ANDL -> appendOpcode(Opcode.ANDL, 2);
            case ORL -> appendOpcode(Opcode.ORL, 2);
            case XORL -> appendOpcode(Opcode.XORL, 2);
            case NOTL -> appendOpcode(Opcode.NOTL, 1);
            case NEGL -> appendOpcode(Opcode.NEGL, 1);
            case SHLL -> appendOpcode(Opcode.SHLL, 2);
            case SHRL -> appendOpcode(Opcode.SHRL, 2);
            case SARL -> appendOpcode(Opcode.SARL, 2);
        }
    }

    private void declareVariable(final String name) {
        consume(TokenType.DOT);
        switch (curToken.lexeme()) {
            // number
            case "long" -> {
                consume(TokenType.STRING);

                while (curToken.type() != TokenType.EOL) {
                    final int num = IntegerUtils.parseInt(curToken.lexeme());
                    consume(TokenType.NUMBER);
                    appendToCodeBuff(num, 4);
                    if (curToken.type() == TokenType.COMMA) {
                        consume(TokenType.COMMA);
                    } else {
                        break;
                    }
                }
            }
            case "fill" -> {
                consume(TokenType.STRING);
                final int repeat = IntegerUtils.parseInt(curToken.lexeme());
                consume(TokenType.NUMBER);
                consume(TokenType.COMMA);
                final int size = IntegerUtils.parseInt(curToken.lexeme());
                consume(TokenType.NUMBER);
                consume(TokenType.COMMA);
                final int value = IntegerUtils.parseInt(curToken.lexeme());
                consume(TokenType.NUMBER);

                for (int i = 0; i < repeat; i++) {
                    appendToCodeBuff(value, size);
                }
            }
            default -> throw new UnsupportedOperationException();
        }

        if (vars.contains(name)) {
            throw new IllegalStateException("vars duplicate: %s".formatted(name));
        }
        vars.add(name);
    }

    private void appendOpcode(final Opcode opcode, final int operCount) {
        appendToCodeBuff(opcode.code, 1);
        consume(TokenType.STRING);
        switch (operCount) {
            case 1 -> compileOperands1();
            case 2 -> compileOperands2();
            default -> throw new UnsupportedOperationException();
        }
    }

    private void compileDot() {
        consume(TokenType.DOT);
        require(curToken.type() == TokenType.STRING, "after '.' symbol must be string. But: %s".formatted(curToken));
        switch (curToken.lexeme()) {
            case "globl" -> {
                consume(TokenType.STRING);
                final String label = curToken.lexeme();

                globals.add(label);
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
