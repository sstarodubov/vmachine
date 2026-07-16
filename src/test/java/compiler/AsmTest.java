package compiler;

import machine.CPU;
import machine.RegStorage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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

        final var asm = new Asm(program);
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


    @Test
    void test30() {
        final var program = """
                 .globl _start
                
                .text
                _start:
                
                    movl $12, %edi       # помещаем в регистр RDI число 12 - 1100
                    andl $6, %edi        # rdi = rdi AND 6 = 1100 AND 0110 = 0100 = 4
                
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
    void test31() {
        final var program = """
                 .globl _start
                
                 .text
                 _start:
                     movl $12, %edi      # помещаем в регистр rdi  число 12 - 1100
                     orl $6, %edi        # rdi = rdi OR 6 = 1100 OR 0110 = 1110 = 14
                
                     movl $60, %eax
                     syscall
                """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(14, cpu.statusCode);
    }


    @Test
    void test32() {
        final var program = """
                 .globl _start
                
                  .text
                  _start:
                
                      movl $12, %edi      # помещаем в регистр rdi  число 12 - 1100
                      xorl $6, %edi       # rdi = rdi XOR 6 = 1100 XOR 0110 = 1010 = 10
                
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
    void test33() {
        final var program = """
                 .globl _start
                
                   .text
                   _start:
                
                       xorl %edi, %edi    # rdi = 0
                       movl $12, %edi     # помещаем в регистр rdi число 12 - 00000000 00000000 00000000 00001100
                       notl %edi          # rdi =NOT(rdi)=NOT(12)= 11111111 11111111 11111111 11110011
                
                       movl $60, %eax
                       syscall
                """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(-13, cpu.statusCode);
    }


    @Test
    void test34() {
        final var program = """
                .globl _start
                
                .text
                _start:
                
                    movl $-12, %edi
                    negl %edi        # rdi = -1 * rdi = -1 * -12 = 12
                
                    movl $60, %eax
                    syscall
                """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(12, cpu.statusCode);
    }


    @Test
    void test35() {
        final var program = """
                 .globl _start
                
                .text
                _start:
                    movl $5, %edi       # в RDI число 5 или 00000101
                    shll $1, %edi       # сдвигаем число в RDI на 1 разряд влево = 00000101 << 1 = 00001010 = 10
                
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
    void test36() {
        final var program = """
                .globl _start
                
                  .text
                  _start:
                      movl $0x7fffffff , %eax   
                      shll $2, %eax     
                      jc carry_set     
                      movl $0, %edi
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

        assertEquals(1, cpu.statusCode);
    }

    @Test
    void test37() {
        final var program = """
                 .globl _start
                .text
                _start:
                
                    movl $69, %edi     # в RDI число 69 или 01000101
                    shrl $2, %edi      # сдвигаем число в RDI на 2 разряда вправо = 01000101 >> 2 = 00010001 = 17
                
                    movl $60, %eax
                    syscall
                """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(17, cpu.statusCode);
    }

    @Test
    void test38() {
        final var program = """
                .globl _start
                
                .text
                _start:
                    movl $-32, %edi
                    sarl $4, %edi  
                
                    movl $60, %eax
                    syscall
                """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(-2, cpu.statusCode);
    }

    @Test
    void test39() {
        final var program = """
                .globl _start
                
                .section .text
                number: .long 123   # определяем объект number внутри секции .text
                
                _start:
                    movl number, %edx   # RDX = number
                    movl %edx, %edi     # RDI = RDX = number
                    movl $60, %eax
                    syscall
                """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(123, cpu.statusCode);
    }

    @Test
    void test40() {
        final var program = """
                .globl _start
                
                 .data
                 number: .long 123       # переменная number в секции .data
                
                 .text
                 _start:
                     movl $67, %eax      # помещаем в AL число 67
                     movl %eax, number   # помещаем число из AL в переменную number
                     movl number, %edi  # помещаем число значение переменной number в RDI
                     movl $60, %eax
                     syscall
                """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(67, cpu.statusCode);
    }

    @Test
    void test41() {
        final var program = """
                .globl _start
                .data
                 nums: .long 15, 16, 17, 18
                
                .text
                _start:
                    movl nums, %edi  # AL = 15
                    movl $60, %eax
                    syscall
                """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(15, cpu.statusCode);
    }

    @Test
    void test42() {
        final var program = """
               .globl _start
               .data
               nums: .fill 3, 4, 5

               .text
               _start:
                   movl nums, %edi  # RDI = 5
                   movl $60, %eax
                   syscall
                """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(5, cpu.statusCode);
    }

    @Test
    void test43() {
        final var program = """
                .globl _start
    
                .data
                num: .long 5
    
                .text
                _start:
                    movl $num, %ebx     # помещаем в RBX адрес переменной num
                    movl (%ebx), %edi   # помещаем в RDI значение по адресу из RBX
                    movl $60, %eax
                    syscall
                """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        final int status = cpu.run(code);

        assertEquals(5, cpu.statusCode);
    }

    @Test
    void test44() {
        final var program = """
                .globl _start
    
                 .data
                 num: .long 100
    
                 .text
                 _start:
                     leal num, %ebx     # помещаем в RBX адрес переменной num
                     movl (%ebx), %edi   # помещаем в RDI значение по адресу из RBX
                     movl $60, %eax
                     syscall
                """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(100, cpu.statusCode);
    }

    @Test
    void test45() {
        final var program = """
             .globl _start

              .data
              nums: .long 11, 12, 13, 14, 15, 16

              .text
              _start:
                  movl $nums, %ebx     # помещаем в RBX адрес переменной nums
                  addl $8, %ebx       # прибавляем к адресу в RBX 8 байт
                  movl (%ebx), %edi   # помещаем в RDI значение по адресу из RBX (RDI = 12    )
                  movl $60, %eax
                  syscall
                """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(13, cpu.statusCode);
    }

    @Test
    void test46() {
        final var program = """
            .globl _start
            .data
            num1: .long 5
            num2: .long 6
            nums: .long 11, 12, 13, 14, 15, 16

            .text
            _start:
                movl $nums, %ebx     # помещаем в RBX адрес переменной nums
                subl $8, %ebx       # вычитаем от адресу в RBX 16 байт
                movl (%ebx), %edi   # помещаем в RDI значение по адресу из RBX (RDI = 5)
                movl $60, %eax
                syscall
                
            """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(5, cpu.statusCode);
    }

    @Test
    void test47() {
        final var program = """
                .globl _start
                .data
                nums: .long 1, 2, 3, 4, 5
                count: .long 5      # количество элементов в массиве nums
                .text
                _start:
                    movl $nums, %ebx     # помещаем в RBX адрес массива nums
                    movl count, %ecx    # помещаем в RCX количество элементов в массиве nums
                    movl $0, %edi       # будущая сумма элементов массива
                main_loop:
                    addl (%ebx), %edi   # складываем  значение по адресу из RBX с числом в RDI
                    addl $4, %ebx       # перемещаемся к следующему элементу массива
                    subl $1, %ecx       # отнимаем 1 от значения в RCX
                    jnz main_loop       # если счетчик RCX не равен 0, то переходим обратно к main_loop
                    movl $60, %eax
                    syscall
            """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(15, cpu.statusCode);
    }

    @Test
    void test48() {
        final var program = """
           .globl _start
           .data
           num1: .long 45
           nums: .long 11, 12, 13, 14, 15, 16

           .text
           _start:
               movl $nums, %ebx     # помещаем в RBX адрес массива nums
               movl 8(%ebx), %edi   # RDI =  значение по адресу (RBX+4)

               movl $60, %eax
               syscall
           """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(13, cpu.statusCode);
    }

    @Test
    void test49() {
        final var program = """
               .globl _start
               .data
               nums: .long 111, 112, 113, 114, 115, 116
    
               .text
               _start:
                   movl $nums, %ebx            # в RBX адрес массива nums, RBX - базовый регистр
                   movl $2, %esi               # RSI - индексный регистр  
                   movl (%ebx, %esi, 4), %edi  # в RDI число по адресу (RBX + RSI * 8)
    
                   movl $60, %eax
                   syscall
           """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(113, cpu.statusCode);
    }

    @Test
    void test50() {
        final var program = """
              .globl _start
               .data
               nums: .long 11, 12, 13, 14, 15, 16

               .text
               _start:
                   movl $2, %esi               # RSI - индексный регистр   \s
                   movl nums(, %esi, 4), %edi  # в RDI число по адресу ($nums + RSI * 4)

                   movl $60, %eax
                   syscall
           """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(13, cpu.statusCode);
    }

    @Test
    void test51() {
        final var program = """
              .globl _start
              .data
              nums: .long 1, 2, 3, 4, 5, 6
              count: .long .- nums
              

              .text
              _start:
                  movl count, %edi    # помещаем в RDI размер массива nums
                  movl $60, %eax
                  syscall
           """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(24, cpu.statusCode);
    }

    @Test
    void test52() {
        final var program = """
           .globl _start
           .data
           num1: .long 45
           nums: .long 11, 12, 13, 14, 15, 16

           .text
           _start:
               movl $nums, %ebx     # помещаем в RBX адрес массива nums
               movl -4(%ebx), %edi   # RDI =  значение по адресу (RBX-4)

               movl $60, %eax
               syscall
           """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(45, cpu.statusCode);
    }

    @Test
    void test53() {
        final var program = """
                .globl _start
                .text
                _start:
                    movl $'A', %edi  #  в регистре RDI числовой код символа A - 65

                    movl $60, %eax
                    syscall
           """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(65, cpu.statusCode);
    }

    @Test
    void test54() {
        final var program = """
                .globl _start
                .data
                charD: .long 'D'  # определяем один символ

                .text
                _start:
                    movl charD, %edi  #  в регистре RDI числовой код символа D - 68

                    movl $60, %eax
                    syscall
           """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(68, cpu.statusCode);
    }

    @Test
    void test55() {
        final var origin = System.out;
        try(var out = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(out));
            final var program = """
               .globl _start
               .data
               message: .asciz "Hello METANIT.COM"   # текст выводимого сообщения
               count: .long .- message                      # длина сообщения

               .text
               _start:
                   movl $message, %esi  # в RSI - адрес строки
                   movl $1, %edi        # в RDI - дексриптор вывода в стандартный поток (консоль)
                   movl count, %edx    # в RDX - длина строки
                   movl $1, %eax        # в RAX - номер функции для вывода в поток\s
                   syscall              # вызываем функцию Linux
           """;

            final var asm = new Asm(program);
            final ByteBuffer code = asm.compile();
            final var cpu = new CPU();
            cpu.run(code);

            assertEquals("Hello METANIT.COM\n", out.toString());
            System.setOut(origin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void test56() {
        final var program = """
            .globl _start
            .equ var1, 1
            .equ var2, 2
            .equ var3, 3

            .text
            _start:
                movl $var1, %edi    # RDI = 1
                addl $var2, %edi    # RDI = RDI + 2 = 3
                addl $var3, %edi    # RDI = RDI + 3 = 6
                movl $60, %eax
                syscall
           """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(6, cpu.statusCode);
    }


    @Test
    void test57() {
        final var program = """
               .globl _start
               .equ first, 0
               .equ second, 4
               .equ third, 8

               .data
               nums: .long 5, 6, 7

               .text
               _start:
                   movl $nums, %ebx        # в RBX адрес переменной nums
                   movl third(%ebx), %edi  # в RDI значение по адресу (RBX + third)

                   movl $60, %eax
                   syscall
           """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(7, cpu.statusCode);
    }


    @Test
    void test58() {
        final var origin = System.out;
        try(var out = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(out));
            final var program = """
           .globl _start
           
        
           .data
           # условная структура
           person: .ascii "Alice"  # имя
                   .long 34        # возраст
                   
           # смещение компонентов в структуре
           .equ NAME_OFFSET, 0
           .equ AGE_OFFSET, 5
           
           .text
           _start:
               movl $person, %esi   # в RSI - адрес строки
               movl $1, %edi        # в RDI - дексриптор вывода в стандартный поток (консоль)
               movl $AGE_OFFSET, %edx    # в RDX - длина строки
               movl $1, %eax        # в RAX - номер функции для вывода в поток\s
               syscall              # вызываем функцию Linux
               movl AGE_OFFSET(%esi), %edi  # в RDI - возраст
               movl $60, %eax
               syscall
           """;
            final var asm = new Asm(program);
            final ByteBuffer code = asm.compile();
            final var cpu = new CPU();
            cpu.run(code);
            assertEquals(34, cpu.statusCode);
            assertEquals("Alice\n", out.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.setOut(origin);
    }

    @Test
    void test59() {
        final var origin = System.out;
        try(var out = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(out));

            final var program = """
                    .globl _start
                    
                    .data
                    alice: .ascii "Alice"
                    alice_name_size: .long .- alice
                    
                    # условная структура
                    person:
                            .long alice  #имя алисы
                            .long alice_name_size   
                            .long 34     
                    
                    # смещение компонентов в структуре
                    .equ NAME_OFFSET, 0
                    .equ NAME_SIZE_OFFSET, 4
                    .equ AGE_OFFSET, 8 
                    
                    .text
                    _start:
                        movl $person, %ebx
                        movl NAME_OFFSET(%ebx), %esi  # в RSI - адрес строки
                    
                        movl $1, %edi        # в RDI - дексриптор вывода в стандартный поток (консоль)
                        movl NAME_SIZE_OFFSET(%ebx), %edx    # в RDX - длина строки
                        movl $1, %eax        # в RAX - номер функции для вывода в поток\s
                    
                        syscall              # вызываем функцию Linux
                    
                        movl AGE_OFFSET(%ebx), %edi  # в RDI - возраст
                        movl $60, %eax
                        syscall
                    """;
            final var asm = new Asm(program);
            final ByteBuffer code = asm.compile();
            final var cpu = new CPU();
            cpu.run(code);

            assertEquals(34, cpu.statusCode);
            assertEquals("Alice\n", out.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.setOut(origin);
    }

    @Test
    void test60() {
            final var program = """
                    .globl _start
                    
                    .text
                    _start:
                        movl $15, %edx
                        pushl %edx            # в стек помещаем содержимое регистра RDX
                        popl %edi            # значение из вершины стека помещаем в регистр RDI
                        movl $60, %eax
                        syscall
                    """;
            final var asm = new Asm(program);
            final ByteBuffer code = asm.compile();
            final var cpu = new CPU();
            cpu.run(code);

            assertEquals(15, cpu.statusCode);
    }
    @Test
    void test61() {
        final var program = """
                .globl _start
                 .text
                 _start:
                     pushfq      # сохраняем значения флагов
                     movl $0x7fffffff, %eax
                     addl $2, %eax
                     popfq       # восстанавливаем значения флагов
                     jc set      # если флаг CF установлен, переход к метке set
                     movl $2, %edi
                     jmp exit
                 set:
                     movl $3, %edi
                 exit:
                     movl $60, %eax
                     syscall""";
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);

        assertEquals(2, cpu.statusCode);
    }

    @Test
    void test62() {

        final var program = """
                .globl _start
                 .text
                 _start:
                     pushfq      # сохраняем значения флагов
                     popfq       # восстанавливаем значения флагов
                     
                    """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.regStorage.writeByte(RegStorage.sf, (byte) 1);
        cpu.regStorage.writeByte(RegStorage.zf, (byte) 2);
        cpu.regStorage.writeByte(RegStorage.of, (byte) 3);
        cpu.regStorage.writeByte(RegStorage.cf, (byte) 4);
        cpu.run(code);

        assertEquals(1, cpu.regStorage.readByte(RegStorage.sf));
        assertEquals(2, cpu.regStorage.readByte(RegStorage.zf));
        assertEquals(3, cpu.regStorage.readByte(RegStorage.of));
        assertEquals(4, cpu.regStorage.readByte(RegStorage.cf));
        assertEquals(cpu.memory.size(), cpu.regStorage.readEsp());
    }

    @Test
    void test63() {
        final var program = """
                .globl _start
                .text
                _start:
                    movl $11, %edi
                    movl $33, %edx
                
                    pushl %edi
                    pushl %edx
                
                    addl $8, %esp     # прибавляем к адресу в RSP 16 байт\s
                
                    movl $60, %eax
                    syscall
                    """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();
        cpu.run(code);
        assertEquals(cpu.memory.size(), cpu.regStorage.readEsp());
    }

    @Test
    void test64() {
        final var program = """
             .globl _start

             .text
             _start:
                 subl $8, %esp  # резервируем в стеке 16 байт
                 movl $11, %edx
                 movl %edx, (%esp)       # помещаем в стек значение регистра RDX
                 movl (%esp), %edi      # в RDI помещаем значение по адресу из RSP - число 11\s

                 addl $8, %esp     # восстанавливаем значение стека

                 movl $60, %eax
                 syscall
                    """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(11, cpu.readStackInt(-8));
        assertEquals(11, cpu.statusCode);
    }

    @Test
    void test65() {
        final var program = """
               .globl _start
              .text
              _start:
                  pushl $12
                  pushl $13
                  pushl $14
                  pushl $15

                  movl 8(%esp), %edi      # 16(%rsp) - адрес значения 13

                  addl $16, %esp     # восстанавливаем значение стека

                  movl $60, %eax
                  syscall
                    """;
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(13, cpu.statusCode);
    }

    @Test
    void test66() {
        final var program = """
             .globl _start
             .text
             _start:
                 subl $8, %esp     # резервируем в стеке 16 байт

                 movl $12, %ecx
                 movl $13, %edx

                 movl %ecx, 4(%esp)     # 4(%rsp) = 12
                 movl %edx, (%esp)     # (%rsp) = 13

                 movl (%esp), %edi      # rdi= 13
                 addl 4(%esp), %edi      # rdi = rdi + 12

                 addl $8, %esp     # восстанавливаем значение стека

                 movl $60, %eax
                 syscall""";
        final var asm = new Asm(program);
        final ByteBuffer code = asm.compile();
        final var cpu = new CPU();

        cpu.run(code);
        assertEquals(25, cpu.statusCode);
    }
}