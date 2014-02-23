package test;

import algos.HuffmanCompressor;
import algos.ICompressor;
import algos.LZW;
import algos.RunLengthEncoder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Denis on 23.02.14.
 */
public abstract class TestCaseGenerator {

    private ICompressor compressor;
    private int numberOfTests;
    private int maxTestLength;
    private Random random = new Random(System.currentTimeMillis());
    private byte[][] inputs;
    private byte[][] outputs;

    public static void main(String[] args) throws IOException{
        int n = 100;
        int len = 5000;
        String[] fileNames = {"RLE.txt", "LZW.txt", "Huffman.txt"};
        for(String fileName : fileNames){
            createTestFile(n, len, fileName);
        }
    }

    private static void createTestFile(final int n, final int len, String fileName) throws IOException {
        TestCaseGenerator testCaseGenerator = new TestCaseGenerator(n, len) {
            @Override
            protected ICompressor getCompressor() {
                return new RunLengthEncoder();
            }
        };
        testCaseGenerator.generateTests();
        testCaseGenerator.writeTestsToFile(fileName);
    }

    public TestCaseGenerator(int numberOfTests, int maxTestLength){
        this.numberOfTests = numberOfTests;
        this.maxTestLength = maxTestLength;
        inputs = new byte[numberOfTests][];
        outputs = new byte[numberOfTests][];
    }

    public void writeTestsToFile(String fileName) throws IOException{
        PrintWriter printWriter = new PrintWriter(fileName);
        for(int i = 0; i < numberOfTests; ++i){
           String input = Arrays.toString(inputs[i]);
           String output = Arrays.toString(outputs[i]);
           printWriter.println(input);
           printWriter.println(output);
        }
        printWriter.flush();
        printWriter.close();
    }

    public void generateTests() {
        generateInputs();
        generateOutputs();
    }

    private void generateInputs(){
        for(int i = 0; i < numberOfTests; ++i){
            inputs[i] = generateOneTest();
        }
    }

    private void generateOutputs(){
        for(int i = 0; i < numberOfTests; ++i){
            compressor = getCompressor();
            outputs[i] = compressor.Compress(inputs[i]);
        }
    }

    private byte[] generateOneTest(){
        int length = random.nextInt(maxTestLength);
        byte[] test = new byte[length];
        random.nextBytes(test);
        return test;
    }

    protected abstract ICompressor getCompressor();
}
