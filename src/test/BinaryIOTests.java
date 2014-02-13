package test;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import algos.BinaryIO;
public class BinaryIOTests {

	byte[] data;
	@Before
	public void setUp() throws Exception {
		data = new byte[10000];
	}

	@Test
	public void testWriteBits() {
		BinaryIO bio = new BinaryIO(data);
		Random r = new Random();
		int size = 5001;
		byte[] numbers = new byte[size];
		int[] bits = new int[size];
		for(int i=0;i<size;++i)
		{
			numbers[i] = (byte)(-127 + r.nextInt(250));
			bits[i] = 12;
		}
		for(int i=0;i<size;++i)
			bio.WriteBits(numbers[i] & 0xFF, bits[i]);
		bio.Flush();
		bio = new BinaryIO(data);
		for(int i=0;i<size;++i)
			assertEquals(numbers[i],(byte)bio.ReadBits(bits[i]));		
	}

	@Test
	public void testReadBits() {
		
	}

}
