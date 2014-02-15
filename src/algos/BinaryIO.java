package algos;

public class BinaryIO {
    public static final int BYTESININT = 4;
    private final int SIZEOFBYTE = 8;
    private final int SIZEOFINT = 32;
    private final int MASK = 0xFF;
    int bytesProceeded;
    int buffer;
    int bitsInBuffer;
    private byte[] data;
    private int dataPointer;

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
            copyFullBufferToData();
            copyRemainingBitsOfValueToBuffer(value, bitsCount, shift);
        }
    }

    private void copyRemainingBitsOfValueToBuffer(int value, int bitsCount, int shift) {
        buffer = value << (SIZEOFINT - bitsCount + shift);
        bitsInBuffer = bitsCount - shift;
        buffer >>>= (freeBitsInBuffer());

    }

    private void copyFullBufferToData() {
        byte[] bufferAsBytes = bufferToByteArray();

        for (int i = (BYTESININT - 1); i >= 0; --i, dataPointer++) {
            data[dataPointer] = bufferAsBytes[i];
        }
        bytesProceeded += BYTESININT;
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
        if (bufferIsEmpty()) {
            return;
        }
        shiftBitsInBufferToTheLeft(freeBitsInBuffer());
        copyRemainingBufferToData();
    }

    private void copyRemainingBufferToData() {
        byte[] bufferAsBytes = bufferToByteArray();

        int bytesCount = (bitsInBuffer + SIZEOFBYTE - 1) / SIZEOFBYTE;
        for (int i = 3; i >= BYTESININT - bytesCount; --i, dataPointer++) {
            data[dataPointer] = bufferAsBytes[i];
        }
        bytesProceeded += bytesCount;
    }

    private boolean bufferIsEmpty() {
        return bitsInBuffer == 0;
    }


    public int ReadBits(int bitsCount) {
        assert bitsCount <= SIZEOFINT;
        fillBufferWithData();
        int result = readBitsFromBuffer(bitsCount);
        return result;
    }

    private void fillBufferWithData() {
        int scannedByte = 0;
        while (isSpaceForByteInBuffer() && HaveMoreData()) {
            scannedByte = readByte();
            writeValueToBuffer(scannedByte, SIZEOFBYTE);
        }
    }

    private int readBitsFromBuffer(int bitsCount) {
        int shift = freeBitsInBuffer();

        int copy = buffer;
        int result = ((copy << shift) >>> (SIZEOFINT - bitsCount));
        buffer <<= (bitsCount + shift);
        bitsInBuffer -= bitsCount;
        buffer >>>= (bitsCount + shift);
        return result;
    }

    private boolean isSpaceForByteInBuffer() {
        return bitsInBuffer <= SIZEOFINT - SIZEOFBYTE;
    }

    private int readByte() {
        int scannedByte;
        scannedByte = 0;
        scannedByte |= (data[dataPointer] & MASK);
        dataPointer += 1;
        return scannedByte;
    }

    public boolean HaveMoreData() {
        return dataPointer < data.length;
    }

    public int GetTotalBytesProceeded() {
        return bytesProceeded;
    }

}