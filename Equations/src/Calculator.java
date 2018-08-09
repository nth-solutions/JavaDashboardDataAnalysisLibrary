import java.util.ArrayList;
import java.util.List;

public class Calculator {
	ArrayList<ArrayList<Double>> data= new ArrayList<ArrayList<Double>>();
	private int sampleRate;
	private int baselineSamples;
	private int accelSensitivity;
	private int gyroSensitivity;
	private int magSensitivity = 4800;
	Calculator(List<List<Double>> data,ArrayList<Integer> parameters, int baselineSamples)
	{
		this.data = (ArrayList) data;
		this.baselineSamples = baselineSamples;
		sampleRate = parameters.get(7);
		accelSensitivity = parameters.get(9);
		gyroSensitivity = parameters.get(10);
		convertData();
	}
	private void convertData()
	{
		for(int i =0; i<data.size(); i++)
		{
			for(int j = 0; j<data.get(i).size(); j++)
			{
				int sensitivity = magSensitivity;
				if(i<3)
				{
					sensitivity = accelSensitivity;
				}
				else if(i<6)
				{
					sensitivity = gyroSensitivity;
				}
				data.get(i).set(j, data.get(i).get(j)/32768*sensitivity);
			}
		}
	}
	private void normalize()
	{
		for(int i =0; i<data.size(); i++)
		{
			for(int j = 0; j<data.get(i).size(); j++)
			{
				data.get(i).set(j, data.get(i).get(j)-calculateBaseline()[i]);
			}
		}
	}
	private double[] calculateBaseline()
	{
		double[] baselines = new double[9];
		for(int i = 0; i< data.size(); i++)
		{
			for(int j= 0; j<baselineSamples; j++)
			{
				baselines[i] += data.get(i).get(j);
			}
		}
		return baselines;
	}
	public ArrayList<Double> integrate(ArrayList<Double> column)
	{
		ArrayList<Double> retVal = new ArrayList<Double>();
		for(int i = 0; i<column.size()-1; i++)
		{
			retVal.add((column.get(i)+column.get(i+1))/2/sampleRate);
		}
		return retVal;
	}
	public double[] max(ArrayList<Double> column)
	{
		double max = column.get(0);
		double point = 0;
		for(int i = 0; i<column.size(); i++)
		{
			if (column.get(i)>max)
			{
			max = column.get(i);
			point = i;
			}
		}
		double[] retVal = new double[2];
		retVal[0] = max;
		retVal[1] = point;
		return retVal;
	}
	public double[] min(ArrayList<Double> column)
	{
		double min = column.get(0);
		double point = 0;
		for(int i = 0; i<column.size(); i++)
		{
			if (column.get(i)>min)
			{
			min = column.get(i);
			point = i;
			}
		}
		double[] retVal = new double[2];
		retVal[0] = min;
		retVal[1] = point;
		return retVal;
	}
	public ArrayList<Double> product(ArrayList<Double> column1, ArrayList<Double> column2)
	{
		ArrayList<Double> retVal = new ArrayList<Double>();
		for(int i = 0; i<column1.size(); i++)
		{
			retVal.add(column1.get(i)*column2.get(i));
		}
		return retVal;
	}
	public double average(ArrayList<Double> range)
	{
		double sum = 0;
		for(int i = 0; i<range.size(); i++)
		{
			sum+= range.get(i);
		}
		return sum/range.size();
	}
	public ArrayList<Double> movingAverage(ArrayList<Double> column, int length)
	{
		ArrayList<Double> retVal = new ArrayList<Double>();
		
		for(int i = 0; i<column.size()-length; i++)
		{
			ArrayList<Double> range = new ArrayList<Double>();
			for(int j =0; j<length; j++)
			{
				range.add(column.get(i+j));
			}
			retVal.add(average(range));
		}
		for(int i =0; i<length; i++)
		{
			retVal.add(column.get(column.size()-length+i));
		}
		return retVal;
	}
	public ArrayList<Double> subRange(ArrayList<Double> a, int start, int end )
	{
		ArrayList<Double> retVal = new ArrayList<Double>();
		for(int i = start; i< end; i++)
		{
			retVal.add(a.get(i));
		}
		return retVal;
	}
	public String[] inclinedPlane()
	{
		String[] retVal = new String[2];
		ArrayList<Double> acceleration = data.get(1);
		ArrayList<Double> velocity = integrate(acceleration);
		double opposite = 0;
		int oppositePoint = 0;
		int impactPoint = 0;
		if(max(acceleration)[0] > Math.abs(min(acceleration)[0]))
		{
			impactPoint = (int) max(acceleration)[1];
			opposite = Math.abs(min(subRange(acceleration,0,impactPoint))[0]);
			oppositePoint = (int)min(subRange(acceleration,0,impactPoint))[1];
			retVal[0] = "Acceleration Along Slope: " + Double.toString(min(subRange(velocity,0,impactPoint))[0]) + "m/s^2";
		}
		else
		{
			impactPoint = (int) min(acceleration)[1];
			opposite = Math.abs(max(subRange(acceleration,0,impactPoint))[0]);
			oppositePoint = (int)max(subRange(acceleration,0,impactPoint))[1];
			retVal[0] = "Acceleration Along Slope: " + Double.toString(max(subRange(velocity,0,impactPoint))[0]) + "m/s^2";
		}
		ArrayList<Double> leftRange = new ArrayList<Double>();
		ArrayList<Double> rightRange = new ArrayList<Double>();
		for(int i = 0; i < sampleRate/4; i++)
		{
			leftRange.add(acceleration.get(oppositePoint - sampleRate/2+ i));
		}
		for(int i = 0; i < sampleRate/4; i++)
		{
			rightRange.add(acceleration.get(oppositePoint + sampleRate/4+ i));
		}
		if(Math.abs(average(leftRange)-opposite) < Math.abs(average(rightRange)-opposite))
		{
			retVal[0] = "Max Velocity: " + Double.toString(average(leftRange)) + "m/s";
		}
		else
		{
			retVal[0] = "Max Velocity: " + Double.toString(average(rightRange)) + "m/s";
		}
		return retVal;
	}
	public String[] pendulum()
	{
		String[] retVal = new String[2];
		ArrayList<Double> gyro = data.get(5);
		double maxGyro = max(data.get(5))[0];
		int firstMax = (int) max(data.get(5))[0];
		retVal[0] = "Max Velocity: " + Double.toString(maxGyro*Math.PI/180) + "rad/s";
		int minAfterFirstMax = (int)min(subRange(gyro, firstMax, gyro.size()))[1];
		retVal[1] = "Period: " + Double.toString(2*(minAfterFirstMax-firstMax)/sampleRate) + "s";
		return retVal;
	}
}
