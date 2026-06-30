package machine;

import lombok.Builder;

import java.nio.ByteBuffer;

@Builder
public class Memory {

    private final ByteBuffer textSegment;

    public int textSegmentSize() {
        return textSegment.limit();
    }

    public byte readTextByte(int offset) {
        return textSegment.get(offset);
    }

    public int readTextInt(int offset) {
        return textSegment.getInt(offset);
    }
}
