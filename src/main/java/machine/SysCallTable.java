package machine;


import machine.syscalls.SysCall;
import machine.syscalls.SysExit;

import static machine.utils.Assertions.require;

public final class SysCallTable {
    private final SysCall[] syscalls;

    public SysCallTable() {
        syscalls = new SysCall[127];
        registerSysCall(new SysExit());
    }

    private void registerSysCall(final SysCall sysCall) {
        require(syscalls[sysCall.id()] != null, "syscalls must be unique");
        syscalls[sysCall.id()] = sysCall;
    }

    public void executeOn(final CPUx32 cpUx32, final int sysCallId) {
        syscalls[sysCallId].execute(cpUx32);
    }
}
