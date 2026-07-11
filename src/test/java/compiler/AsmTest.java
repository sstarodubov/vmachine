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

    @Test
    void test17() {
        final var program = """
              .globl _start
              .section .text
              _start:
                  movl $exit, %ebx
                  movl $11, %edi
                  jmp *%ebx
                  movl $6, %edi
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

    @Test
    void test18() {
        final var program = """
             .globl _start
             .section .text
             _start:
                 movl $0x7fffffff, %ecx
                 movl $1, %edx
                 addl %ecx, %edx     # RDX = RDX + RCX
                 jc carry_set       # если флаг переноса установлен, переход к метке carry_set
                 movl $0, %edi       # если флаг переноса не установлен, RDI = 0
                 jmp exit
             carry_set:              # если флаг переноса установлен
                 movl $1, %edi       # RDI = 1
             exit:                   # метка exit
                 movl $60, %eax
                 syscall
            """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(1, cpu.statusCode);
    }

    @Test
    void test19() {
        final var program = """
             .globl _start
             .section .text
             _start:
                 movl $100, %ecx
                 movl $1, %edx
                 addl %ecx, %edx
                 jc carry_set     
                 movl $100, %edi   
                 jmp exit
             carry_set:         
                 movl $1, %edi 
             exit:            
                 movl $60, %eax
                 syscall
            """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(100, cpu.statusCode);
    }


    @Test
    void test20() {
        final var program = """
                .globl _start
                .section .text
                _start:
                    movl $5, %ecx
                    movl $5, %edx
                    subl %ecx, %edx     # RDX = RDX - RCX
                    jz zero_set         # если флаг нуля установлен, переход к метке zero_set
                    movl $2, %edi       # если флаг нуля не установлен, RDI = 2
                    jmp exit
                zero_set:              # если флаг нуля установлен
                    movl $4, %edi       # RDI = 4
                exit:
                    movl $60, %eax
                    syscall
            """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(4, cpu.statusCode);
    }


    @Test
    void test21() {
        final var program = """
                .globl _start
                .section .text
                _start:
                    movl $5, %ecx
                    movl $4, %edx
                    subl %ecx, %edx   
                    jz zero_set      
                    movl $2, %edi   
                    jmp exit
                zero_set:          
                    movl $4, %edi  
                exit:
                    movl $60, %eax
                    syscall
            """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(2, cpu.statusCode);
    }



    @Test
    void test22() {
        final var program = """
              .globl _start
               .section .text
               _start:
                   movl $5, %ecx
                   movl $0, %edi
               loop:
                   addl $2, %edi     # RDI = RDI + 2\s
                   subl $1, %ecx     # RCX = RCX - 1\s
                   jnz loop          # если флаг нуля НЕ установлен, переход обратно к метке loop

                   movl $60, %eax
                   syscall
            """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(10, cpu.statusCode);
    }
}