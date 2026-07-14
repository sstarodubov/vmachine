package machine;


import machine.syscalls.SysCall;
import machine.syscalls.SysExit;
import machine.syscalls.SysWrite;

import static machine.utils.Assertions.require;

public final class SysCallTable {
    private final SysCall[] syscalls;

    public SysCallTable() {
        syscalls = new SysCall[127];
        registerSysCall(new SysExit());
        registerSysCall(new SysWrite());
    }

    private void registerSysCall(final SysCall sysCall) {
        require(syscalls[sysCall.id()] == null, "syscalls must be unique");
        syscalls[sysCall.id()] = sysCall;
    }

    public void executeOn(final CPU CPU, final int sysCallId) {
        syscalls[sysCallId].execute(CPU);
    }
}
