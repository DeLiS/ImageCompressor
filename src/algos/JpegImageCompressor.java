package algos;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class JpegImageCompressor implements IImageCompressor {

    public static final double C_FOR_U_EQ_ZERO = 1.0 / Math.sqrt(2.0);
    public static final int C_FOR_NON_ZERO_U = 1;
    private final int BLOCKSIZE = 8;
    private final int[][] ZigZagMatrix = {{0, 1, 5, 6, 14, 15, 27, 28},
            {2, 4, 7, 13, 16, 26, 29, 42},
            {3, 8, 12, 17, 25, 30, 41, 43},
            {9, 11, 18, 24, 31, 40, 44, 53},
            {10, 19, 23, 32, 39, 45, 52, 54},
            {20, 22, 33, 38, 46, 51, 55, 60},
            {21, 34, 37, 47, 50, 56, 59, 61},
            {35, 36, 48, 49, 57, 58, 62, 63}};
    private final int[][] YquantizationMatrix = {{16, 11, 10, 16, 24, 40, 51, 61},
            {12, 12, 14, 19, 26, 58, 60, 55},
            {14, 13, 16, 24, 40, 57, 69, 56},
            {14, 17, 22, 29, 51, 87, 80, 62},
            {18, 22, 37, 56, 68, 109, 103, 77},
            {24, 35, 55, 64, 81, 104, 113, 92},
            {49, 64, 78, 87, 103, 121, 120, 101},
            {72, 92, 95, 98, 112, 100, 103, 99}};
    private final int[][] CquantizationMatrix = {{17, 18, 24, 47, 99, 99, 99, 99},
            {18, 21, 26, 66, 99, 99, 99, 99},
            {24, 26, 56, 99, 99, 99, 99, 99},
            {47, 66, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99}};
    private int width;
    private int height;
    private double[][] Y;
    private double[][] Cb;
    private double[][] Cr;
    private double[][] Ycompressed;
    private double[][] CbCompressed;
    private double[][] CrCompressed;
    private double[][] C;
    private double[][][][] CC;
    private int[][] globalResultInt;
    private double compressionRatio;

    public JpegImageCompressor() {
        PreCalcCoefficients();
        compressionRatio = 1;
    }

    //����������� ���� C(i,u)*C(j,v)
    private void PreCalcCoefficients() {
        preCalcCs();
        preCalcCCs();
    }

    private void preCalcCCs() {
        CC = new double[BLOCKSIZE][BLOCKSIZE][BLOCKSIZE][BLOCKSIZE];
        for (int i = 0; i < BLOCKSIZE; ++i) {
            for (int j = 0; j < BLOCKSIZE; ++j) {
                for (int k = 0; k < BLOCKSIZE; ++k) {
                    for (int l = 0; l < BLOCKSIZE; ++l) {
                        CC[i][j][k][l] = C[i][j] * C[k][l];
                    }
                }
            }
        }
    }

    private void preCalcCs() {
        C = new double[BLOCKSIZE][BLOCKSIZE];
        for (int i = 0; i < BLOCKSIZE; ++i) {
            for (int j = 0; j < BLOCKSIZE; ++j) {
                C[i][j] = Coefficient(i, j);
            }
        }
    }

    private double Coefficient(int i, int u) {
        double c = C_FOR_NON_ZERO_U; // по формуле - CConst(u), но с 1 работает лучше;
        double result = c * Math.cos(((2 * i + 1) * u * Math.PI) / 2 / BLOCKSIZE);
        return result;
    }

    private static int GetCode(int n) {
        return Math.abs(n);
    }

    private static int GetNumber(int code, byte bitsCount) {

        int bitsCountCalculated = GetNumberOfBits(code);
        if (bitsCountCalculated < bitsCount) {
            return -code;
        }
        return code;

    }

    private static byte GetNumberOfBits(int n) {
        if (n == 0) return 0;
        byte carry = 0;
        if (n < 0) {
            n = -n;
            carry = 1;
        }
        byte result = (byte) (Math.floor(Math.log(1.0 * n) / Math.log(2.0)) + 1);
        return (byte) (result + carry);
    }

    private double CConst(double u) {
        if (u == 0) {
            return C_FOR_U_EQ_ZERO;
        } else {
            return C_FOR_NON_ZERO_U;
        }
    }

    @Override
    public byte[] Compress(BufferedImage image) {

        GetYUVimage(image);
        //��������� ������ �� ������, �������� � �������������� ������� �����
        initYCbCrMatrixes();
        int matrixCount = getTotalMatrixesCount();
        initGlobalResultArray(matrixCount);
        int counter = 0;
        counter = compressYmatrixes(counter);
        compressCbAndCrMatrixes(counter);

        byte[] result = PostProcess2(matrixCount);
        return result;
    }

    private void compressCbAndCrMatrixes(int counter) {
        for (int i = 0; i < height; i += 2 * BLOCKSIZE) {
            for (int j = 0; j < width; j += 2 * BLOCKSIZE) {
                counter = compressCbMatrix(counter, i, j);
                counter = compressCrMatrix(counter, i, j);
            }
        }
    }

    private int compressCrMatrix(int counter, int i, int j) {
        ForwardProcess2(Cr, i, j, 2, CquantizationMatrix, counter);
        ++counter;
        return counter;
    }

    private int compressCbMatrix(int counter, int i, int j) {
        ForwardProcess2(Cb, i, j, 2, CquantizationMatrix, counter);
        ++counter;
        return counter;
    }

    private int compressYmatrixes(int counter) {
        for (int i = 0; i < height; i += BLOCKSIZE) {
            for (int j = 0; j < width; j += BLOCKSIZE) {
                ForwardProcess2(Y, i, j, 1, YquantizationMatrix, counter);
                ++counter;
            }
        }
        return counter;
    }

    private void initGlobalResultArray(int matrixCount) {
        globalResultInt = new int[matrixCount][];
    }

    private int getTotalMatrixesCount() {
        return (width / BLOCKSIZE * height / BLOCKSIZE) / 2 * 3;
    }

    private byte[] PostProcess2(int matrixCount) {
        byte[] result = PostCompression(globalResultInt, matrixCount);
        return result;
    }

    private void GetYUVimage(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        int[][] red = new int[height][width];
        int[][] green = new int[height][width];
        int[][] blue = new int[height][width];
        Y = new double[height][width];
        Cb = new double[height][width];
        Cr = new double[height][width];

        fillRGBarraysFromBytes(image, red, green, blue);
        // ������� � YUV
        RGBtoYCbCr(red, green, blue);
    }

    private void fillRGBarraysFromBytes(BufferedImage image, int[][] red, int[][] green, int[][] blue) {
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                int rgb = image.getRGB(i, j);
                red[i][j] = (int) ((rgb >> 16) & 0xFF);
                green[i][j] = (int) ((rgb >> 8) & 0xFF);
                blue[i][j] = (int) ((rgb) & 0xFF);
            }
        }
    }

    private void RGBtoYCbCr(int[][] red, int[][] green, int[][] blue) {
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                Y[i][j] = 0.299 * red[i][j] + 0.587 * green[i][j] + 0.114 * blue[i][j];
                Cb[i][j] = -0.1687 * red[i][j] - 0.3313 * green[i][j] + 0.5 * blue[i][j] + 128;
                Cr[i][j] = 0.5 * red[i][j] - 0.4187 * green[i][j] - 0.0813 * blue[i][j] + 128;
            }
        }
    }

    private void ForwardProcess2(double[][] oldMatrix, int leftTopRow, int leftTopColumn, int step, int[][] quantizationMatrix, int counter) {
        double[][] matrix = getSubMatrixOfBlockSize(oldMatrix, leftTopRow, leftTopColumn, step);
        double[][] resultMatrix = DiscreteCosinusTransformation2(matrix, true);
        Quantization(resultMatrix, 0, 0, quantizationMatrix);
        int[] linearized = Scan2(resultMatrix);
        globalResultInt[counter] = linearized;
    }

    private double[][] getSubMatrixOfBlockSize(double[][] oldMatrix, int leftTopRow, int leftTopColumn, int step) {
        double[][] matrix = new double[BLOCKSIZE][BLOCKSIZE];
        for (int i = 0; i < BLOCKSIZE; ++i) {
            for (int j = 0; j < BLOCKSIZE; ++j) {
                matrix[i][j] = oldMatrix[leftTopRow + i * step][leftTopColumn + j * step];
            }
        }
        return matrix;
    }

    private void Quantization(double[][] matrix, int leftTopRow, int leftTopColumn, int[][] quantizationMatrix) {
        for (int i = 0; i < BLOCKSIZE; ++i) {
            for (int j = 0; j < BLOCKSIZE; ++j) {
                int row = leftTopRow + i;
                int column = leftTopColumn + j;
                matrix[row][column] = Math.floor(matrix[row][column] / (compressionRatio * quantizationMatrix[i][j]));
            }
        }
    }

    private int[] Scan2(double[][] matrix) {
        int[] tmp = new int[BLOCKSIZE * BLOCKSIZE];
        for (int i = 0; i < BLOCKSIZE; ++i) {
            for (int j = 0; j < BLOCKSIZE; ++j) {
                tmp[ZigZagMatrix[i][j]] = ((int) matrix[i][j]);
            }
        }
        return tmp;
    }

    public BufferedImage Decompress(byte[] image) {

        globalResultInt = PreDecompression(image);

        Y = new double[height][width];
        Cb = new double[height][width];
        Cr = new double[height][width];


        initYCbCrMatrixes();


        int blocksCount = globalResultInt.length;
        int Yend = 2 * blocksCount / 3;
        int Uend = Yend + (blocksCount - Yend) / 2;
        //int Vend = Uend + blocksCount / 3;
        decompressYmatrix(Yend);
        decompressCbAndCrMatrixes(blocksCount, Yend);
        FillGaps(Cb);
        FillGaps(Cr);


        int[][] R = new int[height][width];
        int[][] G = new int[height][width];
        int[][] B = new int[height][width];
        YUVtoRGB(R, G, B);
        BufferedImage bi = createImageFromRGB(R, G, B);
        return bi;
    }

    private BufferedImage createImageFromRGB(int[][] r, int[][] g, int[][] b) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int rgb = (r[i][j] << 16) | (g[i][j] << 8) | b[i][j];
                bi.setRGB(i, j, rgb);
            }
        }
        return bi;
    }

    private void YUVtoRGB(int[][] r, int[][] g, int[][] b) {
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                r[i][j] = (int) Math.round(Y[i][j] + 1.402 * (Cr[i][j] - 128));
                checkBoundariesForColour(r, i, j);
                g[i][j] = (int) Math.round(Y[i][j] - 0.34414 * (Cb[i][j] - 128) - 0.71414 * (Cr[i][j] - 128));
                checkBoundariesForColour(g, i, j);
                b[i][j] = (int) Math.round(Y[i][j] + 1.772 * (Cb[i][j] - 128));
                checkBoundariesForColour(b, i, j);
            }
        }
    }

    private void checkBoundariesForColour(int[][] colour, int i, int j) {
        if (colour[i][j] < 0) {
            System.out.println(colour[i][j]);
            colour[i][j] = colour[i][j] + 128;
        }
        if (colour[i][j] > 255) {
            System.out.println(colour[i][j]);
            colour[i][j] = colour[i][j] - 128;
        }
    }

    private void decompressCbAndCrMatrixes(int blocksCount, int yend) {
        int blockIndex = 0;
        for (int i = yend; i < blocksCount; ++i) {
            int blocksInRow = width / (2 * BLOCKSIZE);
            int leftTopRow = (blockIndex / blocksInRow) * BLOCKSIZE;//(i * BLOCKSIZE) / blocksInRow;
            int leftTopColumn = (blockIndex % blocksInRow) * BLOCKSIZE;
            BackwardProcess2(Cb, CbCompressed, globalResultInt[i], leftTopRow, leftTopColumn, 2, CquantizationMatrix);
            ++i;
            BackwardProcess2(Cr, CrCompressed, globalResultInt[i], leftTopRow, leftTopColumn, 2, CquantizationMatrix);
            blockIndex++;
        }
    }

    private void decompressYmatrix(int yend) {
        for (int i = 0; i < yend; ++i) {
            if (i < yend) {
                int blocksInRow = width / BLOCKSIZE;
                int leftTopRow = (i / blocksInRow) * BLOCKSIZE;//(i * BLOCKSIZE) / blocksInRow;
                int leftTopColumn = (i % blocksInRow) * BLOCKSIZE;

                BackwardProcess2(Y, Ycompressed, globalResultInt[i], leftTopRow, leftTopColumn, 1, YquantizationMatrix);
            }
        }
    }

    private int[][] PreDecompression(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        width = bb.getInt();
        height = bb.getInt();
        int matrixCount = bb.getInt();
        int quantitiesSize = bb.getInt();
        int binarySize = bb.getInt();
        byte[] quantitiesCompressed = new byte[quantitiesSize];
        byte[] binary = new byte[binarySize];
        bb = bb.get(quantitiesCompressed, 0, quantitiesSize);
        bb = bb.get(binary, 0, binarySize);
        byte[] quantities = decompressQuantitiesWithHuffman(quantitiesCompressed);
        BinaryIO bio = new BinaryIO(binary);
        int[][] result = decodeMatrix(matrixCount, quantities, bio);
        return result;
    }

    private int[][] decodeMatrix(int matrixCount, byte[] quantities, BinaryIO bio) {
        int[][] result = new int[matrixCount][BLOCKSIZE * BLOCKSIZE];
        int qIndex = decodeFirstColumn(result, quantities, bio);
        decodeRestOfTheMatrix(result, quantities, bio, qIndex);
        return result;
    }

    private void decodeRestOfTheMatrix(int[][] result, byte[] quantities, BinaryIO bio, int qIndex) {
        int bitsCount;
        int code;
        int value;
        int curRow = 0, curCol = 1;
        while (qIndex < quantities.length) {
            int zerosCount = quantities[qIndex++] & 0xFF;
            for (int i = 0; i < zerosCount; ++i) {
                result[curRow][curCol++] = 0;
                if (curCol == BLOCKSIZE * BLOCKSIZE) {
                    curRow++;
                    curCol = 1;
                }
            }
            bitsCount = quantities[qIndex++] & 0xFF;
            if (bitsCount == 0) {
                result[curRow][curCol++] = 0;
                if (curCol == BLOCKSIZE * BLOCKSIZE) {
                    curRow++;
                    curCol = 1;
                }
                continue;
            } else {
                code = bio.ReadBits(bitsCount);
                value = GetNumber(code, (byte) bitsCount);

                result[curRow][curCol++] = value;
                if (curCol == BLOCKSIZE * BLOCKSIZE) {
                    curRow++;
                    curCol = 1;
                }
            }

        }
    }

    private int decodeFirstColumn(int[][] result, byte[] quantities, BinaryIO bio) {
        int bitsCount = quantities[0];

        int code = 0;
        if (bitsCount > 0) {
            code = bio.ReadBits(bitsCount);
        }
        int value = GetNumber(code, (byte) bitsCount);

        result[0][0] = value;
        int qIndex = 1;
        for (int i = 1; i < result.length; ++i) {
            bitsCount = quantities[qIndex++];
            if (bitsCount > 0) {
                code = bio.ReadBits(bitsCount);
            } else {
                code = 0;
            }
            value = GetNumber(code, (byte) bitsCount);
            result[i][0] = result[i - 1][0] + value;
        }
        return qIndex;
    }

    private byte[] decompressQuantitiesWithHuffman(byte[] quantitiesCompressed) {
        HuffmanCompressor huffman = new HuffmanCompressor();
        return huffman.Decompress(quantitiesCompressed);
    }

    private void FillGaps(double[][] matrix) {
        int lastRow = matrix.length - 1;
        int lastColumn = matrix[0].length - 1;
        //��������� ��������� ������ � �������, ���� ��� ������
        if (lastRow % 2 == 1) {
            for (int j = 0; j < matrix[0].length - 1; j += 2) {
                matrix[lastRow][j] = matrix[lastRow - 1][j];
            }
            for (int j = 1; j < matrix[0].length - 1; j += 2) {
                matrix[lastRow][j] = (matrix[lastRow][j - 1] + matrix[lastRow][j + 1]) / 2;
            }

        }
        if (lastColumn % 2 == 1) {
            for (int i = 0; i < matrix.length - 1; i += 2) {
                matrix[i][lastColumn] = matrix[i][lastColumn - 1];
            }
            for (int i = 1; i < matrix.length - 1; i += 2) {
                matrix[i][lastColumn] = (matrix[i - 1][lastColumn] + matrix[i + 1][lastColumn]) / 2;
            }
        }
        if (lastColumn % 2 == 1 && lastRow % 2 == 1) {
            matrix[lastRow][lastColumn] = matrix[lastRow - 1][lastColumn - 1];
        }
        // ��������� �� ������ � �������, � ������� ���������� ���� ���-�� ���� ( ��� ������ ������ �������)
        for (int i = 0; i < matrix.length; i += 2) {
            for (int j = 1; j < matrix[i].length - 1; j += 2) {
                matrix[i][j] = (matrix[i][j - 1] + matrix[i][j + 1]) / 2;
                matrix[j][i] = (matrix[j - 1][i] + matrix[j + 1][i]) / 2;
            }
        }
        // ��������� ���������� ������ ( ������ ��� ��� ���������), ����� ������ ������.
        for (int i = 1; i < matrix.length - 1; i += 2) {
            for (int j = 1; j < matrix.length - 1; j += 2) {
                //������ �������, ������ ��� ��������� ������ ��������� �� �� ���������
                matrix[i][j] = (matrix[i - 1][j - 1] + matrix[i + 1][j - 1] + matrix[i + 1][j + 1] + matrix[i - 1][j + 1]) / 4;
            }
        }
    }

    //leftTopRow, leftTopColumn - � ������ �������
    private void BackwardProcess2(double[][] decompressedMatrix, double[][] compressedMatrix, int[] compressed, int leftTopRow, int leftTopColumn, int step, int[][] quantizationMatrix) {

        int[] linearized = compressed;
        double[][] matrix = new double[BLOCKSIZE][BLOCKSIZE];
        InversedScan2(linearized, matrix);
        DeQuantization(matrix, 0, 0, quantizationMatrix);

        double[][] result = DiscreteCosinusTransformation2(matrix, false);

        for (int i = 0; i < step * BLOCKSIZE; i += step) {
            for (int j = 0; j < step * BLOCKSIZE; j += step) {
                decompressedMatrix[step * leftTopRow + i][step * leftTopColumn + j] = result[i / step][j / step];
            }
        }
    }

    private void DeQuantization(double[][] compressedMatrix, int leftTopRow, int leftTopColumn, int[][] quantizationMatrix) {
        for (int i = 0; i < BLOCKSIZE; ++i) {
            for (int j = 0; j < BLOCKSIZE; ++j) {
                compressedMatrix[leftTopRow + i][leftTopColumn + j] *= (compressionRatio * quantizationMatrix[i][j]);
            }
        }
    }

    private void InversedScan2(int[] linearized, double[][] matrix) {
        int p = 0;
        for (int i = 0; i < BLOCKSIZE; ++i) {
            for (int j = 0; j < BLOCKSIZE; ++j) {
                matrix[i][j] = linearized[p++];// bb.getInt();// (int)(linearized[ ZigZagMatrix[i][j] ] & 0xFF);
            }
        }
    }

    private double[][] DiscreteCosinusTransformation2(double[][] matrix, boolean forward) {
        double[][] result = new double[BLOCKSIZE][BLOCKSIZE];
        for (int u = 0; u < BLOCKSIZE; ++u) {
            for (int v = 0; v < BLOCKSIZE; ++v) {

                result[u][v] = 0;
                for (int x = 0; x < BLOCKSIZE; ++x) {
                    for (int y = 0; y < BLOCKSIZE; ++y) {
                        if (forward) {
                            result[u][v] += CC[x][u][y][v] * matrix[x][y];
                        } else {
                            result[u][v] += CConst(x) * CConst(y) * CC[u][x][v][y] * matrix[x][y];
                        }
                    }
                }
                if (forward) {
                    result[u][v] *= CConst(u) * CConst(v);
                }
                result[u][v] /= 4;
            }
        }
        return result;
    }

    private void initYCbCrMatrixes() {
        Ycompressed = new double[height][width];
        CbCompressed = new double[height / 2][width / 2];
        CrCompressed = new double[height / 2][width / 2];
    }

    private byte[] PostCompression(int[][] data, int matrixCount) {
        int[] firstColumn = getFirstColumn(data);
        int[] diffs = calculateDifferences(firstColumn);
        byte[] bufferForBinaryRepresentation = new byte[firstColumn.length * 16];
        BinaryIO bio = new BinaryIO(bufferForBinaryRepresentation);
        //в этом массиве будут храниться количества бит и количества нулей
        byte[] quantities = new byte[8 * data.length * data[0].length];
        int bitsPointer = 0;
        byte[] quantitiesUsed = calculateQuantitiesUsed(data, firstColumn, diffs, bio, quantities, bitsPointer);
        byte[] quantitiesCompressed = compressWithHuffman(quantitiesUsed);
        int quantitiesSize = quantitiesCompressed.length;
        int binarySizeInBytes = bio.GetTotalBytesProceeded();
        byte[] binary = Arrays.copyOf(bufferForBinaryRepresentation, binarySizeInBytes);
        byte[] result = createResult(matrixCount, quantitiesCompressed, quantitiesSize, binarySizeInBytes, binary);

        return result;
    }

    private byte[] calculateQuantitiesUsed(int[][] data, int[] firstColumn, int[] diffs, BinaryIO bio, byte[] quantities, int bitsPointer) {
        //записываем сначала первый столбец
        bitsPointer = writeFirstColumn(diffs, bio, quantities, bitsPointer);
        // потом всё остальное по строкам
        int[] line = stretchMatrixInOneLineByRowsFromSecondColumn(data);
        int startIndex = firstColumn.length;
        bitsPointer = compressWithRLE(bio, quantities, bitsPointer, line, startIndex);
        return Arrays.copyOf(quantities, bitsPointer);
    }

    private int compressWithRLE(BinaryIO bio, byte[] quantities, int bitsPointer, int[] line, int startIndex) {
        byte bitsCount;
        int code;
        int curIndex = startIndex;
        while (curIndex < line.length) {
            int counter = 0;
            while (currentElementIsZero(line[curIndex]) && isNotLastElementInLine(line, curIndex)) {
                counter++;
                if (counter == 256) {
                    bitsPointer = addPairToArray(quantities, bitsPointer, (byte) 255, (byte) 0); //при раскодировке этот ноль пишется в результат, из битового массива ничего не читается
                    counter = 0;
                }
                ++curIndex;
            }
            if (counter > 0) {
                bitsPointer = addPairToArray(quantities, bitsPointer, (byte) (counter - 1), (byte) 0);
            }

            bitsCount = GetNumberOfBits(line[curIndex]);
            code = GetCode(line[curIndex]);
            bitsPointer = addPairToArray(quantities, bitsPointer, (byte) 0, bitsCount);
            if (bitsCount > 0) {
                bio.WriteBits(code, bitsCount);
            }
            ++curIndex;
        }
        bio.Flush();
        return bitsPointer;
    }

    private int addPairToArray(byte[] array, int startIndex, byte firstNumber, byte secondNumber) {
        array[startIndex] = firstNumber;
        array[startIndex + 1] = secondNumber;
        return startIndex + 2;
    }

    private byte[] compressWithHuffman(byte[] quantitiesUsed) {
        HuffmanCompressor huffman = new HuffmanCompressor();
        return huffman.Compress(quantitiesUsed);
    }

    private byte[] createResult(int matrixCount, byte[] quantitiesCompressed, int quantitiesSize, int binarySize, byte[] binary) {
        byte[] result = new byte[40 + quantitiesSize + binarySize];
        ByteBuffer bb = ByteBuffer.wrap(result);
        bb.putInt(width);
        bb.putInt(height);
        bb.putInt(matrixCount);
        bb.putInt(quantitiesSize);
        bb.putInt(binarySize);
        bb.put(quantitiesCompressed);
        bb.put(binary);
        return result;
    }

    private boolean isNotLastElementInLine(int[] line, int i) {
        return i < line.length - 1;
    }

    private boolean currentElementIsZero(int i) {
        return i == 0;
    }

    private int[] stretchMatrixInOneLineByRowsFromSecondColumn(int[][] data) {
        int[] line = new int[(data.length) * (data[0].length - 1)];
        int p = 0;
        for (int i = 0; i < data.length; ++i) {
            for (int j = 1; j < data[i].length; ++j)
                line[p++] = data[i][j];
        }
        return line;
    }

    private int writeFirstColumn(int[] diffs, BinaryIO bio, byte[] quantities, int bitsPointer) {
        byte bitsCount;
        int code;
        for (int i = 0; i < diffs.length; ++i) {
            bitsCount = GetNumberOfBits(diffs[i]);
            code = GetCode(diffs[i]);
            quantities[bitsPointer++] = bitsCount;
            if (bitsCount > 0)
                bio.WriteBits(code, bitsCount);
        }
        return bitsPointer;
    }

    private int[] calculateDifferences(int[] firstColumn) {
        int[] diffs = new int[firstColumn.length];
        diffs[0] = firstColumn[0];
        for (int i = 1; i < diffs.length; ++i)
            diffs[i] = firstColumn[i] - firstColumn[i - 1];
        return diffs;
    }

    private int[] getFirstColumn(int[][] data) {
        int[] firstColumn = new int[data.length];
        for (int i = 0; i < firstColumn.length; ++i) {
            firstColumn[i] = data[i][0];
        }
        return firstColumn;
    }
}
