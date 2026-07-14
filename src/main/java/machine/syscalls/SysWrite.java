package machine.syscalls;

import machine.CPU;

public class SysWrite implements SysCall{

    @Override
    public void execute(CPU cpu) {
        final int addr = cpu.regStorage.readEsi();
        final int outDescriptor = cpu.regStorage.readEdi();
        final int len = cpu.regStorage.readEdx();
        final var buff = new byte[len];
        int j = 0;
        for (int i = addr; i < addr + len; i++) {
           buff[j++] = cpu.memory.readTextByte(i);
        }
        switch (outDescriptor) {
            case 1 -> System.out.println(new String(buff));
            case 2 -> System.err.println(new String(buff));
            default -> throw new UnsupportedOperationException();
        }
    }

    @Override
    public int id() {
        return 1;
    }
}
