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
        assertThrows(IllegalStateException.class, () -> asm.compile());
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
         /*
                movl(2), NUMBER.code()(1), 0, 0, 0, 1, REGISTER.code()(2), Registers.eax(0),
                movl(2), REGISTOR.code()(2), Registers.eax(0), REGISTER.code()(2), Registers.edi(8),
                syscall
         */
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

    @Test
    void test7() {
         /*
                movl(2), NUMBER.code()(1), 0, 0, 0, 11, REGISTER.code()(2), Registers.ecx(4),
                movl(2), NUMBER.code()(1), 0, 0, 0, 22, REGISTER.code()(2), Registers.edi(8),
                addl(5), REGISTOR.code()(2), Registers.ecx(4), REGISTER.code()(2), Registers.edi(8),
         */
        final var program = """
               .globl _start
               
               .section .text
               _start:
                   movl $11, %ecx  # ECX = 11
                   movl $22, %edi  # EDI = 11
                   addl %ecx, %edi # EDI = ECX + EDI = 11 + 22 = 33
                """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(33, cpu.regStorage.readEdi());
    }


    @Test
    void test8() {
        final var program = """
               .globl _start
               
               .section .text
               _start:
                   movl $11, %ecx
                   addl $100, %ecx
                """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(111, cpu.regStorage.readEcx());
    }
}