package org.peterstrand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SmallSectionResolver {
	
	private float[] sensorValues;
	private float[] frequencies;

	private List<Peak> peaks;
	
	public List<Peak> getPeaks() {
		return peaks;
	}
	private SmallSectionResolver()
	{
		super();
	}
	public static SmallSectionResolver createSectionResolver(float[] sensorValues)
	{
		assert sensorValues.length==FastFourierTransform.SMALLSECTION_SIZE;
		
		SmallSectionResolver result = new SmallSectionResolver();
		result.sensorValues = sensorValues;
		return result;
	}
	
	static private List<Peak> getPeaks(float[] frequencyPowers)
	{
		List<Peak> result = new ArrayList<Peak>();
		
		float minValueToConsider = frequencyPowers[0]/7.5f;
		float minAvgValueToConsider = getAverage(frequencyPowers,1)*3;
		int curFrequencyStart=0;
		boolean hasFrequency=false;
		float maxPower=0;
		
		// locate peaks
		// a peak is a collection of adjectent frequencypowers that each has these properties
		// - its powervalue most be larger that initialpower/7.5
		// - its powervalue most be larger than 3 times the average power value
		int j=0;
		for (float frequencyPower : frequencyPowers) {
			if (frequencyPower>minValueToConsider && frequencyPower>minAvgValueToConsider)
			{
				if(!hasFrequency)
				{
					hasFrequency=true;
					maxPower=frequencyPower;
					curFrequencyStart = j;
				}
				else
				{
					if (maxPower<frequencyPower)
						maxPower=frequencyPower;
				}
				
			}
			else
			{
				if (hasFrequency)
				{
					result.add(new Peak(maxPower, ((float)((curFrequencyStart+j)/2))/frequencyPowers.length));
					hasFrequency=false;
				}
			}	
			j++;
		}
		Collections.sort(result, new Comparator<Peak>() {

			@Override
			public int compare(Peak one, Peak two) {
				if (one.power>two.power)
					return -1;
				if (one.power<two.power)
					return 1;
				return 0;

			}
		});
		return result;
		
		
	}
	
	
	private static float getAverage(float[] floats) {
		return getAverage(floats,0);
	}
	private static float getAverage(float[] floats,int startIndex) {
		float sum = 0.0f;
		int i=0;
		for (float frequency : floats) {
			if (i>=startIndex)
				sum+=frequency;
			i++;
			
		}
		float avgValue = sum/(floats.length-startIndex);
		return avgValue;
	}
	
	private static MovementGuess getMovementFromAvg(float avg)
	{
		if (avg<=1.5)
			return MovementGuess.MOVING_SM;
		if (avg<=3.0)
			return MovementGuess.MOVING_ME;
		return MovementGuess.MOVING_HI;
		
	}
	
	public MovementGuess calculateMovement()
	{
		
		FastFourierTransform fft = new FastFourierTransform();
		
		frequencies = fft.fftMag(sensorValues);

		float avgEnergyUsed = getAverage(sensorValues);
		if (avgEnergyUsed<0.15)
			return MovementGuess.IDLE;
		
		if (avgEnergyUsed<1.5)
			return MovementGuess.MOVING_SM;
		
		assert frequencies.length == FastFourierTransform.SMALLSECTION_SIZE;
		
		peaks = getPeaks(frequencies);
		
		if (peaks.size()==0)
			return MovementGuess.IDLE;
		if (peaks.size()==1)
			return getMovementFromAvg(avgEnergyUsed);
		
		boolean firstIsSignificant = false;
		// figure out if first peak is significant
		if (peaks.size()==2)
		{	
			firstIsSignificant = true;
		}
		else
		{
			int counted = 0;
			float powerSum=0.0f;
			for (int i=2;i<Math.min(peaks.size(),5);i++)
			{
				powerSum+=peaks.get(i).power;
				counted++;				
			}
			
			float otherPeaksAvg = powerSum/counted;
			if (peaks.get(1).power*0.85>otherPeaksAvg)
				firstIsSignificant=true;			
		}
		
		if (!firstIsSignificant)
			return getMovementFromAvg(avgEnergyUsed);


		float firstFrequency = peaks.get(1).frequency;
		
		if ((firstFrequency>0.10) && (firstFrequency<0.25))
			return MovementGuess.WALKING;
		
		if ((firstFrequency>=0.25) && (firstFrequency<0.6))
			return MovementGuess.RUNNING;		
		
		return getMovementFromAvg(avgEnergyUsed);
		
	}
	public float[] getFrequencies() {
		return frequencies;
	}
	public float[] getRawValues() {
		return sensorValues;
	}
	public String getGuess() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
