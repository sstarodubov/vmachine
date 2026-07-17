package machine.syscalls;

import machine.CPU;

public class SysExit implements SysCall {

    @Override
    public void execute(final CPU cpu) {
        cpu.exitCode = cpu.regStorage.readEdi();
        cpu.exit = true;
    }

    @Override
    public int id() {
        return 60;
    }
}
