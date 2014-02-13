package algos;
import java.util.*;
public class RunLengthEncoder implements ICompressor {

	private final int maxBytesInBlock = 127;
	@Override
	public byte[] Compress(byte[] data) {
		// Compresses byte array using rle algorithm
		int length = data.length;
		byte[] buffer = new byte[2*length];
		int current = 0;
		for(int i = 0; i < length; ++i)
		{
			byte curbyte = data[i];
			int count = 1;
			int start = i;
			if(i == length - 1)
			{
				buffer[current] = (byte)count;
				++current;
				buffer[current] = curbyte;
				++current;
				break;
			} 
			else 
			{
				if( data[i] == data[i+1])
				{
					while( (count < maxBytesInBlock) && ((i + 1) < length) && (data[i] == data[i+1]))
					{
						count++;
						i++;
					}
					byte firstByte = ((byte)count);				
					buffer[current] = (byte) firstByte;
					++current;
					buffer[current] = curbyte;
					++current;
				}
				else
				{
					// curbyte != data[i+1]
					while( (count < maxBytesInBlock) && (i + 1 < length) && (data[i] != data[i+1]))
					{
						count++;
						i++;
					}					
					if((i + 1 < length) && (data[i] == data[i+1]))
					{
						count--;
						i--;
					}
					
					
					byte firstByte = ((byte)-count);
					buffer[current] = (byte) firstByte;
					++current;
					for (int j = 0; j < count; ++j)
					{						
						buffer[current] = data[start + j];
						++current;
					}
				}
			}		
		}
		int resultSize = current;
		byte[] resultArray = Arrays.copyOfRange(buffer, 0, resultSize);
		return resultArray;
	}

	@Override
	public byte[] Decompress(byte[] data) {
		int requiredSize = 0;
		int cur = 0;
		while(cur < data.length)
		{
			if(data[cur] > 0)
			{
				requiredSize += data[cur];
				cur += 2;
			}
			else
			{
				requiredSize += (-data[cur]);
				cur += (-data[cur]) + 1;
			}
		}
		byte[] decompressed = new byte[requiredSize];
		int current = 0;
		int dataLength = data.length;
		for(int i=0;i<dataLength;++i)
		{
			if(data[i] < 0)
			{
				int count = -data[i];
				++i;				
				for(int j=0;j<count;++j,++i)
				{
					decompressed[current] = data[i];
					++current;
				}				
				--i;
			}
			else
			{
				int count = data[i];
				++i;
				for(int j=0;j<count;++j)
				{
					decompressed[current] = data[i];
					++current;
				}
			}
		}
		return decompressed;
	}
	
	public boolean IsCounter(byte data)
	{
		return data < 0;
	}

}
