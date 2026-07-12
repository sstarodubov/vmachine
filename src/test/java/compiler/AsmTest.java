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
                   addl $2, %edi     
                   subl $1, %ecx    
                   jnz loop        

                   movl $60, %eax
                   syscall
            """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(10, cpu.statusCode);
    }


    @Test
    void test23() {
        final var program = """
              .globl _start
              .section .text
              _start:
                  movl $1, %ebx
                  movl $0, %eax
                  cmpl %ebx, %eax     # сравниваем RAX и RBX. Фактически вычитаем RAX - RBX
                  jc carry_set        # если произошел перенос
                  movl $2, %edi       # если нет переноса
                  jmp exit
              carry_set:
                  movl $4, %edi       # если есть перенос
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
    void test24() {
        final var program = """
            .globl _start
            .text
            _start:
                movl $33, %ecx
                movl $22, %edx
                cmpl %ecx, %edx
                je equal       
                movl $2, %edi 
                jmp exit
            equal:
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
    void test25() {
        final var program = """
                .globl _start
                .text
                _start:
                    movl $0x7fffffff, %ecx
                    movl $1, %edx
                    addl %ecx, %edx         # складываем RCX и RDX, устанавливается флаг переноса CF
                
                    movl $2, %ecx           # вариант, если флаг переноса сброшен (CF = 0)
                    movl $4, %edx           # вариант, если флаг переноса установлен (CF = 1)
                
                    cmovncl %ecx, %edi        # Если CF = 0
                    cmovcl %edx, %edi         # Если CF = 1
                
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
    void test26() {
        final var program = """
                .globl _start
                .text
                _start:
                    movl $0, %ecx
                    movl $1, %edx
                    addl %ecx, %edx         # складываем RCX и RDX, устанавливается флаг переноса CF
                
                    movl $2, %ecx           # вариант, если флаг переноса сброшен (CF = 0)
                    movl $4, %edx           # вариант, если флаг переноса установлен (CF = 1)
                
                    cmovncl %ecx, %edi        # Если CF = 0
                    cmovcl %edx, %edi         # Если CF = 1
                
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
    void test27() {
        final var program = """
                .globl _start
                .text
                _start:
                    movl $2, %ecx
                    movl $2, %edx
                    cmpl %ecx, %edx         # сравниваем RCX и RDX, устанавливается флаг нуля ZF
    
                    movl $8, %ecx           # вариант, если флаг нуля сброшен (ZF = 0)
                    movl $16, %edx           # вариант, если флаг нуля установлен (ZF = 1)
    
                    cmovnel %ecx, %edi        # Если ZF = 0
                    cmovel %edx, %edi         # Если ZF = 1
    
                    movl $60, %eax
                    syscall
               """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(16, cpu.statusCode);
    }



    @Test
    void test28() {
        final var program = """
                .globl _start
                .text
                _start:
                    movl $3, %ecx
                    movl $2, %edx
                    cmpl %ecx, %edx         # сравниваем RCX и RDX, устанавливается флаг нуля ZF
    
                    movl $8, %ecx           # вариант, если флаг нуля сброшен (ZF = 0)
                    movl $16, %edx           # вариант, если флаг нуля установлен (ZF = 1)
    
                    cmovnel %ecx, %edi        # Если ZF = 0
                    cmovel %edx, %edi         # Если ZF = 1
    
                    movl $60, %eax
                    syscall
               """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(8, cpu.statusCode);
    }


    @Test
    void test29() {
        final var program = """
                .globl _start
                .text
                _start:
                    movl $5, %ecx   # регистр-счетчик
                    movl $0, %edi
                mainloop:           # цикл
                    addl $2, %edi   # некоторые действия цикла
                    loopl mainloop  # уменьшаем значение в %rcx на 1, переходим к метке mainloop, если %rcx не содержит 0
    
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