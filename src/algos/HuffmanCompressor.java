package algos;

import java.util.Arrays;
import java.util.PriorityQueue;
public class HuffmanCompressor implements ICompressor {


    public static final int START_CODE = 0;
    public static final int START_CODE_LENGTH = 0;
    public static final int LEFT_BIT = 0;

    private class TreeNode implements Comparable<TreeNode>
	{
		public TreeNode left;
		public TreeNode right;
		public int quantity;
		public Byte character;
		public int size;
		public TreeNode()
		{
			left = right  = null;
			character = null;
			size = 1;
			quantity = 0;
		}		
		public TreeNode(Byte character, int quantity)
		{
			this();	
			size = 3;
			this.character = character;
			this.quantity = quantity;
		}
		public TreeNode(TreeNode left, TreeNode right)
		{
			this();
			this.left = left;
			this.right = right;
			this.quantity = left.quantity + right.quantity;
			this.size = 1 + left.size + right.size;
		}
		@Override
		public int compareTo(TreeNode node) {
			return this.quantity - node.quantity;
		}
	}
	
	private static final int BYTESCOUNT = 256;
	private static final int SIZEOFBYTE = 8;
	private static final int SIZEOFINT = 32;
	private int[] codes;
	private int[] codeLengths;
	private int[] quantities;
	private int differentBytesCount;
	private BinaryIO binaryIO;
	private TreeNode root;
	private int bitsCount;
	public HuffmanCompressor()
	{
		codes = new int[BYTESCOUNT];
		codeLengths = new int[BYTESCOUNT];
		quantities = new int[BYTESCOUNT];
		Arrays.fill(quantities, 0);
		Arrays.fill(codeLengths, -1);
		differentBytesCount = 0;
		bitsCount = 0;
	}
	@Override
	public byte[] Compress(byte[] data) {
        byte[] buffer = prepareBuffer(data);
		CalculateQuantities(data);
		BuildTree();
		Tour(root, START_CODE, START_CODE_LENGTH);
		writeQuantities();
        writeCodes(data);
		int totalLength = getTotalLengthOfBuffer();
        writeBitsCount(buffer);
		return Arrays.copyOf(buffer, totalLength);
	}

    private int getTotalLengthOfBuffer() {
        return binaryIO.GetTotalBytesProceeded();
    }

    private void writeBitsCount(byte[] buffer) {
        binaryIO = new BinaryIO(buffer);
        binaryIO.WriteBits(bitsCount,SIZEOFINT);// ����� � ������� �����
        binaryIO.Flush();
    }

    private void writeQuantities()
    {
        // �� ����� ���� ������, ��� 256 ��������� ����
        //(������ �� 1 ������, �.�. 0 ��������� ���� �� ����� ������, 0 - ������ 1)
        binaryIO.WriteBits(differentBytesCount - 1, SIZEOFBYTE);
        for(int i = 0; i < BYTESCOUNT; ++i)
        {
            if( codeLengths[i] >= 0)
            {
                binaryIO.WriteBits(i, SIZEOFBYTE);
                binaryIO.WriteBits(quantities[i], SIZEOFINT);
            }
        }

    }

    private void writeCodes(byte[] data) {
        for(int i = 0; i < data.length; ++i)
        {
            int index = data[i] & 0xFF;
            binaryIO.WriteBits(codes[index], codeLengths[index]);
            bitsCount += codeLengths[index];
        }
        binaryIO.Flush();
    }

    private byte[] prepareBuffer(byte[] data) {
        byte[] buffer = new byte[SIZEOFINT + data.length + (SIZEOFBYTE + SIZEOFINT)*BYTESCOUNT];
        binaryIO = new BinaryIO(buffer);
        binaryIO.WriteBits(0, SIZEOFINT);//�������� �����
        return buffer;
    }

    @Override
	public byte[] Decompress(byte[] data) {
		byte[] buffer = createBuffer(data);
		binaryIO = new BinaryIO(data);
        readBitsCount();
        ReadQuantities();
		BuildTree();
        int bufferPointer = readDataFromTreeToBuffer(buffer);
		return Arrays.copyOf(buffer, bufferPointer);
	}

    private int readDataFromTreeToBuffer(byte[] buffer) {
        TreeNode cur = root;
        int bufferPointer = 0;
        while(bitsCount > 0)
        {
            bitsCount -= 1;
            int bit = binaryIO.ReadBits(1);
            if(bit == LEFT_BIT)
            {
                cur = cur.left;
            }
            else
            {
                cur = cur.right;
            }
            if(cur.character != null)
            {
                buffer[bufferPointer] = cur.character;
                bufferPointer += 1;
                cur = root;
            }
        }
        return bufferPointer;
    }

    private void readBitsCount() {
        bitsCount = binaryIO.ReadBits(SIZEOFINT);
    }

    private byte[] createBuffer(byte[] data) {
        return new byte[SIZEOFINT + 8*data.length + (SIZEOFBYTE + SIZEOFINT)*BYTESCOUNT];
    }

    private void CalculateQuantities(byte[] data) {
		for(int i = 0; i < data.length; ++i)
		{
			int index = data[i] & 0xFF;
			if(quantities[index] == 0)
			{
				differentBytesCount += 1;
			}
			quantities[index] += 1;
		}
	}
	
	private void BuildTree()
	{		
		PriorityQueue<TreeNode> queue = new PriorityQueue<TreeNode>();

        createTreeNodesFromQuantities(queue);
        if(queue.size() == 1)
		{
            addFakeNode(queue);
		}
		while(queue.size() > 1)
		{
            unionLastTwoNodesInQueue(queue);
		}		
		root = queue.poll();		
	}

    private void unionLastTwoNodesInQueue(PriorityQueue<TreeNode> queue) {
        TreeNode first = queue.poll();
        TreeNode second = queue.poll();
        TreeNode union = new TreeNode(first, second);
        queue.add(union);
    }

    private void createTreeNodesFromQuantities(PriorityQueue<TreeNode> queue) {
        for(int i = 0; i < BYTESCOUNT; ++i)
        {
            if( quantities[i] > 0)
            {
                TreeNode node = new TreeNode((byte)i,quantities[i]);
                queue.add(node);
            }
        }
    }

    private void addFakeNode(PriorityQueue<TreeNode> queue) {
        TreeNode fake = new TreeNode((byte)(queue.peek().character + 1),queue.peek().quantity +1);
        queue.add(fake);
        differentBytesCount += 1;
    }

    private void Tour(TreeNode cur, int curCode, int curCodeLength)
	{
		if(cur.character != null)
		{
			byte b = cur.character.byteValue();
			int index = b & 0xFF;
			codes[index] = curCode;
			codeLengths[index] = curCodeLength;
		}
		if(cur.left != null)
		{
			Tour(cur.left, curCode<<1, curCodeLength + 1);
		}
		if(cur.right != null)
		{
			Tour(cur.right, (curCode<<1)|1, curCodeLength + 1);
		}
	}

	private void ReadQuantities()
	{
		int differentBytesCount = binaryIO.ReadBits(SIZEOFBYTE) + 1;
		for(int i = 0; i < differentBytesCount; ++i)
		{
			int index = binaryIO.ReadBits(SIZEOFBYTE);
			int quantity = binaryIO.ReadBits(SIZEOFINT);
			quantities[index] = quantity;
		}
	}
	
}
