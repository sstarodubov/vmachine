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

    @Test
    void test9() {
        final var program = """
        .globl _start
        .section .text
        _start:
            movl $11, %ecx  # RCX = 11
            movl $55, %edi  # RDI = 55
            subl %ecx, %edi  # RDI = RDI - RCX= 55 - 11 = 44
            
            """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(44, cpu.regStorage.readEdi());
    }

    @Test
    void test10() {
        final var program = """
                .globl _start
                .section .text
                _start:
                    movl $4, %edi
                    incl %edi   # RDI = RDI + 1= 4 + 1 = 5
            """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(5, cpu.regStorage.readEdi());
    }

    @Test
    void test11() {
        final var program = """
                .globl _start
                .section .text
                _start:
                    movl $4, %edi
                    decl %edi   # RDI = RDI + 1= 4 + 1 = 5
            """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(3, cpu.regStorage.readEdi());
    }


    @Test
    void test12() {
        final var program = """
              .globl _start
              .section .text
              _start:
                  movl $2, %edi
                  movl $4, %eax
                  mull %edi           # EAX = EAX * EDI
            """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(8, cpu.regStorage.readEax());
    }

    @Test
    void test14() {
        //EDX:EAX = EAX × operand32
        final var program = """
              .globl _start
              .section .text
              _start:
                  movl $2147483647, %edi
                  movl $4, %eax
                  mull %edi           # EAX = EAX * EDI
            """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);

        final var result = ByteBuffer.allocate(8);
        result.putInt(cpu.regStorage.readEdx());
        result.putInt(4, cpu.regStorage.readEax());
        assertEquals((long) Integer.MAX_VALUE * 4, result.getLong(0));
    }

    @Test
    void test15() {
        //EDX:EAX = EAX × operand32
        final var program = """
              .globl _start
              .section .text
              _start:
                  movl $0x7fffffff, %edi
                  movl $4, %eax
                  mull %edi           # EAX = EAX * EDI
            """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);

        final var result = ByteBuffer.allocate(8);
        result.putInt(cpu.regStorage.readEdx());
        result.putInt(4, cpu.regStorage.readEax());
        assertEquals((long) Integer.MAX_VALUE * 4, result.getLong(0));
    }

    @Test
    void test16() {
        final var program = """
               .globl _start
               .section .text
               
               _start:
                   movl $11, %edi      
                   jmp exit           
                   movl $22, %edi    
               exit:                
                   movl $60, %eax
                   syscall
            """;

        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(11, cpu.statusCode);
    }
}