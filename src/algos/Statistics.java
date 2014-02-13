package algos;
import java.io.*;
import java.util.*;
public class Statistics {
	private static Statistics _statistics = null;
	private static String _filename = "statistics.txt";
	public static Statistics GetInstance()
	{
		if(_statistics == null)
		{
			_statistics = new Statistics();
		}
		return _statistics;
	}
	
	
	TreeSet<ImageStatistics> _statisticsSet;
	
	private Statistics()
	{
		_statisticsSet = new TreeSet<ImageStatistics>();
		File f = new File(_filename);
		if(f.exists() && f.length() > 0)
		{
			ReadData();		
		}		
	}
		
	private void ReadData()
	{
		try
		{
			BufferedReader in = new BufferedReader( new FileReader( new File(_filename).getAbsoluteFile()));
			while(in.ready())
			{
				String nextItem = in.readLine();
				ImageStatistics imageStatistics = new ImageStatistics(nextItem);
				_statisticsSet.add(imageStatistics);
			}
			in.close();
		}
		catch(IOException e)
		{
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void AddItem(ImageStatistics imageStatistics)
	{
		_statisticsSet.add(imageStatistics);
	}
	public void WriteData()
	{
		try
		{
			PrintWriter out = new PrintWriter(new File(_filename).getAbsoluteFile());
			for(Iterator<ImageStatistics> it = _statisticsSet.iterator();it.hasNext();)
			{
				String tmp = it.next().Write();
				out.println(tmp);
			}
			out.flush();
			out.close();
		}
		catch(IOException e)
		{
			
		}
	}
}
