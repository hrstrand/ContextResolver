package org.peterstrand;

public class Peak implements Comparable<Peak>{
	@Override
	public String toString() {
		return "Peak [frequency=" + frequency + ", power=" + power + "]";
	}

	float power;
	float frequency;
	
	public Peak(float power, float frequency) {
		super();
		this.power = power;
		this.frequency = frequency;
	}

	@Override
	public int compareTo(Peak another) {
		if (power>another.power)
			return 1;
		if (power<another.power)
			return -1;
		return 0;

		
	}
}
