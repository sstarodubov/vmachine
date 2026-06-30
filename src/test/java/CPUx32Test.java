import machine.CPUx32;
import machine.Memory;
import machine.Registers;
import machine.SysCallTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static machine.Opcodes.movl;
import static machine.Opcodes.syscall;
import static machine.OperandType.*;

class CPUx32Test {

    CPUx32 cpu;

    @Test
    void run() {
        /*
            movq $60, %eax         # EAX = 60 (номер syscall: sys_exit)
            movq $22, %edi         # EDI = 22 (код возврата)
            syscall                # Вызов ядра
         */
        cpu = CPUx32.builder()
                .registers(new Registers())
                .sysCallTable(new SysCallTable())
                .memory(Memory.builder()
                        .textSegment(ByteBuffer.wrap(new byte[]{
                                movl, NUMBER.code(), 0, 0, 0, 60, REGISTER.code(), 0, 0, 0, Registers.eax,
                                movl, NUMBER.code(), 0, 0, 0, 22, REGISTER.code(), 0, 0, 0, Registers.edi,
                                syscall
                        }))
                        .build())
                .build();

        Assertions.assertEquals(22, cpu.run());
    }
}