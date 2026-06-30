package machine.syscalls;

import machine.CPUx32;

public class SysExit implements SysCall {

    @Override
    public void execute(final CPUx32 cpu) {
        cpu.statusCode = cpu.registers.readEdi();
    }

    @Override
    public int id() {
        return 60;
    }
}
