package machine;

import machine.utils.Assertions;

import java.nio.ByteBuffer;

public final class Memory {

    private final ByteBuffer mem;
    private int textSegStartIdx = -1;
    private int textSegEndIdx = -1;
    private int dataSegStartIdx = -1;
    private int dataSegEndIdx = -1;

    public Memory(ByteBuffer mem) {
        this.mem = mem;
    }

    public Memory() {
        this.mem = ByteBuffer.allocate(1024);
    }

    public int textSegmentSize() {
        return textSegEndIdx + 1;
    }

    public byte readTextByte(int offset) {
        Assertions.require(offset >= textSegStartIdx, "seg fault: %d".formatted(offset));
        Assertions.require(offset <= textSegEndIdx, "seg fault: %d".formatted(offset));
        return mem.get(offset);
    }

    public int readTextInt(int offset) {
        return mem.getInt(offset);
    }

    public void loadCode(final ByteBuffer code) {
        /* header structure:
           0: address of main function
           4: text segment start
           8: text segment end
           12: data segment start
           16: data segment end
         */
        final int textSegmentStart = code.getInt(4);
        final int textSegmentEnd = code.getInt(8);
        final int dataSegmentStart = code.getInt(12);
        final int dataSegmentEnd = code.getInt(16);

        this.textSegStartIdx = textSegmentStart;
        this.textSegEndIdx = textSegmentEnd;
        this.dataSegStartIdx = dataSegmentStart;
        this.dataSegEndIdx = dataSegmentEnd;

        mem.put(code);
        mem.limit(code.limit());
    }
}
