package algos;

public class HuffmanImageCompressor extends GeneralImageCompressor {

	@Override
	protected ICompressor GetCompressor() {
		return new HuffmanCompressor();
	}

}
