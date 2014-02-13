package algos;
import java.util.*;
public class LZW implements ICompressor {

	
	private final short CLEAR_CODE = 257;
	private final short EMPTY_CODE = 258;
	private final short END_CODE = 259;
	private final short POOL_START = END_CODE  + 1;
	private final int MAXCODELENGTH = 12;
	private final int MAGICNUMBER = (2<<(MAXCODELENGTH)) - 1;
	private final int TABLECAPACITY = 2<<13 +1;
	private final int STARTBITSTOWRITE = 9;
	private final int[] THRESHOLDS = {511,1023,2047,4095,8191};
	private final int CLEARTABLESIZE = 4095;
	private byte[] buffer;	
	private class ListOfInts
	{
		public LinkedList<Integer> list;
		public ListOfInts()
		{
			list = new LinkedList<Integer>();
		}
	}
	private class ListOfBytes
	{
		public LinkedList<Byte> list;
		public ListOfBytes()
		{
			list = new LinkedList<Byte>();
		}
	}
	@Override
	public byte[] Compress(byte[] data) {
		
		buffer = new byte[2*(data.length + 2)];
		BinaryIO writer = new BinaryIO(buffer);
		
		int nextCode = POOL_START; // в этой переменной будет храниться код, который надо выдать следующей цепочке
		int bitsInCode = STARTBITSTOWRITE; // а в этой переменной будет храниться текущее количество бит, которое занимет код
		
		ListOfInts[] table = new ListOfInts[TABLECAPACITY];		
		InitHashTable(table);
		writer.WriteBits(CLEAR_CODE, bitsInCode);
		int prevCode = EMPTY_CODE;
		for(int i=0;i<data.length; ++i)
		{
			int cur = (int)data[i] & 0xFF ;			
			int key = MakeKey(prevCode, cur);
			int hash = GetHash(key);
			
			boolean contains = false;
			int valueCode = 0, valuePrevCode;
			int valueLastSymbol;
			
			
			for(int j=0;j<table[hash].list.size(); ++j)
			{
				Integer value = table[hash].list.get(j);
				valueCode = GetCode(value);
				valuePrevCode = GetPrevCode(value);
				valueLastSymbol = GetLastSymbol(value);
				
				if(prevCode == valuePrevCode && cur == valueLastSymbol)
				{
					contains = true;
					break;
				}				
			}
			
			if(contains)
			{
				prevCode = valueCode;
			}
			else
			{
				int toWrite = prevCode;//((byte) (prevCode))&0xFF;
				writer.WriteBits(toWrite, bitsInCode);		
				int newElement = GetNewElement(nextCode,prevCode,cur);
				table[hash].list.add(newElement);
				
				int prevKey = MakeKey(EMPTY_CODE,cur);
				int prevHash = GetHash(prevKey);
				prevCode = EMPTY_CODE;
				for(int j=0;j<table[prevHash].list.size(); ++j)
				{
					Integer value = table[prevHash].list.get(j);
					valueCode = GetCode(value);
					valuePrevCode = GetPrevCode(value);
					valueLastSymbol = GetLastSymbol(value);
					
					if(prevCode == valuePrevCode && cur == valueLastSymbol)
					{
						prevCode = valueCode;
						break;
					}				
				}
				
				++nextCode;
				if(Arrays.binarySearch(THRESHOLDS, nextCode) >= 0)
				{
					++bitsInCode;					
				}
				if(nextCode == CLEARTABLESIZE)
				{
					writer.WriteBits(prevCode, bitsInCode);
					writer.WriteBits(CLEAR_CODE, bitsInCode);
					InitHashTable(table);
					nextCode = POOL_START;
					bitsInCode = STARTBITSTOWRITE;
					prevCode = EMPTY_CODE;
				}
			}
		}
		int toWrite = prevCode;//((byte) (prevCode))&0xFF;
		writer.WriteBits(toWrite, bitsInCode);
		writer.WriteBits(END_CODE, bitsInCode);
		writer.Flush();
		return Arrays.copyOf(buffer, writer.GetTotalBytesProceeded());
	}
	
