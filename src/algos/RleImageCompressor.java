package algos;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class RleImageCompressor implements IImageCompressor {

    @Override
    public byte[] Compress(BufferedImage image) {


        int width = image.getWidth();
        int height = image.getHeight();
        int pixelCount = width * height;
        byte[] red = new byte[pixelCount];
        byte[] green = new byte[pixelCount];
        byte[] blue = new byte[pixelCount];

        getColourArraysFromImage(image, width, height, red, green, blue);
        RunLengthEncoder rle = new RunLengthEncoder();
        byte[] compressedRed = rle.Compress(red);
        byte[] compressedGreen = rle.Compress(green);
        byte[] compressedBlue = rle.Compress(blue);
        ByteBuffer bb = ByteBuffer.allocate(8 + compressedRed.length + compressedGreen.length + compressedBlue.length);
        bb.putInt(width);
        bb.putInt(height);
        putArrayInByteBuffer(compressedRed, bb);
        putArrayInByteBuffer(compressedGreen, bb);
        putArrayInByteBuffer(compressedBlue, bb);

        return bb.array();
    }

    private void getColourArraysFromImage(BufferedImage image, int width, int height, byte[] red, byte[] green, byte[] blue) {
        int current = 0;
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                int rgb = image.getRGB(i, j);
                red[current] = (byte) ((rgb >> 16) & 0xFF);
                green[current] = (byte) ((rgb >> 8) & 0xFF);
                blue[current] = (byte) ((rgb) & 0xFF);
                current++;
            }
        }
    }

    private void putArrayInByteBuffer(byte[] compressedRed, ByteBuffer bb) {
        for (int i = 0; i < compressedRed.length; ++i) {
            bb.put(compressedRed[i]);
        }
    }

    @Override
    public BufferedImage Decompress(byte[] image) {
        ByteBuffer byteBuffer;
        byteBuffer = ByteBuffer.wrap(image);
        int width = byteBuffer.getInt();
        int height = byteBuffer.getInt();
        int pixelCount = width * height;
        byte[] compressed = new byte[image.length - 8];
        byteBuffer = byteBuffer.get(compressed);
        RunLengthEncoder rle = new RunLengthEncoder();
        byte[] decompressed = rle.Decompress(compressed);
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int red = decompressed[i * width + j];
                int green = decompressed[pixelCount + i * width + j];
                int blue = decompressed[2 * pixelCount + i * width + j];
                if (red < 0)
                    red += 256;
                if (green < 0)
                    green += 256;
                if (blue < 0)
                    blue += 256;
                int rgb = (red << 16) | (green << 8) | blue;
                bi.setRGB(j, i, rgb);
            }
        }
        return bi;
    }

}
