package algos;

public class LzwImageCompressor extends GeneralImageCompressor {

    @Override
    protected ICompressor GetCompressor() {
        return new LZW();
    }

}