	void InitHashTable( ListOfInts[] table)
	{
		for(int i=0; i < TABLECAPACITY; ++i)
		{
			table[i] = new ListOfInts();
		}
		for(int i=0;i<256; ++i)
		{
			int key = MakeKey(EMPTY_CODE, i);
			int hash = GetHash(key);
			int newElement = GetNewElement( i, EMPTY_CODE, i);
			table[hash].list.add(newElement);			
		}
	}
	int GetNewElement(int curCode, int prevCode, int cur)
	{
		return (((curCode << 12) | prevCode) << 8) | (cur & 0xFF);
	}
	int GetCode(int value)
	{
		// код текущей цепочки
		return value>>>20;
	}
	int GetPrevCode(int value)
	{
		return (value<<12)>>>20;
	}
	int GetLastSymbol(int value)
	{
		
		return  (value & 0xFF);
	}
	int MakeKey(int prevKey, int lastByte)
	{
		int result = (prevKey<<8) | (lastByte & 0xFF); 
		return result;
	}
	int GetHash(int key)
	{
		int hash = ((( key ) >> MAXCODELENGTH ) ^ key) & (MAGICNUMBER); 
		return hash;
	}
	void InitCodeArray(ListOfBytes[] table)
	{
		for(int i=0;i<table.length; ++i)
		{
			table[i] = null;
		}
		table[CLEAR_CODE] = new ListOfBytes();
		for(int i=0;i<256;++i)
		{
			table[i] = new ListOfBytes();
			table[i].list.add((byte)(i&0xFF));
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public byte[] Decompress(byte[] data){
		
		buffer = new byte[(2+data.length) * 2];
		int bytesCount = 0;
		int bitsInCode = STARTBITSTOWRITE;
		int nextCode = POOL_START;
		BinaryIO reader = new BinaryIO(data);
		ListOfBytes[] table = new ListOfBytes[TABLECAPACITY];
		InitCodeArray(table);
		
		int code = reader.ReadBits(bitsInCode);
		int oldCode = EMPTY_CODE;
		while ( code != END_CODE)
		{
			if( code == CLEAR_CODE)
			{
				InitCodeArray(table);
				nextCode = POOL_START;
				bitsInCode = STARTBITSTOWRITE;
				code = reader.ReadBits(bitsInCode);
				code = ((byte) code) & 0xFF;				 
				if( code == END_CODE)
				{
					return Arrays.copyOf(buffer,bytesCount);
				}
				bytesCount += AddData(bytesCount, code, table);
				oldCode = code;
			}
			else
			{
				if(table[code] != null)
				{
					int bytesWritten = AddData(bytesCount, code, table);
					bytesCount += bytesWritten;
					table[nextCode] = new ListOfBytes();
					table[nextCode].list = (LinkedList<Byte>) table[oldCode].list.clone();
					table[nextCode].list.add(table[code].list.get(0));
					nextCode += 1;
					if(Arrays.binarySearch(THRESHOLDS, nextCode+1) >= 0)
					{
						++bitsInCode;					
					}
					oldCode = code;
				}
				else
				{
					LinkedList<Byte> llb = (LinkedList<Byte>) table[oldCode].list.clone();
					llb.add(llb.get(0));
					table[nextCode] = new ListOfBytes();
					table[nextCode].list = llb;
					bytesCount += AddData(bytesCount, nextCode, table);
					oldCode = nextCode;
					nextCode += 1;
					if(Arrays.binarySearch(THRESHOLDS, nextCode+1) >= 0)
					{
						++bitsInCode;
					}
				}
			}
			code = reader.ReadBits(bitsInCode);
		}
		
		return Arrays.copyOf(buffer,bytesCount);
	}
	
	public int AddData(int start, int code, ListOfBytes[] table)
	{
		
		ListOfBytes elem = table[code];
		int size = elem.list.size();
		if(start + size >= buffer.length)
		{
			int c = (start + size)/buffer.length + 1;
			byte[] newBuf = new byte[buffer.length*c];
			System.arraycopy(buffer, 0, newBuf, 0, buffer.length);
			buffer = newBuf;
		}
		for(int i = 0; i< size; ++i)
		{
			buffer[start + i] = elem.list.get(i);
		}
		return size;
	}

}
