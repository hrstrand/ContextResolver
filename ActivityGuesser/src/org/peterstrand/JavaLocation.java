package org.peterstrand;
 
public class JavaLocation implements GenericLocation {
	
	float	accuracy;
	double	latitude;
	double	longitude;
	long	time;
	String	provider;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(accuracy);
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((provider == null) ? 0 : provider.hashCode());
		result = prime * result + (int) (time ^ (time >>> 32));
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
		JavaLocation other = (JavaLocation) obj;
		if (Float.floatToIntBits(accuracy) != Float.floatToIntBits(other.accuracy))
			return false;
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
			return false;
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
			return false;
		if (provider == null) {
			if (other.provider != null)
				return false;
		} else if (!provider.equals(other.provider))
			return false;
		if (time != other.time)
			return false;
		return true;
	}

	public JavaLocation(String provider) {
		super();
		this.provider = provider;
	}
	
	@Override
	public String toString() {
		return "JavaLocation [accuracy=" + accuracy + ", latitude=" + latitude + ", longitude=" + longitude + ", provider=" + provider + ", time=" + time + "]";
	}

	public String getProvider() {
		return provider;
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public float getAccuracy() {
		return accuracy;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public long getTime() {
		return time;
	}
	
	@Override
	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
		
	}
	
	@Override
	public void setLatitude(double latitude) {
		this.latitude = latitude;
		
	}
	
	@Override
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	@Override
	public void setTime(long time) {
		this.time = time;
	}
	
	private double distance(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		
		dist = dist * 1.609344 * 1000;
		
		return (dist);
	}
	
	private double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}
	
	private double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}
	
	@Override
	public float distanceTo(GenericLocation l2) {
		return (float) distance(latitude, longitude, l2.getLatitude(), l2.getLongitude());
	}
	
	@Override
	public boolean hasAccuracy() {
		// TODO Auto-generated method stub
		return true;
	}
	
}
