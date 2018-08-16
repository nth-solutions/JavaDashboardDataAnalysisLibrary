import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

	public static void main(String[] args) { 
		
		String test = "COE"; //COE, COM, Pendulum, Spring, or Inclined Plane
		String location1 = "C:\\Users\\Silver\\Desktop\\cs.csv";//location of pendulum, spring, inclined plane, left cart COM, or Spin COE raw data
		String location2 = "C:\\Users\\Silver\\Desktop\\cd.csv";//location of right cart COM or Drop COE raw data
		double baseline = 1; //seconds
		int movingAverage = 10; //samples

		//For COE test only
		double time = 0;
		double spinMOI = 0.00008;
		double dropMass = 0.024;
		
		//For COM test only
		double leftMass = 0.236;
		double rightMass = 0.23;
		
		CSVReader reader = new CSVReader(location1); 
		CSVReader reader1 = new CSVReader(location2);
		List<List <Double>> data1 = reader.data();
		List<List <Double>> data2 = reader1.data();
		ArrayList<Integer> parameters = new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0,960,0,16,2000));
		String[] log = null;
		Calculator calc = null;
		switch(test)
		{
			case "COE":	 calc = new Calculator(data1, data2, parameters, baseline, movingAverage); log = calc.COE(time, spinMOI, dropMass); 
						break;
			case "COM":	 calc = new Calculator(data1, data2, parameters, baseline, movingAverage); log = calc.COM(leftMass, rightMass); 
						break;
			case "Pendulum":	calc = new Calculator(data1, parameters, baseline, movingAverage); log = calc.pendulum();
						break;
			case "Spring": calc = new Calculator(data1, parameters, baseline, movingAverage); log = calc.spring();
						break;
			case "Inclined Plane": calc = new Calculator(data1, parameters, baseline, movingAverage);log = calc.inclinedPlane();
						break;
		}
		for(String s: log)
		{
			System.out.println(s);
		}
	}

}
