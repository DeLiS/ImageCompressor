package algos;
import java.awt.image.*;
public interface IImageCompressor {
	public byte[] Compress(BufferedImage image);
	public BufferedImage Decompress(byte[] image);

}
