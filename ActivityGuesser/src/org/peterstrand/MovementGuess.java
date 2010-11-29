package org.peterstrand;

public enum MovementGuess {
	IDLE(1),
	MOVING_SM(1),
	MOVING_ME(1),
	MOVING_HI(2),
	WALKING(5),
	RUNNING(5)
	;
	
	private int v;
	
	private MovementGuess(int v)
	{ 
		this.v = v;
	}
	
	public int getValue()
	{
		return v;
	}
	
	public static MovementGuess lookup(String s)
	{
		for (MovementGuess g : values()) {
			if (g.toString().toLowerCase().equals(s.toLowerCase()))
				return g;
		}
		return null;
	}
	
}
