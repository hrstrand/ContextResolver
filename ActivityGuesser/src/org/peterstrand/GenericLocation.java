package org.peterstrand;

public interface GenericLocation {

	void setLatitude(double latitude);

	void setLongitude(double longitude);

	void setAccuracy(float accuracy);

	void setTime(long parseLong);

	long getTime();

	float getAccuracy();

	double getLatitude();

	double getLongitude();

	float distanceTo(GenericLocation l2);

	boolean hasAccuracy();

	String getProvider();
	
}
