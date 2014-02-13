package test;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import algos.BinaryIO;
public class BinaryIOTests {

    public static final int dataSize = 10000;
    byte[] data;
	@Before
	public void setUp() throws Exception {
		data = new byte[dataSize];
	}

	@Test
	public void testWriteBits() {
		BinaryIO bio = new BinaryIO(data);
		Random r = new Random();
		int inputSize = 5001;
		byte[] numbers = new byte[inputSize];
		int[] bits = new int[inputSize];

        generateNumbers(r, inputSize, numbers, bits);
        writeNumbers(bio, inputSize, numbers, bits);

        readNumbersAndAssert(inputSize, numbers, bits);

	}

    private void readNumbersAndAssert(int size, byte[] numbers, int[] bits) {
        BinaryIO bio = new BinaryIO(data);
        for(int i=0;i<size;++i){
            assertEquals(numbers[i],(byte)bio.ReadBits(bits[i]));
        }
    }

    private void writeNumbers(BinaryIO bio, int size, byte[] numbers, int[] bits) {
        for(int i=0;i<size;++i){
			bio.WriteBits(numbers[i] & 0xFF, bits[i]);
        }
        bio.Flush();
    }

    private void generateNumbers(Random r, int size, byte[] numbers, int[] bits) {
        for(int i=0;i<size;++i)
        {
            numbers[i] = (byte)(-127 + r.nextInt(250));
            bits[i] = 12;
        }
    }

    @Test
	public void testReadBits() {
		
	}

}
