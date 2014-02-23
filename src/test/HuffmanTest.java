package test;

import algos.HuffmanCompressor;
import algos.ICompressor;

/**
 * Created by Denis on 23.02.14.
 */
public class HuffmanTest extends CompressionTest {
    @Override
    protected String getTestFileName() {
        return "Huffman.txt";
    }

    @Override
    protected ICompressor getCompressor() {
        return new HuffmanCompressor();
    }
}
