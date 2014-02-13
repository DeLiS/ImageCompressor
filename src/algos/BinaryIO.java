package algos;

public class BinaryIO {
    public static final int BYTESININT = 4;
    private final int SIZEOFBYTE = 8;
    private final int SIZEOFINT = 32;
    private final int MASK = 0xFF;
    private byte[] data;
    private int dataPointer;
    int bytesProceeded;
    int buffer;
    int bitsInBuffer;

    public BinaryIO(byte[] dest) {
        data = dest;
        dataPointer = 0;
        bytesProceeded = 0;
        bitsInBuffer = 0;
        clearBuffer();
    }

    public void WriteBits(int value, int bitsCount) {
        if (bufferHasFreeSpaceMoreThan(bitsCount)) {
            writeValueToBuffer(value, bitsCount);
        } else {
            int shift = freeBitsInBuffer();
            fillUpBufferWithPartOfValueBits(value, bitsCount, shift);
            copyBufferToData();
            copyRemainingBitsOfValueToBuffer(value, bitsCount, shift);
        }
    }

    private void copyRemainingBitsOfValueToBuffer(int value, int bitsCount, int shift) {
        buffer = value << (SIZEOFINT - bitsCount + shift);
        bitsInBuffer = bitsCount - shift;
        buffer >>>= (freeBitsInBuffer());
        bytesProceeded += 4;
    }

    private void copyBufferToData() {
        byte[] bufferAsBytes = bufferToByteArray();

        for (int i = 3; i >= 0; --i, dataPointer++) {
            data[dataPointer] = bufferAsBytes[i];
        }
    }

    private void fillUpBufferWithPartOfValueBits(int value, int bitsCount, int shift) {
        prepareBuffer(shift);
        value >>>= (bitsCount - shift);
        buffer |= value;
    }

    private void prepareBuffer(int shift) {
        if (bufferIsEmpty(shift)) {
            clearBuffer();
        } else {
            shiftBitsInBufferToTheLeft(shift);
        }
    }

    private void shiftBitsInBufferToTheLeft(int shift) {
        buffer <<= shift;
    }

    private boolean bufferIsEmpty(int shift) {
        return shift == SIZEOFINT;
    }

    private void clearBuffer() {
        buffer = 0;
    }

    private int freeBitsInBuffer() {
        return SIZEOFINT - bitsInBuffer;
    }

    private void writeValueToBuffer(int value, int bitsCount) {
        buffer <<= bitsCount;
        buffer |= value;
        bitsInBuffer += bitsCount;
    }

    private boolean bufferHasFreeSpaceMoreThan(int bitsCount) {
        return freeBitsInBuffer() > bitsCount;
    }

    private byte[] bufferToByteArray() {
        byte[] tmp = new byte[BYTESININT];
        tmp[0] = (byte) (((buffer << 24) >>> 24) & MASK);
        tmp[1] = (byte) (((buffer << 16) >>> 24) & MASK);
        tmp[2] = (byte) (((buffer << 8) >>> 24) & MASK);
        tmp[3] = (byte) (((buffer << 0) >>> 24) & MASK);
        return tmp;
    }

    public void Flush() {
        if (bitsInBuffer == 0)
            return;
        buffer <<= (freeBitsInBuffer());
        int bytesCount = (bitsInBuffer + SIZEOFBYTE - 1) / SIZEOFBYTE;
        byte[] tmp = bufferToByteArray();

        for (int i = 3; i >= 4 - bytesCount; --i, dataPointer++) {
            data[dataPointer] = tmp[i];
        }
        bytesProceeded += bytesCount;
    }

    public int ReadBits(int bitsCount) {
        if (bitsCount > SIZEOFINT) {
            //
        }
        int tmp = 0;
        while (bitsInBuffer <= SIZEOFINT - SIZEOFBYTE && dataPointer < data.length) {

            tmp = 0;
            tmp |= (data[dataPointer] & MASK);
            dataPointer += 1;
            writeValueToBuffer(tmp, SIZEOFBYTE);
        }
        int shift = freeBitsInBuffer();

        int copy = buffer;
        int result = ((copy << shift) >>> (SIZEOFINT - bitsCount));
        buffer <<= (bitsCount + shift);
        bitsInBuffer -= bitsCount;
        buffer >>>= (bitsCount + shift);
        return result;
    }

    public boolean CanMove() {
        return dataPointer < data.length;
    }

    public int GetTotalBytesProceeded() {
        return bytesProceeded;
    }

    public int GetBitsInBuffer() {
        return bitsInBuffer;
    }
}