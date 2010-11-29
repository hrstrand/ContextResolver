package org.peterstrand.service;

import org.peterstrand.MovementGuess;

public class MovementStateManager {
	
	private MovementState walkState;
	private MovementState runState;
	private MovementState bikeState;
	private MovementState vehicleState;
	
	
	private MovementStateManager()
	{
		walkState = new MovementState(2f);
		runState = new MovementState(2f);
		bikeState = new MovementState(2f);
		vehicleState = new MovementState(2.1f);
	}
	
	private static MovementStateManager instance = new MovementStateManager();
	
	public static MovementStateManager get()
	{
		return instance;
	}
	
	public String toString()
	{
		return 
		"walk : "+Util.formatter.format(walkState.getProp())+"\t"+
		"run  : "+Util.formatter.format(runState.getProp())+"\t"+
		"bike : "+Util.formatter.format(bikeState.getProp())+"\t"+
		"vehi : "+Util.formatter.format(vehicleState.getProp());
	}
	
	public SummarizedGuess getSummarizedGuess()
	{
		/*IDLE,
		STATIONARY,
		WALKING,
		RUNNING,
		BIKING,
		VEHICLE
		*/
		SummarizedGuess result = SummarizedGuess.WALKING;
		float hiValue = walkState.getProp();
		
		if (runState.getProp()>hiValue)
		{
			result = SummarizedGuess.RUNNING;
			hiValue = runState.getProp(); 
		}
	
		if (bikeState.getProp()>hiValue)
		{
			result = SummarizedGuess.BIKING;
			hiValue = bikeState.getProp(); 
		}
		
		if (vehicleState.getProp()>hiValue)
		{
			result = SummarizedGuess.VEHICLE;
			hiValue = vehicleState.getProp(); 
		}
		
		
		if (hiValue<=0.3f)
			result = SummarizedGuess.IDLE;
		
		if(result == SummarizedGuess.BIKING)
			result = SummarizedGuess.VEHICLE;
		return result;
		
		
		
	}
	
	
	public void handleData(MovementGuess guess, float speed)
	{
		float speedInKmh = speed*3.6f;


		
		
		if (guess == MovementGuess.IDLE)
		{
			walkState.decProp(1.0f);
			runState.decProp(1.0f);
			bikeState.decProp(0.3f);
			vehicleState.decProp(0.1f);
		}
		
		
		if (guess == MovementGuess.WALKING)
		{
			walkState.incProp(0.5f);
			runState.decProp(0.1f);
			bikeState.decProp(0.2f);
			vehicleState.decProp(0.3f);
		}
		
		if (guess == MovementGuess.RUNNING)
		{
			runState.incProp(0.5f);
			walkState.decProp(0.1f);
			bikeState.decProp(0.2f);
			vehicleState.decProp(0.3f);
		}
		
		if (guess == MovementGuess.MOVING_SM)
		{
			// little energy does not correspond to walking or running
			runState.decProp(0.3f);
			walkState.decProp(0.3f);
			
			// increase bike a bit 
			if ((speedInKmh>5) && (speedInKmh<30))
			{
				bikeState.incProp(0.05f);
			}
			if (speedInKmh>5)
			{
				vehicleState.incProp(0.1f);
				runState.decProp(0.5f);
				walkState.decProp(0.5f);
			}
			
		}
		
		if (guess == MovementGuess.MOVING_ME)
		{
			if ((speedInKmh>5) && (speedInKmh<30))
			{
				bikeState.incProp(0.5f);
				runState.decProp(0.5f);
				walkState.decProp(0.5f);				
				
			}
			
		}
		
		if (guess == MovementGuess.MOVING_HI)
		{
			if ((speedInKmh>5) && (speedInKmh<30))
			{
				bikeState.incProp(0.5f);
				runState.decProp(0.5f);
				walkState.decProp(0.5f);
			}
		}
		
		
		if (speedInKmh>30)
		{
			vehicleState.incProp(0.5f);
		}
		
		
		
	}
}
