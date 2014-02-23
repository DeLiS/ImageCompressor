package test;

import algos.ICompressor;
import algos.LZW;

/**
 * Created by Denis on 23.02.14.
 */
public class LzwTest extends CompressionTest{

    @Override
    protected String getTestFileName() {
        return "LZW.txt";
    }

    @Override
    protected ICompressor getCompressor() {
        return new LZW();
    }
}
