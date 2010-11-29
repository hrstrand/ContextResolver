package org.peterstrand.service;

import org.peterstrand.GenericLocation;
import org.peterstrand.MovementGuess;

import android.location.Address;
import android.location.Location;

public class SmallSection {
	private long startTime;
	private long endTime;
	private GenericLocation location;
	private Address address;
	private MovementGuess guess;


	public SmallSection(long startTime, long endTime, MovementGuess guess, GenericLocation location) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
		this.guess = guess;
		this.location = location;
		

	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Address getAddress() {
		return address;
	}

	public GenericLocation getLocation() {
		return location;
	}

	public long getStartTime() {
		return startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	
	public MovementGuess getGuess() {
		return guess;
	}
	
	
	
}
