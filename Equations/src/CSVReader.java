import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;;
public class CSVReader {
	String csvFile;
    BufferedReader br = null;
    String line = "";
    String csvSplitBy = ",";
    CSVReader(String path)
    {
    	csvFile = path;
    }
	public List<List<Double>> data()
	{
		List<List<Double>> data = new ArrayList<List<Double>>();
		try {

            br = new BufferedReader(new FileReader(csvFile));
            for(int i = 0 ; i<6; i++)
            {
            	data.add(new ArrayList<Double>());
            }
            while ((line = br.readLine()) != null) {
                String[] val = line.split(csvSplitBy);
                for(int i  = 0; i<6; i++)
                {
                	data.get(i).add(Double.parseDouble(val[i]));
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return data;
	}
}
