package compiler;

import machine.CPU;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class AsmTest {

    @Test
    void test() {
        /*
                movl, NUMBER.code(), 0, 0, 0, 60, REGISTER.code(), Registers.eax,
                movl, NUMBER.code(), 0, 0, 0, 22, REGISTER.code(), Registers.edi,
                syscall
         */
        final var program = """
                 # first program
                .globl _start
                
                .section .text
                _start:
                    movl $60, %eax
                    movl $22, %edi
                    syscall
                """;

        final  var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        assertEquals(22, cpu.run(code));
    }

    @Test
    void test2() {
        final var program = """
                 # first program
                .globl _start
                
                .section .text
                _start:
                    movw $61, %ax
                """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(61, cpu.regStorage.readAx());
    }

    @Test
    void test3() {
        final var program = """
                 # first program
                .globl _start
                
                .section .text
                _start:
                    movb $3, %al
                """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(3, cpu.regStorage.readAl());
    }

    @Test
    void test13() {
        final var program = """
                 # first program
                .globl _start
                
                .section .text
                _start:
                    movb $3, %eax
                """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        assertThrows(IllegalStateException.class, () -> cpu.run(code));
    }


    @Test
    void test4() {
        final var program = """
                 # first program
                .globl _start
                
                .section .text
                _start:
                    movb $100, %ah
                """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(100, cpu.regStorage.readAh());
    }

    @Test
    void test5() {
        final var program = """
                 # first program
                .globl _start
                
                .section .text
                _start:
                    movl $0x80, %eax
                """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(128, cpu.regStorage.readEax());
    }

    @Test
    void test6() {
        final var program = """
                 # first program
                .globl _start
                
                .section .text
                _start:
                    movl $1, %eax
                    movl %eax, %edx
                """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(1, cpu.regStorage.readEdx());
    }
}