package test;

import algos.ICompressor;
import algos.RunLengthEncoder;

/**
 * Created by Denis on 23.02.14.
 */
public class RleTest extends CompressionTest {
    @Override
    protected String getTestFileName() {
        return "RLE.txt";
    }

    @Override
    protected ICompressor getCompressor() {
        return new RunLengthEncoder();
    }
}
