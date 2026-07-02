package compiler;

import machine.CPUx32;
import machine.Memory;
import machine.Registers;
import machine.SysCallTable;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class AsmTest {

    @Test
    void test() {
        /*
                movl, NUMBER.code(), 0, 0, 0, 60, REGISTER.code(), 0, 0, 0, Registers.eax,
                movl, NUMBER.code(), 0, 0, 0, 22, REGISTER.code(), 0, 0, 0, Registers.edi,
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

        final var cpu = CPUx32.builder()
                .registers(new Registers())
                .sysCallTable(new SysCallTable())
                .memory(Memory.builder()
                        .textSegment(code)
                        .build())
                .build();

        assertEquals(22, cpu.run(32));
    }
}