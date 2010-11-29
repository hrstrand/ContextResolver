package org.peterstrand.service;

public class MovementState {
	private float propability = 0;
	private float max = 0;
	
	public MovementState(float max) {
		this.max = max;
	}

	public void incProp(float p)
	{
		propability+=p;
		if (propability>max)
			propability=max;
	}
	
	public void decProp(float p)
	{
		propability-=p;
		if (propability<0)
			propability=0;
	}
	
	
	public float getProp()
	{
		return propability;
	}
}
	