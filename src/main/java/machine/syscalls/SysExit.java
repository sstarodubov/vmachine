package machine.syscalls;

import machine.CPU;

public class SysExit implements SysCall {

    @Override
    public void execute(final CPU cpu) {
        cpu.statusCode = cpu.regStorage.readEdi();
    }

    @Override
    public int id() {
        return 60;
    }
}
