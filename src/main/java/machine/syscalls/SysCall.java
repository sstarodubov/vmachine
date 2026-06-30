package machine.syscalls;

import machine.CPUx32;

public interface SysCall {

    void execute(CPUx32 cpUx32);

    int id();
}
