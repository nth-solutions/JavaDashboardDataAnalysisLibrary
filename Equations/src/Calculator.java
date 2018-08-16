import java.util.ArrayList;
import java.util.List;

public class Calculator {
	ArrayList<ArrayList<Double>> data1= new ArrayList<ArrayList<Double>>();
	ArrayList<ArrayList<Double>> data2= new ArrayList<ArrayList<Double>>();
	private int sampleRate;
	private int baselineSamples;
	private int filter;
	private int accelSensitivity;
	private int gyroSensitivity;
	private double[] baselines = new double[9];
	private int magSensitivity = 4800;
	//Create this instance for pendulum, spring, and inclined plane tests(one module tests)
	//requires the data, parameters in the form that is generated by the dash-board, a baseline length in seconds(try 1 second), and moving average length in samples(try 10 or 100)
	Calculator(List<List<Double>> data1,ArrayList<Integer> parameters, double baseline, int movingAverage)
	{
		this.data1 = (ArrayList) data1;
		this.baselineSamples = (int)Math.floor(baseline*sampleRate);
		sampleRate = parameters.get(7);
		accelSensitivity = parameters.get(9);
		gyroSensitivity = parameters.get(10);
		filter = movingAverage;
		//takes time to convert from raw data to to usable data in m/s/s, dps, and mT
		signData(this.data1);
		convertData(this.data1);
		calculateBaseline(this.data1);
		normalize(this.data1);
		smooth(this.data1);
	}
	//Create this instance for Conservation of Energy and Conservation of Momentum Tests(two module tests)
	//requires the two sets of data, parameters in the form that is generated by the dash-board, a baseline length in seconds(try 1 second), and moving average length in samples(try 10 or 100)
	Calculator(List<List<Double>> data1, List<List<Double>> data2,ArrayList<Integer> parameters, double baseline, int movingAverage)
	{
		this.data1 = (ArrayList) data1;
		this.data2 = (ArrayList) data2;
		sampleRate = parameters.get(7);
		this.baselineSamples = (int)Math.floor(baseline*sampleRate);
		filter = movingAverage;
		accelSensitivity = parameters.get(9);
		gyroSensitivity = parameters.get(10);
		//takes time to convert both data samples from raw data to to usable data in m/s/s, dps, and mT
		signData(this.data1);
		convertData(this.data1);
		calculateBaseline(this.data1);
		normalize(this.data1);
		smooth(this.data1);
		signData(this.data2);
		convertData(this.data2);
		calculateBaseline(this.data2);
		normalize(this.data2);
		smooth(this.data2);
	}
	//The following method adds a signed value to the data; changes range from 0-65535 to -32767 to 32767
	private void signData(ArrayList<ArrayList<Double>> data)
	{
		for(int i =0; i<data.size(); i++)
		{
			for(int j = 0; j<data.get(i).size(); j++)
			{
				//if value is over 32768, flips it onto the negative side
				if(data.get(i).get(j)>32768)
				{
					data.get(i).set(j, data.get(i).get(j)-65536);
				}
			}
		}
	}
	//The following method converts from signed data to physics units: m/s/s, dps, mT
	private void convertData(ArrayList<ArrayList<Double>> data)
	{
		for(int i =0; i<data.size(); i++)
		{
			for(int j = 0; j<data.get(i).size(); j++)
			{
				double sensitivity = magSensitivity;
				if(i<3)
				{
					sensitivity = accelSensitivity*9.807;
				}
				else if(i<6)
				{
					sensitivity = gyroSensitivity;
				}
				//conversion from signed value to unit
				data.get(i).set(j, data.get(i).get(j)/32768*sensitivity);
			}
		}
	}
	//The following method uses the baseline to calibrate the values
	private void normalize(ArrayList<ArrayList<Double>> data)
	{
		for(int i =0; i<data.size(); i++)
		{
			for(int j = 0; j<data.get(i).size(); j++)
			{
				//subtracts the baseline from each of the values in order to "zero" the stationary part of the test
				data.get(i).set(j, data.get(i).get(j)-baselines[i]);
			}
		}
	}
	//the following method calculates the baseline for each column and stores it in the array baselines
	private void calculateBaseline(ArrayList<ArrayList<Double>> data)
	{
		double[] baselines = new double[9]; 
		for(int i = 0; i< data.size(); i++)
		{
			for(int j= 0; j<baselineSamples; j++)
			{
				baselines[i] += data.get(i).get(j)/baselineSamples;
			}
		}
		for(int i = 0; i< data.size(); i++)
		{
			this.baselines[i] = baselines[i];
		}
	}
	//the following method applies the moving average function to all of the columns in data
	public void smooth(ArrayList<ArrayList<Double>> data)
	{
		for(int i =0; i<data.size(); i++)
		{
			data.set(i, movingAverage(data.get(i), filter));
		}
	}
	//the following method applies the moving average to a specific column given the length of the average
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
			//each value is replaced with the average of "length" values after it
			retVal.add(average(range));
		}
		//keeps the value of the remaining values where a moving average cannot be applied
		for(int i =0; i<length; i++)
		{
			retVal.add(column.get(column.size()-length+i));
		}
		return retVal;
	}
	//The following method uses trapezoidal estimation in order to return an integrate version of the parameter "column"
	public ArrayList<Double> integrate(ArrayList<Double> column)
	{
		ArrayList<Double> retVal = new ArrayList<Double>();
		retVal.add((double) 0);
		for(int i = 0; i<column.size()-1; i++)
		{
			//adds the area of the trapezoid encompassed by the two points.
			retVal.add(retVal.get(i)+(column.get(i)+column.get(i+1))*0.5/(double)sampleRate);
		}
		return retVal;
	}
	//the following methods returns a column that contains absolute values of the values included in the parameter column
	public ArrayList<Double> absolute(ArrayList<Double> column)
	{
		ArrayList<Double> retVal = new ArrayList<Double>();
		for(int i = 0; i<column.size()-1; i++)
		{
			retVal.add(Math.abs(column.get(i)));
		}
		return retVal;
	}
	//the following method returns the an array in the form [maximum value of the column, point where the maximum occurs]
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
	//the following method returns the an array in the form [minimum value of the column, point where the minimum occurs]
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
	//the following method, checks whether the magnitude of the max is greater of the min is greater, and returns the greater value in the form(max/min, point)
	public double[] greatest(ArrayList<Double> column)
	{
		double[] max = max(column);
		double[] min = min(column);
		if(max[0]>Math.abs(min[0]))
			return max;
		return min;
	}
	//returns a columns that contains the product of the two parameterized columns
	public ArrayList<Double> product(ArrayList<Double> column1, ArrayList<Double> column2)
	{
		ArrayList<Double> retVal = new ArrayList<Double>();
		for(int i = 0; i<column1.size(); i++)
		{
			if(column2.size()>i)
			{
				retVal.add(column1.get(i)*column2.get(i));
			}
		}
		return retVal;
	}
	//returns the average of the parameter range
	public double average(ArrayList<Double> range)
	{
		double sum = 0;
		for(int i = 0; i<range.size(); i++)
		{
			sum+= range.get(i);
		}
		return sum/range.size();
	}
	//The following method creates a subrange of the parameter a from a start index and an end index
	public ArrayList<Double> subRange(ArrayList<Double> a, int start, int end )
	{
		ArrayList<Double> retVal = new ArrayList<Double>();
		for(int i = start; i< end; i++)
		{
			retVal.add(a.get(i));
		}
		return retVal;
	}
	//The following method obtains values for the inclined plane test, and returns in the form (acceleration along the slope, and max velocity of the "fall")
	public String[] inclinedPlane()
	{
		String[] retVal = new String[2];
		ArrayList<Double> acceleration = data1.get(1);
		ArrayList<Double> velocity = integrate(acceleration);
		double opposite = 0;
		//the opposite point of the impact
		int oppositePoint = 0;
		//impactPoint of the cart hitting the bottom
		int impactPoint = 0;
		//checks whether the max or the min is greater in magnitude
		if(max(acceleration)[0] >= Math.abs(min(acceleration)[0]))
		{
			//sets impact to the point of max acceleration
			impactPoint = (int) max(acceleration)[1];
			opposite = Math.abs(min(subRange(acceleration,0,impactPoint))[0]);
			oppositePoint = (int)min(subRange(acceleration,0,impactPoint))[1];
			//sets max velocity equal to the maximum speed before the point of collision to avoid integration errors
			retVal[1] = "Max Velocity: " + Double.toString(min(subRange(velocity,0,impactPoint))[0]) + " m/s^2";
		}
		else
		{
			//does the same thing as the if but in the case that the module is facing the opposite direction
			System.out.println(max(acceleration)[0]+","+min(acceleration)[0]);
			impactPoint = (int) min(acceleration)[1];
			opposite = Math.abs(max(subRange(acceleration,0,impactPoint))[0]);
			oppositePoint = (int)max(subRange(acceleration,0,impactPoint))[1];
			retVal[1] = "Max Velocity: " + Double.toString(max(subRange(velocity,0,impactPoint))[0]) + " m/s^2";
		}
		//creates two ranges around the opposite point to evaluate the acceleration along the slope
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
		//finds whichever average is closer and sets the acceleration along the slope equal to that value
		if(Math.abs(average(leftRange)-opposite) < Math.abs(average(rightRange)-opposite))
		{
			retVal[0] = "Acceleration Along Slope: " + Double.toString(average(leftRange)) + " m/s";
		}
		else
		{
			retVal[0] = "Acceleration Along Slope: " + Double.toString(average(rightRange)) + " m/s";
		}
		return retVal;
	}
	//the following method returns values from the pendulum test, in the form (max angular velocity(rad/s), period)
	public String[] pendulum()
	{
		String[] retVal = new String[2];
		ArrayList<Double> gyro = data1.get(5);
		double maxGyro = max(data1.get(5))[0];
		//finds the position of the first max in the gyro
		int firstMax = (int) max(data1.get(5))[1];
		//takes the max gyro and converts it to rad/s
		retVal[0] = "Max Velocity: " + Double.toString(maxGyro*Math.PI/180) + "rad/s";
		//finds the position of the minimum after the first max
		int minAfterFirstMax = (int)min(subRange(gyro, firstMax, gyro.size()))[1]+firstMax;
		//finds the time between the max and min and then doubles the value to get the period
		retVal[1] = "Period: " + Double.toString(2*(double)(minAfterFirstMax-firstMax)/(double)sampleRate) + " s";
		return retVal;
	}
	//the following method returns values for the spring lab, in the form (Period(s), max acceleration(m/s/s), max Velocity(m/s))
	public String[] spring()
	{
		String[] retVal = new String[3];
		ArrayList<Double> acceleration = data1.get(1);
		ArrayList<Double> velocity = integrate(acceleration);
		int firstMax = 0;
		int secondMax = 0;
		ArrayList<Boolean> maxima = localMaxima(velocity);
		//finding first max and second max
		for(int i = 0; i<maxima.size();i++)
		{
			if(maxima.get(i))
			{
				firstMax = i;
				break;
			}
		}
		for(int i = firstMax+1; i<maxima.size();i++)
		{
			if(maxima.get(i))
			{
				secondMax = i;
				break;
			}
		}
		//returns period by subtracting position of first max and position of second max
		retVal[0] = "Period: " + Double.toString((double)(secondMax-firstMax)/(double)sampleRate)+" s";
		//returns the maximum acceleration 
		retVal[1] = "Max Acceleration: " + Double.toString(greatest(acceleration)[0])+" m/s/s";
		//returns the max/min velocity depending on whichever one is greater
		retVal[2] = "Max Velocity: " + Double.toString(greatest(velocity)[0])+" m/s";
		return retVal;
	}
	//the following method creates a column that contains true if there is a max at a specific row in the parameter column, and false otherwise
	public ArrayList<Boolean> localMaxima (ArrayList<Double> column)
	{
		ArrayList<Boolean> maxima = new ArrayList<Boolean>();
		maxima.add(false);
		for(int i = 1; i<column.size()-1;i++)
		{
			//if the value is greater than both its predecessor and successor, and reaches a certain threshold that is 0.5x the max velocity
			if(column.get(i)>column.get(i-1)&&column.get(i)>column.get(i+1)&&column.get(i)>max(column)[0]/2)
			{
				maxima.add(true);
			}
			else
			{
				maxima.add(false);
			}
		}
		maxima.add(false);
		return maxima;
	}
	//returns values for the Conservation of momentum test, with the mass of both carts as parameters.
	//make sure that mass1 matches with data1 and mass 2 matches with data2
	//return values in the form(cart 1 mom bc, cart 2 mom bc, cart 1 mom ac, cart 2 mom ac, total mom bc, and total mom ac)
	public String[] COM(double mass1, double mass2)
	{
		String[] retVal = new String[6];
		ArrayList<Double> acceleration1 = data1.get(1);
		ArrayList<Double> acceleration2 = data2.get(1);
		ArrayList<Double> velocity1 = integrate(acceleration1);
		ArrayList<Double> velocity2 = integrate(acceleration2);
		ArrayList<Double> accelproduct =  product(acceleration1, acceleration2);
		accelproduct = absolute(accelproduct);
		//collision point occurs when the product of the accelerations is at a maximum
		int collisionPoint = (int) max(accelproduct)[1];
		System.out.println(collisionPoint);
		//the algorithm evaluates the momentum before and after the collision by going .25 s before and after collision point and recording the velocities of each cart
		retVal[0] = "Cart 1 Momentum Before Collision: "+Double.toString(mass1*velocity1.get(collisionPoint-sampleRate/4))+" kgm/s";
		retVal[1] = "Cart 2 Momentum Before Collision: "+Double.toString(mass2*velocity2.get(collisionPoint-sampleRate/4))+" kgm/s";
		retVal[2] = "Cart 1 Momentum After Collision: "+Double.toString(mass1*velocity1.get(collisionPoint+sampleRate/4))+" kgm/s";
		retVal[3] = "Cart 2 Momentum After Collision: "+Double.toString(mass2*velocity2.get(collisionPoint+sampleRate/4))+" kgm/s";
		retVal[4] = "Total Momentum Before Collision: "+ (double)(evaluateString(retVal[0])+evaluateString(retVal[1]))+" kgm/s";
		retVal[5] = "Total Momentum After Collision: "+ (double)(evaluateString(retVal[2])+evaluateString(retVal[3]))+" kgm/s";
		return retVal;
	}
	//the following method returns values for the conservation of energy test
	//the moment of inertia of the spinning device, mass of the dropping device, and time that the user wants to evaluate the energy
	//returns a variety of values which are indicated by their preceeding texts
	public String[] COE(double time, double spinMoi, double dropMass)
	{
		String[] retVal = new String[12];
		ArrayList<Double> spin = data1.get(5);
		ArrayList<Double> dropAcceleration = data2.get(1);
		ArrayList<Double> dropVelocity = integrate(dropAcceleration);
		ArrayList<Double> dropDistance = integrate(dropVelocity);
		int point = 0;
		int timePoint = (int) Math.floor(time*sampleRate);
		//depending on which direction the module is facing, gathers values on the point of max acceleration/bottom of drop and drop distance
		if(max(dropAcceleration)[0]>Math.abs(min(dropAcceleration)[0]))
		{
			point = (int)max(dropAcceleration)[1];
			double dist = Math.abs(min(subRange(dropDistance,0,point))[0]);
			retVal[2] = "Total Drop Distance: " + dist+" m";
			retVal[3] = "Energy at the top: " + dropMass*9.81*dist+" J";
			retVal[10] = "Potential Energy(point): " + dropMass*9.81*(dist-Math.abs(dropDistance.get(timePoint)))+" J";
		}
		else
		{
			
			point = (int)min(dropAcceleration)[1];
			double dist = Math.abs(max(subRange(dropDistance,0,point))[0]);
			retVal[2] = "Total Drop Distance: " + dist +" m";
			retVal[3] = "Energy at the top: " + dropMass*9.81*dist+" J";
			retVal[10] = "Potential Energy(point): " + dropMass*9.81*(dist - Math.abs(dropDistance.get(timePoint)))+" J";
		}
		//depending on direction of spin, finds the maximum angular velocity and rotational kinetic energy
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
		//returns the acceleration of the dropping module a tenth of a second before the bottom
		retVal[1] = "Linear Acceleration: " + Math.abs(dropAcceleration.get(point-sampleRate/10))+" m/s/s";
		//takes the gyro reading at a point and converts to rad/s
		retVal[5] = "Angular Velocity(point): "+Math.abs(spin.get(timePoint)*Math.PI/180)+" rad/s";
		//gets velocity at a certain point
		retVal[6] = "Linear Velocity(point): " + Math.abs(dropVelocity.get(timePoint))+" m/s";
		//gets drop distance at a certain point
		retVal[7] = "Drop Distance(point): " + Math.abs(integrate(dropVelocity).get(timePoint));
		//1/2 i omega^2
		retVal[8] = "Rotational Kinetic Energy(point): " + 0.5*spinMoi*Math.abs(spin.get(timePoint)*Math.PI/180)*Math.abs(spin.get(timePoint)*Math.PI/180)+" J";
		//1/2 m v^2
		retVal[9] = "Linear Kinetic Energy(point): " + 0.5*dropMass*Math.abs(dropVelocity.get(timePoint))*Math.abs(dropVelocity.get(timePoint))+" J";
		//adds all of the energies together
		retVal[11]  = "Total Energy(point): " + (double)(evaluateString(retVal[8])+evaluateString(retVal[9])+evaluateString(retVal[10]))+" J";
		return retVal;
	}
	//the following method retries the number value of a specified return value from the string that it returns
	public double evaluateString(String s)
	{
		return Double.parseDouble(s.split(" ")[s.split(" ").length-2]);
	}
}
