package org.peterstrand.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.peterstrand.GenericLocation;
import org.peterstrand.MovementGuess;
import org.peterstrand.SmallSectionResolver;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.util.Log;

public class MovementSession {

	 
	
	private long startTime;
	private GenericLocation initialLocation;
	private File directory;
	private int smallSectionCount;
	private SensorService service;
	private List<SmallSection> smallSections;
	private SessionState state;
	
	private MovementSession(long startTime, SensorService service)
	{
		super();
		this.startTime = startTime;
		this.service = service;
		String topFolder = Util.dateFormat.format(startTime);
		String subFolder = Util.timeFormat.format(startTime);
		this.directory = new File(Environment.getExternalStorageDirectory()+File.separator+"contextresolver"+File.separator+topFolder+File.separator+subFolder);
		this.directory.mkdirs();
		this.smallSectionCount = 0;
		this.smallSections = new ArrayList<SmallSection>();
		this.state = SessionState.RECORD;
		
	}
	
	private void changeState(SessionState newState)
	{
		Log.i(SensorService.LOGID,"changing movementsessionstate from "+this.state+" to "+newState);
		this.state = newState;
	}
	
	public static MovementSession createNewSession( SensorService service) {
		MovementSession result = new MovementSession( System.currentTimeMillis(),service);
		return result;
	}

	public void addSmallSectionResult(SmallSection smallSection, SmallSectionResolver resolver)
	{
		boolean stopService = false;
		boolean isIdle = true;
		if (!hasInitialLocation())
		{
			if (smallSection.getLocation()!=null)
				initialLocation = smallSection.getLocation();
		}
		smallSections.add(smallSection);
		
		 if (smallSections.size()>SensorService.SMALL_SECTIONS_IN_LARGE)
		 {
			 
			for (int i=smallSections.size()-1;i>smallSections.size()-10;i--)
			{
				if (smallSections.get(i).getGuess()!=MovementGuess.IDLE)
				{
					isIdle=false;
					break;
				}
				 
			 }
			
			 if(isIdle)
			 {
				 // ok -- we've gone idle after some activity. Geocode the location
				 Location lastLocation = service.getLastKnownLocation();
				 if(lastLocation!=null)
				 {
					 try {
						 Geocoder geocoder = new Geocoder(service, Locale.getDefault());
						 List<Address> addresses;
						 addresses = geocoder.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 1);
						 if (addresses.size()>=1)
						 {
							 smallSection.setAddress(addresses.get(0));
							 Log.i(SensorService.LOGID, "resolved address to "+smallSection.getAddress());
						 }
					 } catch (IOException e) {
						 Log.e(SensorService.LOGID,e.getMessage());
					 }
				 }
				 // first is service start-idle. If the service has just started and the device is idle, go ahead and kill the service again.
				 Log.i(SensorService.LOGID,"stopping service due to idleness");
				
				 changeState(SessionState.STOP);
				

			 }
		 }
		 
		 
		 // clean up in smallsections
		 if (smallSections.size()>SensorService.KEEP_SMALLSECTIONS)
			 smallSections.remove(0);

		
		float localSpeed = Util.calcAvgSpeedFromSmallSections(smallSections, 60*1, 60*3 , System.currentTimeMillis());
		float longSpeed  = Util.calcAvgSpeedFromSmallSections(smallSections, 60*5, 60*10, System.currentTimeMillis());
		

		
		try {
			Util.writeSmallSection(smallSection, directory, smallSectionCount, resolver, localSpeed, longSpeed);
		} catch (IOException e) {
			Log.e(SensorService.LOGID, e.getMessage());
		}
		
		smallSectionCount++;
		

	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public boolean hasInitialLocation()
	{
		return initialLocation != null;
	}
	
	public SessionState getState()
	{
		return state;
	}
	
	/**
	 * returns speed in meters pr. seconds for the range of smallsections that is in the second range
	 * returns -1 if not enough data
	 * returns -2 if the user is stationary
	 * @param smallSections
	 * @return
	 */
	public float calcAvgSpeed( int secondsBackMin, int secondsBackMax, long currentTime)
	{
		int stationaries = 0;
		int pointsChecked = 0;
		
		GenericLocation latestLocation = smallSections.get(smallSections.size()-1).getLocation();
		GenericLocation mostAccurateInTimeSlot = null;
	
		
		for (SmallSection section : smallSections)
		{
			GenericLocation thisLoc = section.getLocation();
			if (thisLoc.hasAccuracy())
			{
			/*	System.out.println("-------------------------------------------------------------");
				System.out.println("lo-time "+datetimeFormat.format(currentTime-(secondsBackMax*1000L)));
				System.out.println("hi-time "+datetimeFormat.format(currentTime-(secondsBackMin*1000L)));
				System.out.println("loctime "+datetimeFormat.format(thisLoc.getTime()));
				
				System.out.println("curtime "+datetimeFormat.format(currentTime));
				*/
				if (thisLoc.getTime()<currentTime-(secondsBackMin*1000L) && (thisLoc.getTime()>currentTime-(secondsBackMax*1000L)))			
				{
					float minDist =  Util.getMinDistance(thisLoc, latestLocation);
					if (minDist<25)
						stationaries++;
					pointsChecked++;
			
					
					// record the most accurate reading in that time slot
					if(mostAccurateInTimeSlot==null ||  (thisLoc.getAccuracy()<mostAccurateInTimeSlot.getAccuracy()))
						mostAccurateInTimeSlot = thisLoc;
	
						
				}
			}
		}
		
		if (mostAccurateInTimeSlot==null)
			return -1;
		
		// take the distance between the 2nd point and the latest
		GenericLocation pm1 = latestLocation;
		GenericLocation pm2 = mostAccurateInTimeSlot;
		//only use points with accuracy less than 100 meters
		
		//float d1 =  Util.getGuessedDistance(pm1, pm2);
		float d1 =  Util.getMinDistance(pm1, pm2);
		//float d1 = pm1.distanceTo(pm2);
		//only use points with accuracy less than 100 meters
		
		float speed =  d1/((pm1.getTime()-pm2.getTime())/1000);
		
		// check if stationary
		// more than 5 points needed and they must all be stationary
		//if (pointsChecked>5 && pointsChecked==stationaries)
		//	return -2;
		return speed;
	}
	
	
}
