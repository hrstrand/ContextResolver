package org.peterstrand.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.peterstrand.MovementGuess;

import android.location.Address;
import android.location.Location;
import android.util.Log;

public class LargeSection {
	private long startTime;
	private long endTime;
	private Location lastKnownLocation;
	private Address lastKnownAddress;
	private SummarizedGuess guess;
	private float avgSpeed;

	
	public LargeSection(long startTime, long endTime, Location lastKnownLocation, Address lastKnownAddress, SummarizedGuess guess, float avgSpeed) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		this.lastKnownLocation = lastKnownLocation;
		this.lastKnownAddress = lastKnownAddress;
		this.guess = guess;
		this.avgSpeed = avgSpeed;
	}
	
	public long getStartTime() {
		return startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public Location getLastKnownLocation() {
		return lastKnownLocation;
	}
	public Address getLastKnownAddress() {
		return lastKnownAddress;
	}
	public SummarizedGuess getGuess() {
		return guess;
	}
	public float getAvgSpeed() {
		return avgSpeed;
	}
	
	
	
	public static SummarizedGuess getSummaryForNext(List<SmallSection> smallSections, LargeSection lastLargeSection, long currentTime)
	{
		float speed = Util.calcAvgSpeedFromSmallSections(smallSections, 60, 300, currentTime);
		if (speed==-2)
			return SummarizedGuess.STATIONARY;
		
		float speedKMT = speed<0? -1 : speed*3.6f;
		Map<MovementGuess, Integer> scoremap = new HashMap<MovementGuess, Integer>();
		for (SmallSection smallSection : smallSections) {
			int num=0;
			if(scoremap.containsKey(smallSection.getGuess()))
			{
				num = scoremap.get(smallSection.getGuess());
				
			}
			num = num+smallSection.getGuess().getValue();
			
			scoremap.put(smallSection.getGuess(), num);
		}
		MovementGuess highest = MovementGuess.IDLE;
		int high = 0;
		// find highest
		for (MovementGuess g : scoremap.keySet()) {
			if(scoremap.get(g)>high)
			{
				high = scoremap.get(g);
				highest = g;
			}
		}
		
		
		if (highest==MovementGuess.IDLE)
		{
			if (speedKMT>0.5)
				Log.i("CONTRADICT", "MovementGuess highscore is IDLE, but speed is "+speed);
			
			return SummarizedGuess.IDLE;
		}
		/*
		if (highest==MovementGuess.)
		{
			if (spee>0.2)
				Log.i("CONTRADICT", "MovementGuess highscore is IDLE, but speed is "+speed);
			
			return SummarizedGuess.IDLE;
		}
		*/
		return SummarizedGuess.IDLE;
		
	}
	
	
}
