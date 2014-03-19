package algos;

import java.util.Arrays;
import java.util.LinkedList;

public class LZW implements ICompressor {


    private final short CLEAR_CODE = 257;
    private final short EMPTY_CODE = 258;
    private final short END_CODE = 259;
    private final short POOL_START = END_CODE + 1;
    private final int MAX_CODE_LENGTH = 12;
    private final int MAGIC_NUMBER = (2 << (MAX_CODE_LENGTH)) - 1;
    private final int TABLE_CAPACITY = 2 << 13 + 1;
    private final int START_BITS_TO_WRITE = 9;
    private final int[] THRESHOLDS = {511, 1023, 2047, 4095, 8191};
    private final int CLEARTABLESIZE = 4095;

    private byte[] buffer;
    private int nextCode;
    private int bitsInCode;
    private int prevCode;
    private int valueCode;

    @Override
    public byte[] Compress(byte[] data) {

        BinaryIO writer = initBufferAndBinaryWriter(data);
        ListOfInts[] table = new ListOfInts[TABLE_CAPACITY];

        resetTable(table);
        writeClearCode(writer);

        codeDataBytes(data, writer, table);

        finishWritingCodes(writer);
        byte[] result = getUsedSubarrayOfBuffer(writer);
        return result;
    }

    private void codeDataBytes(byte[] data, BinaryIO writer, ListOfInts[] table) {
        for (int i = 0; i < data.length; ++i) {
            int currentByte = getCurrentByte(data[i]);
            int key = MakeKey(prevCode, currentByte);
            int hash = GetHash(key);

            boolean contains = getValueCodeIfContainsCurrentChain(table[hash], currentByte);

            if (contains) {
                prevCode = valueCode;
            } else {
                createNewEntryAndWriteItsCode(writer, table, currentByte, hash);
            }
        }
    }

    private void createNewEntryAndWriteItsCode(BinaryIO writer, ListOfInts[] table, int currentByte, int hash) {
        writer.WriteBits(prevCode, bitsInCode);
        addNewElementToHashTable(table, currentByte, hash);

        LinkedList<Integer> collisionsList = getCollisionListByPrevCodeAndCurByte(EMPTY_CODE, currentByte, table);
        prevCode = EMPTY_CODE;
        for (Integer value : collisionsList) {
            if (areEqualValues(prevCode, currentByte, value)) {
                prevCode = GetCode(value);
                break;
            }
        }

        ++nextCode;
        checkCodeRestrictions(writer, table);
    }

    private LinkedList<Integer> getCollisionListByPrevCodeAndCurByte(short previousCode, int currentByte, ListOfInts[] table) {
        int prevKey = MakeKey(previousCode, currentByte);
        return getCollisionListByKey(table, prevKey);
    }

    private LinkedList<Integer> getCollisionListByKey(ListOfInts[] table, int prevKey) {
        int prevHash = GetHash(prevKey);
        return getCollisionsList(table, prevHash);
    }

    private void checkCodeRestrictions(BinaryIO writer, ListOfInts[] table) {
        if (codeIsAThreshold(nextCode)) {
            ++bitsInCode;
        }
        if (reachedCodeSizeLimit()) {
            writer.WriteBits(prevCode, bitsInCode);
            writeClearCode(writer);
            resetTable(table);
        }
    }

    private BinaryIO initBufferAndBinaryWriter(byte[] data) {
        buffer = new byte[2 * (data.length + 2)];
        return new BinaryIO(buffer);
    }

    private void resetTable(ListOfInts[] table) {
        initHashTable(table);
        nextCode = POOL_START;
        bitsInCode = START_BITS_TO_WRITE;
        prevCode = EMPTY_CODE;
    }

    void initHashTable(ListOfInts[] table) {
        initListsInTable(table);
        addInTableSingleByteChains(table);
    }

    private void addInTableSingleByteChains(ListOfInts[] table) {
        for (int codedByte = 0; codedByte < 256; ++codedByte) {
            addSingleByteChainToTable(table, codedByte);
        }
    }

    private void addSingleByteChainToTable(ListOfInts[] table, int i) {
        LinkedList<Integer> collisionsList = getCollisionListByPrevCodeAndCurByte(EMPTY_CODE, i, table);
        int newElement = GetNewElement(i, EMPTY_CODE, i);
        addNewElementToCollisionsList(newElement, collisionsList);
    }

    int GetHash(int key) {
        int hash = (((key) >> MAX_CODE_LENGTH) ^ key) & (MAGIC_NUMBER);
        return hash;
    }

    int MakeKey(int prevKey, int lastByte) {
        int result = (prevKey << 8) | (lastByte & 0xFF);
        return result;
    }

    int GetNewElement(int curCode, int prevCode, int cur) {
        return (((curCode << 12) | prevCode) << 8) | (cur & 0xFF);
    }

    private LinkedList<Integer> getCollisionsList(ListOfInts[] table, int hash) {
        return table[hash].list;
    }

    private void addNewElementToCollisionsList(int newElement, LinkedList<Integer> collisionsList) {
        collisionsList.add(newElement);
    }

    private void initListsInTable(ListOfInts[] table) {
        for (int i = 0; i < TABLE_CAPACITY; ++i) {
            table[i] = new ListOfInts();
        }
    }

    private void writeClearCode(BinaryIO writer) {
        writer.WriteBits(CLEAR_CODE, bitsInCode);
    }

    private boolean reachedCodeSizeLimit() {
        return nextCode == CLEARTABLESIZE;
    }

    private int getCurrentByte(byte b) {
        return b & 0xFF;
    }

