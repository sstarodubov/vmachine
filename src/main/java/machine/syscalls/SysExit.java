package machine.syscalls;

import machine.CPUx32;

public class SysExit implements SysCall {

    @Override
    public void execute(final CPUx32 cpu) {
        final var status = cpu.registers.readEdi();
        cpu.setStatusCode(status);
    }

    @Override
    public int id() {
        return 60;
    }
}
