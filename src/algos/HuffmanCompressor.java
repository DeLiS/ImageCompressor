package algos;

import java.util.Arrays;
import java.util.PriorityQueue;
public class HuffmanCompressor implements ICompressor {

	
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
	
	private final int BYTESCOUNT = 256;
	private final int SIZEOFBYTE = 8;
	private final int SIZEOFINT = 32;
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
		byte[] buffer = new byte[SIZEOFINT + data.length + (SIZEOFBYTE + SIZEOFINT)*BYTESCOUNT];
		binaryIO = new BinaryIO(buffer);
		binaryIO.WriteBits(0, SIZEOFINT);//занимаем место
		CalculateQuantities(data);
		BuildTree();
		Tour(root,0,0);
		WriteQuantities();
		for(int i = 0; i < data.length; ++i)
		{
			int index = data[i] & 0xFF;
			binaryIO.WriteBits(codes[index], codeLengths[index]);
			bitsCount += codeLengths[index];
		}
		binaryIO.Flush();
		int totalLength = binaryIO.GetTotalBytesProceeded();
		binaryIO = new BinaryIO(buffer);
		binaryIO.WriteBits(bitsCount,SIZEOFINT);// пишем в занятое место
		binaryIO.Flush();
		
		return Arrays.copyOf(buffer, totalLength);
	}

	@Override
	public byte[] Decompress(byte[] data) {
		
		byte[] buffer = new byte[SIZEOFINT + 8*data.length + (SIZEOFBYTE + SIZEOFINT)*BYTESCOUNT];
		binaryIO = new BinaryIO(data);
		int bufferPointer = 0;
		bitsCount = binaryIO.ReadBits(SIZEOFINT);
		ReadQuantities();
		BuildTree();
		TreeNode cur = root;
		while(bitsCount > 0)
		{
			bitsCount -= 1;
			int bit = binaryIO.ReadBits(1);
			if(bit == 0)
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
		return Arrays.copyOf(buffer, bufferPointer);
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
		
		for(int i = 0; i < BYTESCOUNT; ++i)
		{
			if( quantities[i] > 0)
			{
				TreeNode node = new TreeNode((byte)i,quantities[i]);
				queue.add(node);
			}
		}
		if(queue.size() == 1)
		{
			TreeNode fake = new TreeNode((byte)(queue.peek().character + 1),queue.peek().quantity +1);
			queue.add(fake);
			differentBytesCount += 1;
		}
		while(queue.size() > 1)
		{
			TreeNode first = queue.poll();
			TreeNode second = queue.poll();
			TreeNode union = new TreeNode(first, second);
			queue.add(union);
		}		
		root = queue.poll();		
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
	private void WriteQuantities()
	{
		// не может быть больше, чем 256 различных байт 
		//(храним на 1 меньше, т.к. 0 различных байт не имеет смысла, 0 - значит 1)
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