    //sets member variable valueCode
    private boolean getValueCodeIfContainsCurrentChain(ListOfInts listOfInts, int currentByte) {
        valueCode = 0;

        boolean contains = false;


        for (int j = 0; j < listOfInts.list.size() && !contains; ++j) {
            Integer value = listOfInts.list.get(j);
            valueCode = GetCode(value);
            contains = areEqualValues(prevCode, currentByte, value);
        }
        return contains;
    }

    private boolean areEqualValues(int previousCode, int currentByte, int value) {
        int valuePrevCode = GetPrevCode(value);
        int valueLastSymbol = GetLastSymbol(value);
        boolean result = previousCode == valuePrevCode && currentByte == valueLastSymbol;
        return result;
    }

    int GetLastSymbol(int value) {

        return (value & 0xFF);
    }

    int GetPrevCode(int value) {
        return (value << 12) >>> 20;
    }

    int GetCode(int value) {
        // ��� ������� �������
        return value >>> 20;
    }

    private void addNewElementToHashTable(ListOfInts[] table, int currentByte, int hash) {
        int newElement = GetNewElement(nextCode, prevCode, currentByte);
        LinkedList<Integer> collisionsList = getCollisionsList(table, hash);
        addNewElementToCollisionsList(newElement, collisionsList);
    }

    private void finishWritingCodes(BinaryIO writer) {
        writer.WriteBits(prevCode, bitsInCode);
        writer.WriteBits(END_CODE, bitsInCode);
        writer.Flush();
    }

    private byte[] getUsedSubarrayOfBuffer(BinaryIO writer) {
        return Arrays.copyOf(buffer, writer.GetTotalBytesProceeded());
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] Decompress(byte[] data) {

        buffer = new byte[(2 + data.length) * 2];
        int bytesCount = 0;
        bitsInCode = START_BITS_TO_WRITE;
        nextCode = POOL_START;
        BinaryIO reader = new BinaryIO(data);
        ListOfBytes[] table = new ListOfBytes[TABLE_CAPACITY];
        InitCodeArray(table);

        valueCode = reader.ReadBits(bitsInCode);
        prevCode = EMPTY_CODE;
        while (isNotEndCode()) {
            if (isClearCode()) {
                resetCodeArrayAndParams(table);
                readNextCode(reader);
                if (isEndCode()) {
                    return Arrays.copyOf(buffer, bytesCount);
                }
                bytesCount = addDataByCode(bytesCount, table);
            } else {
                if (tableIncludesValueCode(table)) {

                    bytesCount = createNewEntryInTable(bytesCount, table);
                    prevCode = valueCode;
                } else {
                    valueCode = nextCode;
                    bytesCount = createNewEntryInTable(bytesCount, table);
                    prevCode = valueCode;
                }
            }
            valueCode = reader.ReadBits(bitsInCode);
        }

        return Arrays.copyOf(buffer, bytesCount);
    }

    private int createNewEntryInTable(int bytesCount, ListOfBytes[] table) {
        table[nextCode] = new ListOfBytes();
        table[nextCode].list = (LinkedList<Byte>) table[prevCode].list.clone();
        table[nextCode].list.add(table[valueCode].list.get(0));
        bytesCount += AddData(bytesCount, valueCode, table);
        nextCode += 1;
        checkNextCodeSize();
        return bytesCount;
    }

    private void checkNextCodeSize() {
        if (codeIsAThreshold(nextCode + 1)) {
            ++bitsInCode;
        }
    }

    private boolean codeIsAThreshold(int code) {
        return Arrays.binarySearch(THRESHOLDS, code) >= 0;
    }

    private boolean tableIncludesValueCode(ListOfBytes[] table) {
        return table[valueCode] != null;
    }

    private void resetCodeArrayAndParams(ListOfBytes[] table) {
        InitCodeArray(table);
        nextCode = POOL_START;
        bitsInCode = START_BITS_TO_WRITE;
    }

    private void readNextCode(BinaryIO reader) {
        valueCode = reader.ReadBits(bitsInCode);
        valueCode = ((byte) valueCode) & 0xFF;
    }

    private int addDataByCode(int bytesCount, ListOfBytes[] table) {
        bytesCount += AddData(bytesCount, valueCode, table);
        prevCode = valueCode;
        return bytesCount;
    }

    public int AddData(int start, int code, ListOfBytes[] table) {

        ListOfBytes elem = table[code];
        int size = elem.list.size();
        if (start + size >= buffer.length) {
            int c = (start + size) / buffer.length + 1;
            byte[] newBuf = new byte[buffer.length * c];
            System.arraycopy(buffer, 0, newBuf, 0, buffer.length);
            buffer = newBuf;
        }
        for (int i = 0; i < size; ++i) {
            buffer[start + i] = elem.list.get(i);
        }
        return size;
    }

    private boolean isEndCode() {
        return valueCode == END_CODE;
    }

    private boolean isNotEndCode() {
        return valueCode != END_CODE;
    }

    private boolean isClearCode() {
        return valueCode == CLEAR_CODE;
    }

    void InitCodeArray(ListOfBytes[] table) {
        for (int i = 0; i < table.length; ++i) {
            table[i] = null;
        }
        table[CLEAR_CODE] = new ListOfBytes();
        for (int i = 0; i < 256; ++i) {
            table[i] = new ListOfBytes();
            table[i].list.add((byte) (i & 0xFF));
        }
    }

    private class ListOfInts {
        public LinkedList<Integer> list;

        public ListOfInts() {
            list = new LinkedList<Integer>();
        }
    }

    private class ListOfBytes {
        public LinkedList<Byte> list;

        public ListOfBytes() {
            list = new LinkedList<Byte>();
        }
    }

}
