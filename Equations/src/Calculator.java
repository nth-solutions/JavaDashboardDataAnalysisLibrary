import java.util.ArrayList;
import java.util.List;

public class Calculator {
	ArrayList<ArrayList<Double>> data1= new ArrayList<ArrayList<Double>>();
	ArrayList<ArrayList<Double>> data2= new ArrayList<ArrayList<Double>>();
	private int sampleRate;
	private int baselineSamples;
	private int accelSensitivity;
	private int gyroSensitivity;
	private int magSensitivity = 4800;
	Calculator(List<List<Double>> data1,ArrayList<Integer> parameters, int baselineSamples)
	{
		this.data1 = (ArrayList) data1;
		this.baselineSamples = baselineSamples;
		sampleRate = parameters.get(7);
		accelSensitivity = parameters.get(9);
		gyroSensitivity = parameters.get(10);
		signData(this.data1);
		convertData(this.data1);
		normalize(this.data1);
	}
	Calculator(List<List<Double>> data1, List<List<Double>> data2,ArrayList<Integer> parameters, double baseline)
	{
		this.data1 = (ArrayList) data1;
		this.data2 = (ArrayList) data2;
		sampleRate = parameters.get(7);
		this.baselineSamples = (int)Math.floor(baseline*sampleRate);
		accelSensitivity = parameters.get(9);
		gyroSensitivity = parameters.get(10);
		signData(this.data1);
		convertData(this.data1);
		normalize(this.data1);
		signData(this.data1);
		convertData(this.data2);
		normalize(this.data2);
	}
	private void signData(ArrayList<ArrayList<Double>> data)
	{
		for(int i =0; i<data.size(); i++)
		{
			for(int j = 0; j<data.get(i).size(); j++)
			{
				if(data.get(i).get(j)>32768)
				{
					data.get(i).set(j, data.get(i).get(j)-65536);
				}
			}
		}
	}
	private void convertData(ArrayList<ArrayList<Double>> data)
	{
		for(int i =0; i<data.size(); i++)
		{
			for(int j = 0; j<data.get(i).size(); j++)
			{
				double sensitivity = magSensitivity;
				if(i<3)
				{
					sensitivity = accelSensitivity*9.81;
				}
				else if(i<6)
				{
					sensitivity = gyroSensitivity;
				}
				data.get(i).set(j, data.get(i).get(j)/32768*sensitivity);
			}
		}
	}
	private void normalize(ArrayList<ArrayList<Double>> data)
	{
		for(int i =0; i<data.size(); i++)
		{
			for(int j = 0; j<data.get(i).size(); j++)
			{
				data.get(i).set(j, data.get(i).get(j)-calculateBaseline(data)[i]);
			}
		}
	}
	private double[] calculateBaseline(ArrayList<ArrayList<Double>> data)
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
		retVal.add((double) 0);
		for(int i = 0; i<column.size()-1; i++)
		{
			
			retVal.add(retVal.get(i)+(column.get(i)+column.get(i+1))*0.5/(double)sampleRate);
			//System.out.println(retVal.get(i)+(column.get(i)+column.get(i+1))*0.5/(double)sampleRate);
		}
		return retVal;
	}
	public ArrayList<Double> absolute(ArrayList<Double> column)
	{
		ArrayList<Double> retVal = new ArrayList<Double>();
		for(int i = 0; i<column.size()-1; i++)
		{
			retVal.add(Math.abs(column.get(i)));
		}
		return retVal;
	}
	public double[] max(ArrayList<Double> column)
	{
		double max = column.get(0);
		double point = 0;
		for(int i = 0; i<column.size(); i++)
		{
			System.out.println(column.get(i));
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
			if (column.get(i)<min)
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
		ArrayList<Double> acceleration = data1.get(1);
		ArrayList<Double> velocity = integrate(acceleration);
		double opposite = 0;
		int oppositePoint = 0;
		int impactPoint = 0;
		if(max(acceleration)[0] > Math.abs(min(acceleration)[0]))
		{
			impactPoint = (int) max(acceleration)[1];
			opposite = Math.abs(min(subRange(acceleration,0,impactPoint))[0]);
			oppositePoint = (int)min(subRange(acceleration,0,impactPoint))[1];
			retVal[0] = "Acceleration Along Slope: " + Double.toString(min(subRange(velocity,0,impactPoint))[0]) + " m/s^2";
		}
		else
		{
			impactPoint = (int) min(acceleration)[1];
			opposite = Math.abs(max(subRange(acceleration,0,impactPoint))[0]);
			oppositePoint = (int)max(subRange(acceleration,0,impactPoint))[1];
			retVal[0] = "Acceleration Along Slope: " + Double.toString(max(subRange(velocity,0,impactPoint))[0]) + " m/s^2";
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
			retVal[0] = "Max Velocity: " + Double.toString(average(leftRange)) + " m/s";
		}
		else
		{
			retVal[0] = "Max Velocity: " + Double.toString(average(rightRange)) + " m/s";
		}
		return retVal;
	}
	public String[] pendulum()
	{
		String[] retVal = new String[2];
		ArrayList<Double> gyro = data1.get(5);
		double maxGyro = max(data1.get(5))[0];
		int firstMax = (int) max(data1.get(5))[1];
		retVal[0] = "Max Velocity: " + Double.toString(maxGyro*Math.PI/180) + "rad/s";
		int minAfterFirstMax = (int)min(subRange(gyro, firstMax, gyro.size()))[1]+firstMax;
		retVal[1] = "Period: " + Double.toString(2*(double)(minAfterFirstMax-firstMax)/(double)sampleRate) + " s";
		return retVal;
	}
	public String[] spring()
	{
		String[] retVal = new String[3];
		ArrayList<Double> acceleration = data1.get(1);
		ArrayList<Double> velocity = integrate(acceleration);
		int firstMax = 0;
		int secondMax = 0;
		for(int i = 0; i<localMaxima(velocity).size();i++)
		{
			if(localMaxima(velocity).get(i))
			{
				firstMax = i;
				break;
			}
		}
		for(int i = firstMax+1; i<localMaxima(velocity).size();i++)
		{
			if(localMaxima(velocity).get(i))
			{
				secondMax = i;
				break;
			}
		}
		retVal[0] = "Period: " + Double.toString((double)(secondMax-firstMax)/(double)sampleRate)+" s";
		retVal[1] = "Max Acceleration: " + Double.toString(max(acceleration)[0])+" m/s/s";
		//retVal[2] = "Max Velocity: " + Double.toString(max(velocity)[0])+" m/s";
		return retVal;
	}
	public ArrayList<Boolean> localMaxima (ArrayList<Double> column)
	{
		ArrayList<Boolean> maxima = new ArrayList<Boolean>();
		maxima.add(false);
		for(int i = 1; i<column.size()-1;i++)
		{
			if(column.get(i)>column.get(i-1)&&column.get(i)>column.get(i+1)&&column.get(i)>max(column)[0])
			{
				System.out.println(true);
				maxima.add(true);
			}
		}
		maxima.add(false);
		return maxima;
	}
	public String[] COM(double mass1, double mass2)
	{
		String[] retVal = new String[6];
		ArrayList<Double> acceleration1 = data1.get(1);
		ArrayList<Double> acceleration2 = data2.get(1);
		ArrayList<Double> velocity1 = integrate(acceleration1);
		ArrayList<Double> velocity2 = integrate(acceleration2);
		ArrayList<Double> accelproduct =  product(acceleration1, acceleration2);
		accelproduct = absolute(accelproduct);
		int collisionPoint = (int) max(accelproduct)[1];
		retVal[0] = "Cart 1 Momentum Before Collision: "+Double.toString(mass1*velocity1.get(collisionPoint-sampleRate/4))+" kgm/s";
		retVal[1] = "Cart 2 Momentum Before Collision: "+Double.toString(mass2*velocity2.get(collisionPoint-sampleRate/4))+" kgm/s";
		retVal[2] = "Cart 1 Momentum After Collision: "+Double.toString(mass1*velocity1.get(collisionPoint+sampleRate/4))+" kgm/s";
		retVal[3] = "Cart 2 Momentum After Collision: "+Double.toString(mass2*velocity2.get(collisionPoint+sampleRate/4))+" kgm/s";
		retVal[4] = "Total Momentum Before Collision: "+ evaluateString(retVal[0])+evaluateString(retVal[1])+" kgm/s";
		retVal[5] = "Total Momentum After Collision: "+ evaluateString(retVal[2])+evaluateString(retVal[3])+" kgm/s";
		return retVal;
	}
	public String[] COE(double time, double spinMoi, double dropMass)
	{
		String[] retVal = new String[12];
		ArrayList<Double> spin = data1.get(5);
		ArrayList<Double> dropAcceleration = data2.get(1);
		ArrayList<Double> dropVelocity = integrate(dropAcceleration);
		ArrayList<Double> dropDistance = integrate(dropVelocity);
		int point = 0;
		int timePoint = (int) Math.floor(time*sampleRate);
		if(max(dropAcceleration)[0]>Math.abs(min(dropAcceleration)[0]))
		{
			point = (int)max(dropAcceleration)[1];
			retVal[2] = "Total Drop Distance: " + Math.abs(max(subRange(dropDistance,0,point))[0])+" m";
			retVal[3] = "Energy at the top: " + dropMass*9.81*Math.abs(max(subRange(dropDistance,0,point))[0])+" J";
			retVal[10] = "Potential Energy(point): " + dropMass*9.81*(Math.abs(max(subRange(dropDistance,0,point))[0])-dropDistance.get(timePoint))+" J";
		}
		else
		{
			point = (int)min(dropAcceleration)[1];
			retVal[2] = "Total Drop Distance: " + Math.abs(min(subRange(dropDistance,0,point))[0])+" m";
			retVal[3] = "Energy at the top: " + dropMass*9.81*Math.abs(min(subRange(dropDistance,0,point))[0])+" J";
			retVal[10] = "Potential Energy(point): " + dropMass*9.81*(Math.abs(min(subRange(dropDistance,0,point))[0])-Math.abs(dropDistance.get(timePoint)))+" J";
		}
		if(max(spin)[0]>Math.abs(min(spin)[0]))
		{
			retVal[0] = "Maximum Angular Velocity: "+max(spin)[0]*Math.PI/180+" rad/s";
			retVal[4] = "Energy at the bottom: " + 0.5*spinMoi*(max(spin)[0]*Math.PI/180)*(max(spin)[0]*Math.PI/180)+" J";
		}
		else
		{
			retVal[0] = "Maximum Angular Velocity: "+Math.abs(min(spin)[0])*Math.PI/180+" rad/s";
			retVal[4] = "Energy at the bottom: " + 0.5*spinMoi*(Math.abs(min(spin)[0])*Math.PI/180)*(Math.abs(min(spin)[0])*Math.PI/180)+" J";
		}
		retVal[1] = "Linear Acceleration: " + Math.abs(dropAcceleration.get(point-sampleRate/10))+" m/s/s";
		retVal[5] = "Angular Velocity(point): "+Math.abs(spin.get(timePoint)*Math.PI/180)+" rad/s";
		retVal[6] = "Linear Velocity(point): " + Math.abs(dropVelocity.get(timePoint))+" m/s";
		retVal[7] = "Drop Distance(point): " + Math.abs(integrate(dropVelocity).get(timePoint));
		retVal[8] = "Rotational Kinetic Energy(point): " + 0.5*spinMoi*Math.abs(spin.get(timePoint)*Math.PI/180)*Math.abs(spin.get(timePoint)*Math.PI/180)+" J";
		retVal[9] = "Linear Kinetic Energy(point): " + 0.5*dropMass*Math.abs(dropVelocity.get(timePoint))*Math.abs(dropVelocity.get(timePoint))+" J";
		retVal[11]  = "Total Energy(point): " + evaluateString(retVal[8])+evaluateString(retVal[9])+evaluateString(retVal[10])+" J";
		return retVal;
	}
	public double evaluateString(String s)
	{
		return Double.parseDouble(s.split(" ")[s.split(" ").length-2]);
	}
}
