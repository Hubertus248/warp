package net.warpgame.engine.core.serialization;

/**
 * @author Hubertus
 * Created 01.07.2018
 */
public class SerializationBuffer {
    private byte[] buffer;
    private int readerIndex = 0;
    private int writerIndex = 0;

    private static int CHAR_SIZE = 2;
    private static int SHORT_SIZE = 2;
    private static int INT_SIZE = 4;
    private static int LONG_SIZE = 8;

    public SerializationBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public SerializationBuffer(int bufferSize) {
        this.buffer = new byte[bufferSize];
    }

    public SerializationBuffer(byte[] buffer, int readerIndex, int writerIndex) {
        this.buffer = buffer;
        this.writerIndex = writerIndex;
        this.readerIndex = readerIndex;
    }

    public void write(boolean val) {
        buffer[writerIndex] = (byte) (val ? 0 : 1);
        writerIndex++;
    }

    public void write(byte val) {
        buffer[writerIndex] = val;
        writerIndex++;
    }

    public void write(char val) {
        for (int i = CHAR_SIZE - 1; i >= 0; i--) {
            buffer[writerIndex + i] = (byte) (val & 0xFF);
            val >>= 8;
        }
        writerIndex += CHAR_SIZE;
    }

    public void write(short val) {
        for (int i = SHORT_SIZE - 1; i >= 0; i--) {
            buffer[writerIndex + i] = (byte) (val & 0xFF);
            val >>= 8;
        }
        writerIndex += SHORT_SIZE;
    }

    public void write(int val) {
        for (int i = INT_SIZE - 1; i >= 0; i--) {
            buffer[writerIndex + i] = (byte) (val & 0xFF);
            val >>= 8;
        }
        writerIndex += INT_SIZE;
    }

    public void write(long val) {
        for (int i = LONG_SIZE - 1; i >= 0; i--) {
            buffer[writerIndex + i] = (byte) (val & 0xFF);
            val >>= 8;
        }
        writerIndex += LONG_SIZE;
    }

    public void write(float val) {
        write(Float.floatToIntBits(val));
    }

    public void write(double val) {
        write(Double.doubleToLongBits(val));
    }

    public void write(String val) {
        write(val.length());
        for (int i = 0; i < val.length(); i++) write(val.charAt(i));
    }

    public boolean readBoolean() {
        readerIndex++;
        return buffer[readerIndex - 1] == (byte) 0;
    }

    public byte readByte() {
        readerIndex++;
        return buffer[readerIndex - 1];
    }

    public char readChar() {
        char val = 0;
        for (int i = 0; i < CHAR_SIZE; i++) {
            val <<= 8;
            val |= (buffer[readerIndex + i] & 0xFF);
        }
        readerIndex += CHAR_SIZE;
        return val;
    }

    public short readShort() {
        short val = 0;
        for (int i = 0; i < SHORT_SIZE; i++) {
            val <<= 8;
            val |= (buffer[readerIndex + i] & 0xFF);
        }
        readerIndex += SHORT_SIZE;
        return val;
    }

    public int readInt() {
        int val = 0;
        for (int i = 0; i < INT_SIZE; i++) {
            val <<= 8;
            val |= (buffer[readerIndex + i] & 0xFF);
        }
        readerIndex += INT_SIZE;
        return val;
    }

    public long readLong() {
        long val = 0;
        for (int i = 0; i < LONG_SIZE; i++) {
            val <<= 8;
            val |= (buffer[readerIndex + i] & 0xFF);
        }
        readerIndex += LONG_SIZE;
        return val;
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public String readString() {
        int length = readInt();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(readChar());
        }
        return stringBuilder.toString();
    }

    public int getReaderIndex() {
        return readerIndex;
    }

    public void setReaderIndex(int readerIndex) {
        this.readerIndex = readerIndex;
    }

    public int getWriterIndex() {
        return writerIndex;
    }

    public void setWriterIndex(int writerIndex) {
        this.writerIndex = writerIndex;
    }

    /**
     * Copies used part of the buffer into new byte array starting from 0 to (writerIndex - 1)
     *
     * @return copied part of the buffer
     */
    public byte[] getWrittenData() {
        byte[] out = new byte[writerIndex];
        System.arraycopy(buffer, 0, out, 0, writerIndex);
        return out;
    }

    public boolean isReadable() {
        return readerIndex < buffer.length - 1;
    }
}
