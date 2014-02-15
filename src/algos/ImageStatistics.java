package algos;
public class ImageStatistics implements Comparable<ImageStatistics> {
	private String _name;
	private int _hashCode;
	private long  _originalSize;
	private long  _compressedSize;
	private long  _workTime;
	private int  _imageType;
	private boolean _isCompressionStatistics;
	public ImageStatistics()
	{
		
	}
	public ImageStatistics(String name, long originalSize, long compressedSize, int imageType, long time, boolean compressionStatistics)
	{
		_name = name;
		_originalSize = originalSize;
		_compressedSize = compressedSize;
		_imageType = imageType;
		_isCompressionStatistics = compressionStatistics;
		_workTime = time;
	}
	public String GetName()
	{
		return _name;
	}
	public long GetOriginalSize()
	{
		return _originalSize;
	}
	public long GetCompressedSize()
	{
		return _compressedSize;
	}
	public float GetCompressionCoefficient()
	{
		return 1.0f*_originalSize/_compressedSize;
	}
	public long GetWorkTime()
	{
		return _workTime;
	}
	public int GetImageType()
	{
		return _imageType;
	}
	public boolean IsCompressionStatistics()
	{
		return _isCompressionStatistics;
	}
	public float GetSecPerByte()
	{
		if(_isCompressionStatistics)
		{
			return 1.0f*GetWorkTime()/GetOriginalSize();
		}
		else
		{
			return 1.0f*GetWorkTime()/GetCompressedSize();
		}
	}
	public void SetName(String name)
	{
		_name = name;
	}
	public void SetOriginalSize(long size)
	{
		_originalSize = size;
	}
	public void SetCompressedSize(long size)
	{
		_compressedSize = size;
	}
	public void SetWorkTime(long workTime)
	{
		_workTime = workTime;
	}
	public void SetIsCompressionStatistics(boolean isCompressionStatistics)
	{
		_isCompressionStatistics = isCompressionStatistics;
	}
	public void SetImageType(int type)
	{
		_imageType = type;
	}
	public String Write()
	{
        StringBuilder result = new StringBuilder();

		result.append(GetName());
        result.append(" ");
        result.append(GetOriginalSize());
        result.append(" ");
        result.append(GetCompressedSize());
        result.append(" ");
        result.append(GetWorkTime());
        result.append(" ");
        result.append(GetImageType());
        result.append(" ");
        result.append(IsCompressionStatistics());

		return result.toString();
	}
	public ImageStatistics(String string) throws Exception
	{
		String[] items = string.split(" ");
		if(items.length != 6)
			throw new Exception("Wrong size");
		SetName(items[0]);
		SetOriginalSize(new Long(items[1]));
		SetCompressedSize(new Long(items[2]));
		SetWorkTime(new Long(items[3]));
		SetImageType(new Integer(items[4]));
		SetIsCompressionStatistics(new Boolean(items[5]));
	}
	public int GetHashCode()
	{
		return _hashCode;
	}
	public void SetHashCode(int hash)
	{
		_hashCode = hash;
	}
	@Override
	public int compareTo(ImageStatistics arg0)
	{
		Integer hash1 = new Integer(GetHashCode());
		Integer hash2 = new Integer(arg0.GetHashCode());
		if(this.IsCompressionStatistics() == arg0.IsCompressionStatistics())
			return hash1.compareTo(hash2);
		else
		{
			if(this.IsCompressionStatistics())
			{
				return -1;
			}
			return 1;
		}
		
		
	}
}
