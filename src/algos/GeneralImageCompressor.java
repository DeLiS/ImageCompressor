package algos;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public abstract class GeneralImageCompressor implements IImageCompressor {

	@Override
	public byte[] Compress(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int pixelCount = width * height;
		byte[] red = new byte[pixelCount];
		byte[] green = new byte[pixelCount];
		byte[] blue = new byte[pixelCount];

		int current = 0;
		for(int j=0;j<height;++j)
		{
			for(int i=0;i<width;++i)
			{
				int rgb = image.getRGB(i,j);		
				red[current] = 		(byte)((rgb >> 16) & 0xFF);
				green[current] = 	(byte)((rgb >>  8) & 0xFF);
				blue[current] = 	(byte)((rgb      ) & 0xFF);
				current++;
			}
		}
		ICompressor compressor = GetCompressor();
		byte[] compressedRed = compressor.Compress(red);
		compressor = GetCompressor();
		byte[] compressedGreen = compressor.Compress(green);
		compressor = GetCompressor();
		byte[] compressedBlue = compressor.Compress(blue);
		ByteBuffer bb = ByteBuffer.allocate(20 + compressedRed.length + compressedGreen.length + compressedBlue.length);
		
		bb.putInt(width);
		bb.putInt(height);
		bb.putInt(compressedRed.length);
		bb.putInt(compressedGreen.length);
		bb.putInt(compressedBlue.length);
		
		for(int i=0;i<compressedRed.length;++i)
		{
			bb.put(compressedRed[i]);
		}
		for(int i=0;i<compressedGreen.length;++i)
		{
			bb.put(compressedGreen[i]);
		}
		for(int i=0;i<compressedBlue.length;++i)
		{
			bb.put(compressedBlue[i]);
		}
		
		return bb.array();
	}

	@Override
	public BufferedImage Decompress(byte[] image) {
		//ByteBuffer byteBuffer = ByteBuffer.allocate(image.length);
		ByteBuffer byteBuffer = ByteBuffer.wrap(image);		
		
		int width = byteBuffer.getInt();
		int height = byteBuffer.getInt();
		int redLength = byteBuffer.getInt();
		int greenLength = byteBuffer.getInt();
		int blueLength = byteBuffer.getInt();
		
		byte[] redCompressed = new byte[redLength];
		byte[] greenCompressed = new byte[greenLength];
		byte[] blueCompressed = new byte[blueLength];		
		byteBuffer = byteBuffer.get(redCompressed,0,redLength);
		byteBuffer = byteBuffer.get(greenCompressed,0,greenLength);
		byteBuffer = byteBuffer.get(blueCompressed,0,blueLength);
		ICompressor compressor = GetCompressor();
		byte[] redDecompressed = compressor.Decompress(redCompressed);
		compressor = GetCompressor();
		byte[] greenDecompressed = compressor.Decompress(greenCompressed);
		compressor = GetCompressor();
		byte[] blueDecompressed = compressor.Decompress(blueCompressed);
		BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
		for(int i=0;i<height;++i)
		{
			for(int j=0;j<width;++j)
			{
				int red = redDecompressed[i*width + j];
				int green = greenDecompressed[i*width + j];
				int blue = blueDecompressed[i*width + j];
				if(red < 0)
					red += 256;
				if(green < 0)
					green += 256;
				if(blue < 0)
					blue += 256;
				int rgb = (red<<16)|(green<<8)|blue;
				bi.setRGB(j, i, rgb);
			}
		}
		return bi;
	}
	
	protected abstract ICompressor GetCompressor();

}
