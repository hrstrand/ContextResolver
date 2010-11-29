package org.peterstrand.service;

import org.peterstrand.GenericLocation;

import android.location.Location;

public class LocationWrapper implements GenericLocation {

	@Override
	public String toString() {
		return location.toString();
	}

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocationWrapper other = (LocationWrapper) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}

	private Location location;
	public  LocationWrapper(Location location)
	{
		super();
		this.location = location;
	}

	@Override
	public void setAccuracy(float accuracy) {
		location.setAccuracy(accuracy);
		
	}
	
	@Override
	public void setLatitude(double latitude) {
		location.setLatitude(latitude);
		
	}
	
	@Override
	public void setLongitude(double longitude) {
		location.setLongitude(longitude);
		
	}
	
	@Override
	public void setTime(long time) {
		location.setTime(time);
		
	}

	@Override
	public float distanceTo(GenericLocation l2) {
		
		return location.distanceTo(createFromGeneric(l2));
	}

	@Override
	public float getAccuracy() {
		return location.getAccuracy();
	}

	@Override
	public double getLatitude() {
		return location.getLatitude();
	}

	@Override
	public double getLongitude() {
		return location.getLongitude();
	}

	@Override
	public long getTime() {
		return location.getTime();
	}

	@Override
	public boolean hasAccuracy() {
		return location.hasAccuracy();
	}
	
	private static Location createFromGeneric(GenericLocation g)
	{
		Location l = new Location(g.getProvider());
		l.setAccuracy(g.getAccuracy());
		l.setLatitude(g.getLatitude());
		l.setLongitude(g.getLongitude());
		l.setTime(g.getTime());
			
		return l;
	}

	@Override
	public String getProvider() {
		return location.getProvider();
		
	}	
}
