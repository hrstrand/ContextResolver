package org.peterstrand.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.peterstrand.FttTest;
import org.peterstrand.GenericLocation;
import org.peterstrand.SmallSectionResolver;



public class Util {
	public static final NumberFormat formatter = new DecimalFormat("#0.00");
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
	public static final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	public static void writeSmallSection(
			SmallSection smallSection, 
			File directory, 
			int index, 
			SmallSectionResolver resolver,
			float localSpeed,
			float longSpeed) throws IOException {
		
		float[] rawValues = resolver.getRawValues(); 
		float[] fft = resolver.getFrequencies();
		directory.mkdirs();
		if (directory.exists())
		{
			 File f  = new File(directory.getAbsolutePath()+File.separator+index+" "+smallSection.getGuess()+".txt");
			 File f2 = new File(directory.getAbsolutePath()+File.separator+index+" "+smallSection.getGuess()+".fft.txt");
			 File f3 = new File(directory.getAbsolutePath()+File.separator+index+" "+smallSection.getGuess()+".html");
			 
			 FileWriter fw;
			 try {
				fw = new FileWriter(f);
				fw.write("GUESS    :"+smallSection.getGuess()+"\n");
				fw.write("STARTTIME:"+smallSection.getStartTime()+"\n");
				fw.write("ENDTIME  :"+smallSection.getEndTime()+"\n");
				fw.write("LOCALSPD :"+localSpeed+"\n");
				fw.write("LONGSPD  :"+longSpeed+"\n");
				if(smallSection.getLocation()==null)
					fw.write("LOCATION :\n");
				else
					fw.write("LOCATION :"+smallSection.getLocation().toString()+"\n");
				
				if(smallSection.getAddress()==null)
					fw.write("ADDRESS :\n");
				else
					fw.write("ADDRESS :"+smallSection.getAddress().toString()+"\n");
				
				
				fw.write("\n");
				for (int i = 0; i< rawValues.length; i++)
				{
					fw.write(formatter.format(rawValues[i])+"\n");
				}
				fw.flush();
				fw.close();
			 } catch (IOException e) {
				e.printStackTrace();
			}
			
			 try {
					fw = new FileWriter(f2);
					fw.write("GUESS    :"+smallSection.getGuess()+"\n");
					fw.write("STARTTIME:"+smallSection.getStartTime()+"\n");
					fw.write("ENDTIME  :"+smallSection.getEndTime()+"\n");
					fw.write("LOCALSPD :"+localSpeed+"\n");
					fw.write("LONGSPD  :"+longSpeed+"\n");
					
					if(smallSection.getLocation()==null)
						fw.write("LOCATION :\n");
					else
						fw.write("LOCATION :"+smallSection.getLocation().toString()+"\n");
					
					if(smallSection.getAddress()==null)
						fw.write("ADDRESS :\n");
					else
						fw.write("ADDRESS :"+smallSection.getAddress().toString()+"\n");
					

					fw.write("\n");
					for (int i = 0; i< fft.length; i++)
					{
						fw.write(formatter.format(fft[i])+"\n");
					}
					fw.flush();
					fw.close();
				 } catch (IOException e) {
					e.printStackTrace();
				}
			 
		    String graphHtml = FttTest.createHtmlGraphs(f3, resolver, smallSection.getStartTime(), smallSection.getEndTime(), smallSection.getGuess(), smallSection.getLocation(), localSpeed, longSpeed);
		    BufferedWriter writer = new BufferedWriter(new FileWriter(f3));
		    
            writer.write("<html><body>");
            writer.write(graphHtml);
            writer.write("</body></html>");
			writer.close();
				 

		}
	}

	/*
	 * returns the minimum distance between two points, when accuracy is considered.
	 * Returns 0 if the two points are closer than the sum of their accuracy
	 */
	
	public static float getMinDistance(GenericLocation l1, GenericLocation l2)
	{
		if (l1==null || l2==null)
			return 0; 
		
		float dist = l1.distanceTo(l2);
		float minDist = dist-l1.getAccuracy()-l2.getAccuracy();
		return Math.max(minDist, 0);
	}
	
	public static float getGuessedDistance(GenericLocation l1, GenericLocation l2)
	{
		if (l1==null || l2==null)
			return 0; 
		
		float dist = l1.distanceTo(l2);
		float minDist = dist-((l1.getAccuracy()+l2.getAccuracy())/2);
		return Math.max(minDist, 0);
	}
	
	
	/**
	 * returns speed in meters pr. seconds for the range of smallsections that is in the second range
	 * returns -1 if not enough data
	 * returns -2 if the user is stationary
	 * @param smallSections
	 * @return
	 */
	public static float calcAvgSpeedFromSmallSections(List<SmallSection> smallSections, int secondsBackMin, int secondsBackMax, long currentTime)
	{
		int stationaries = 0;
		int pointsChecked = 0;
		
		GenericLocation latestLocation = smallSections.get(smallSections.size()-1).getLocation();
		GenericLocation mostAccurateInTimeSlot = null;
	
		GenericLocation last = null;
		
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
