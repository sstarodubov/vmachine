package machine;

import machine.utils.Assertions;

import java.nio.ByteBuffer;

import static machine.utils.Assertions.require;

public final class Memory {

    private final ByteBuffer mem;
    private int textSegStartIdx = -1;
    private int textSegEndIdx = -1;
    private int dataSegStartIdx = -1;
    private int dataSegEndIdx = -1;
    private final int memSize = 256;
    private final int stackSize = 64;
    public Memory(ByteBuffer mem) {
        this.mem = mem;
    }

    public Memory() {
        this.mem = ByteBuffer.allocate(memSize);
    }

    public void print(final int from, final int to) {
        for (int i = from; i < to; i++) {
            System.out.printf("%d:  %d%n", i, mem.get(i));
        }
    }

    public int size() {
       return memSize;
    }

    public int textSegmentSize() {
        return textSegEndIdx + 1;
    }

    public int readInt(final int offset) {
        return this.mem.getInt(offset);
    }

    public void writeInt(final int offset, final int data) {
        require(
                (offset >= dataSegStartIdx && offset <= dataSegEndIdx) ||
                        offset >= size() - stackSize, "segfault: %d".formatted(offset)
        );

        this.mem.putInt(offset, data);
    }

    public void writeByte(final int offset, final byte data) {
        mem.put(offset, data);
    }

    public byte readByte(final int offset) {
        return mem.get(offset);
    }

    public byte readTextByte(final int offset) {
        return mem.get(offset);
    }

    public short readTextShort(final int offset) {
        return mem.getShort(offset);
    }


    public int readTextInt(final int offset) {
        return mem.getInt(offset);
    }


    public void loadCode(final ByteBuffer code) {
        /* header structure:
           0: address of main function
           4: text segment start
           8: text segment end
           12: from segment start
           16: from segment end
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
    }
}
