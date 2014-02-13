package algos;
public class BinaryIO
	{
		private final int SIZEOFBYTE = 8;
		private final int SIZEOFINT = 32;
		private final int MASK = 0xFF;
		private byte[] data;
		private int dataPointer;
		int bytesProceeded;
		int buffer;
		int bitsInBuffer;
		public BinaryIO(byte[] dest)
		{
			data = dest;
			dataPointer = 0;
			bytesProceeded = 0;
			bitsInBuffer = 0;
			buffer = 0;
		}
		public void WriteBits(int value, int bitsCount)
		{
			//System.out.println("Output: "+value);
			if(SIZEOFINT - bitsInBuffer > bitsCount)
			{
				buffer <<= bitsCount;
				buffer |= value;
				bitsInBuffer += bitsCount;
			}
			else
			{
				int shift = SIZEOFINT - bitsInBuffer;
				if(shift == SIZEOFINT)
				{
					buffer = 0;
				}
				else
				{
					buffer <<= shift;
				}
				int copyOfvalue = value;
				value >>>= (bitsCount - shift);
				buffer |= value;
				
				byte[] tmp = new byte[4];
				tmp[0] = (byte)(((buffer << 24) >>> 24) & MASK); // >>> гарантировано заполняет слева нулями, >> зависит от знака
				tmp[1] = (byte)(((buffer << 16) >>> 24) & MASK);
				tmp[2] = (byte)(((buffer << 8)  >>> 24) & MASK);
				tmp[3] = (byte)(((buffer << 0)  >>> 24) & MASK);
				
				for(int i=3; i>=0; --i, dataPointer++)
				{
					data[dataPointer] = tmp[i];
				}
				
				value = copyOfvalue;
				buffer = value << (SIZEOFINT - bitsCount + shift);
				bitsInBuffer = bitsCount - shift;
				buffer >>>= (SIZEOFINT - bitsInBuffer);
				bytesProceeded += 4;
			}	
			
				
					
		}
		public void Flush()
		{
			if(bitsInBuffer == 0)
				return;
			buffer <<= (SIZEOFINT - bitsInBuffer);
			int bytesCount = (bitsInBuffer + SIZEOFBYTE - 1) / SIZEOFBYTE;
			byte[] tmp = new byte[4];
			tmp[0] = (byte)(((buffer << 24) >>> 24) & MASK); // >>> гарантировано заполняет слева нулями, >> зависит от знака
			tmp[1] = (byte)(((buffer << 16) >>> 24) & MASK);
			tmp[2] = (byte)(((buffer << 8)  >>> 24) & MASK);
			tmp[3] = (byte)(((buffer << 0)  >>> 24) & MASK);
			
			for(int i=3; i>= 4 - bytesCount; --i, dataPointer++)
			{
				data[dataPointer] = tmp[i];
			}
			bytesProceeded += bytesCount;
		}
		public int ReadBits(int bitsCount)
		{
			if(bitsCount > SIZEOFINT)
			{
				//				
			}
			int tmp = 0;
			while(bitsInBuffer <= SIZEOFINT - SIZEOFBYTE && dataPointer < data.length)
			{
				
				tmp = 0;
				tmp |= (data[dataPointer]& MASK);
				dataPointer += 1;
				buffer <<= SIZEOFBYTE;
				buffer |= tmp;
				bitsInBuffer += SIZEOFBYTE;
			}
			int shift = SIZEOFINT - bitsInBuffer;
			
			int copy = buffer;
			int result = ((copy << shift) >>> (SIZEOFINT - bitsCount));
			buffer <<= (bitsCount + shift);
			bitsInBuffer -= bitsCount;
			buffer >>>= (bitsCount + shift);
			return result;
		}
		public boolean CanMove()
		{
			return dataPointer < data.length;
		}
		
		public int GetTotalBytesProceeded()
		{
			return bytesProceeded; 
		}
		
		public int GetBitsInBuffer()
		{
			return bitsInBuffer;
		}
	}