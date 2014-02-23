package test;

import algos.ICompressor;
import org.junit.*;
import static org.junit.Assert.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Created by Denis on 23.02.14.
 */
public abstract class CompressionTest {
    protected String testFile;
    protected BufferedReader bufferedReader;
    protected String inputLine;
    protected String outputLine;
    @Before
    public void openTestFile() throws IOException{
        testFile = getTestFileName();
        bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile)));
    }

    @Test
    public void testCompression() throws IOException{
        while((inputLine = bufferedReader.readLine()) != null){
            readOutputLine();
            byte[] inputBytes = createBytesFromString(inputLine);
            byte[] outputBytes = createBytesFromString(outputLine);
            ICompressor compressor = getCompressor();
            byte[] compressionResult = compressor.Compress(inputBytes);
            byte[] decompressionResult = compressor.Decompress(outputBytes);

            assertArrayEquals(inputBytes, decompressionResult);
            assertArrayEquals(outputBytes, compressionResult);
        }
    }

    private byte[] createBytesFromString(String inputLine) {
        String inputWithoutBrackets = inputLine.substring(1, inputLine.length() - 1);
        String[] numbers = inputWithoutBrackets.split(",");
        byte[] result = new byte[numbers.length];
        for(int i = 0; i < numbers.length; ++i){
            String trimmed = numbers[i].trim();
            result[i] = Byte.valueOf(trimmed);
        }
        return result;
    }

    private void readOutputLine() throws IOException {
        outputLine = bufferedReader.readLine();
        if(outputLine == null){
            throw new IllegalArgumentException("Wrong number of lines in test file");
        }
    }


    @After
    public void closeTestFile() throws IOException{
        bufferedReader.close();
    }

    protected abstract String getTestFileName();
    protected abstract ICompressor getCompressor();
}
