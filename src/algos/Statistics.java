package algos;

import java.io.*;
import java.util.Iterator;
import java.util.TreeSet;

public class Statistics {
    private static Statistics statistics = null;
    private static String filename = "statistics.txt";
    TreeSet<ImageStatistics> statisticsSet;


    private Statistics() {
        statisticsSet = new TreeSet<ImageStatistics>();
        File f = new File(filename);
        if (f.exists() && f.length() > 0) {
            ReadData();
        }
    }

    private void ReadData() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(new File(filename).getAbsoluteFile()));
            while (in.ready()) {
                String nextItem = in.readLine();
                ImageStatistics imageStatistics = new ImageStatistics(nextItem);
                statisticsSet.add(imageStatistics);
            }
            in.close();
        } catch (IOException e) {

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Statistics GetInstance() {
        if (statistics == null) {
            statistics = new Statistics();
        }
        return statistics;
    }

    public void AddItem(ImageStatistics imageStatistics) {
        statisticsSet.add(imageStatistics);
    }

    public void WriteData() {
        try {
            PrintWriter out = new PrintWriter(new File(filename).getAbsoluteFile());
            for (Iterator<ImageStatistics> it = statisticsSet.iterator(); it.hasNext(); ) {
                String tmp = it.next().Write();
                out.println(tmp);
            }
            out.flush();
            out.close();
        } catch (IOException e) {

        }
    }
}
