package algos;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public abstract class GeneralImageCompressor implements IImageCompressor {

    private int imageWidth;
    private int imageHeight;
    private byte[] red;
    private byte[] green;
    private byte[] blue;
    private byte[] compressedRed;
    private byte[] compressedGreen;
    private byte[] compressedBlue;

    @Override
	public byte[] Compress(BufferedImage image) {
        initSizeAndColourArraysForCompression(image);
        getColoursFromImage(image);
        compressColours();
        ByteBuffer bb = writeImageToByteBuffer();
		return bb.array();
	}

    private ByteBuffer writeImageToByteBuffer() {
        ByteBuffer bb = ByteBuffer.allocate(20 + compressedRed.length + compressedGreen.length + compressedBlue.length);

        bb.putInt(imageWidth);
        bb.putInt(imageHeight);
        bb.putInt(compressedRed.length);
        bb.putInt(compressedGreen.length);
        bb.putInt(compressedBlue.length);

        for(int i=0;i< compressedRed.length;++i)
        {
            bb.put(compressedRed[i]);
        }
        for(int i=0;i< compressedGreen.length;++i)
        {
            bb.put(compressedGreen[i]);
        }
        for(int i=0;i< compressedBlue.length;++i)
        {
            bb.put(compressedBlue[i]);
        }
        return bb;
    }

    private void compressColours() {
        ICompressor compressor = GetCompressor();
        compressedRed = compressor.Compress(red);
        compressor = GetCompressor();
        compressedGreen = compressor.Compress(green);
        compressor = GetCompressor();
        compressedBlue = compressor.Compress(blue);
    }

    private void getColoursFromImage(BufferedImage image) {
        int current = 0;
        for(int j=0;j< imageHeight;++j)
        {
            for(int i=0;i< imageWidth;++i)
            {
                int rgb = image.getRGB(i,j);
                red[current] = 		(byte)((rgb >> 16) & 0xFF);
                green[current] = 	(byte)((rgb >>  8) & 0xFF);
                blue[current] = 	(byte)((rgb      ) & 0xFF);
                current++;
            }
        }
    }

    private void initSizeAndColourArraysForCompression(BufferedImage image) {
        imageWidth = image.getWidth();
        imageHeight = image.getHeight();
        int pixelCount = imageWidth * imageHeight;
        red = new byte[pixelCount];
        green = new byte[pixelCount];
        blue = new byte[pixelCount];
    }

    @Override
	public BufferedImage Decompress(byte[] image) {
        readImageSizeAndColourArraysForDecompression(image);
        decompressColours();
        BufferedImage bufferedImage = createImageFromColourArrays();
		return bufferedImage;
	}

    private BufferedImage createImageFromColourArrays() {
        BufferedImage bufferedImage = new BufferedImage(imageWidth,imageHeight,BufferedImage.TYPE_3BYTE_BGR);
        for(int i=0;i<imageHeight;++i)
        {
            for(int j=0;j<imageWidth;++j)
            {
                int redByte = red[i*imageWidth + j];
                int greenByte = green[i*imageWidth + j];
                int blueByte = blue[i*imageWidth + j];
                if(redByte < 0)
                    redByte += 256;
                if(greenByte < 0)
                    greenByte += 256;
                if(blueByte < 0)
                    blueByte += 256;
                int rgb = (redByte<<16)|(greenByte<<8)|blueByte;
                bufferedImage.setRGB(j, i, rgb);
            }
        }
        return bufferedImage;
    }

    private void decompressColours() {
        ICompressor compressor = GetCompressor();
        red = compressor.Decompress(compressedRed);
        compressor = GetCompressor();
        green = compressor.Decompress(compressedGreen);
        compressor = GetCompressor();
        blue = compressor.Decompress(compressedBlue);
    }

    private void readImageSizeAndColourArraysForDecompression(byte[] image) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(image);

        imageWidth = byteBuffer.getInt();
        imageHeight = byteBuffer.getInt();
        int redLength = byteBuffer.getInt();
        int greenLength = byteBuffer.getInt();
        int blueLength = byteBuffer.getInt();

        compressedRed = new byte[redLength];
        compressedGreen = new byte[greenLength];
        compressedBlue = new byte[blueLength];

        byteBuffer = byteBuffer.get(compressedRed,0,redLength);
        byteBuffer = byteBuffer.get(compressedGreen,0,greenLength);
        byteBuffer = byteBuffer.get(compressedBlue,0,blueLength);
    }

    protected abstract ICompressor GetCompressor();

}
