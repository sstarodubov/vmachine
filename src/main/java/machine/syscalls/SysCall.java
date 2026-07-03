package machine.syscalls;

import machine.CPU;

public interface SysCall {

    void execute(CPU CPU);

    int id();
}
