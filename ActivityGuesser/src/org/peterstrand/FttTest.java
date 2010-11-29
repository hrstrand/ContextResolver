package org.peterstrand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;
import org.peterstrand.service.MovementStateManager;
import org.peterstrand.service.SmallSection;
import org.peterstrand.service.SummarizedGuess;
import org.peterstrand.service.Util;

import android.location.Location;
import android.location.LocationProvider;

public class FttTest {
	
	private static final int	HI_ACCURACY	= 200;

	public static float enhance(float f)
	{
		return f;
	}
	
	public static String createHtmlGraphs(File file) throws NumberFormatException, IOException
	{
        NumberFormat formatter = new DecimalFormat("#0.000");
		
		String text = null;
        List<Float> l = new ArrayList<Float>();
        List<Long> t = new ArrayList<Long>();
        // repeat until all lines is read
        
		FastFourierTransform  fft = new FastFourierTransform();

		BufferedReader reader = new BufferedReader(new FileReader(file));

        boolean first = true;
        while ((text = reader.readLine()) != null)
        {
        	try
        	{
        		float value = Float.parseFloat(text);
        		if (first)
        			value=0.0f;
        		first=false;
        		l.add(value);
        	}
        	catch(Exception e)
        	{
        	
        	}
        }

        int sz =2;
        while(sz<l.size() && sz<=FastFourierTransform.SMALLSECTION_SIZE)
        {
        	sz*=2;
        }

        while(l.size()>sz)
        	l.remove(l.size()-1);
        
        while(t.size()>sz)
        	t.remove(t.size()-1);
        

        
        float[] asArr = new float[l.size()];
        SmallSectionResolver ssr = SmallSectionResolver.createSectionResolver(asArr);
        int i=0;
        for (float d : l) {
        	asArr[i]=l.get(i);
        	i++;
		}
        MovementGuess guess = ssr.calculateMovement();
        return createHtmlGraphs(file, ssr, 0, 12000,guess,null,-1f, -1f);
		
		
	}

	public static String createHtmlGraphs(File file, SmallSectionResolver resolver, long minTime, long maxTime, MovementGuess guess, GenericLocation location, float localSpeed, float longSpeed) {
		
        int i =0;
        float vMax=0;
        float rMax=0;
        for (float d : resolver.getRawValues()) {
        	if (vMax<d)
        		vMax=d;
        	i++;
		}
       
        
       
        int j = 0;
        for (float f : resolver.getFrequencies()) {
        	f = enhance(f);
        	if (rMax<f)
        		rMax=f;
        	j++;
		}
        

        long rangeT = (maxTime-minTime)/1000L;
        
        StringBuffer html= new StringBuffer();
        html.append("<br/><strong><h1>"+file.getName()+"</h1></strong><br/>");
        html.append("<img src=\"http://chart.apis.google.com/chart?&chxr=0,0,"+rangeT+"|1,0,"+Util.formatter.format(vMax)+"&chxs=0,676767,11.5,0,lt,676767|1,676767,11.5,0,lt,676767&chxt=x,y&chs=1000x300&cht=lc&chco=3D7930&chds=0,"+Util.formatter.format(vMax)+"&chd=t:");
        j=0;
        for (float d :  resolver.getRawValues()) {

        	 html.append(Util.formatter.format(d)+(j== resolver.getRawValues().length-1?"":","));
        	j++;
		}

        html.append("&chg=14.3,-1,1,1&chls=2,4,0&chm=B,C5D4B5BB,0,0,0\" width=\"1000\" height=\"300\" alt=\"\" />");

        html.append("<img src=\"http://chart.apis.google.com/chart?chxr=0,0,"+Util.formatter.format(rMax)+"|1,0,1&chxt=y&chbh=a,1,7&chs=1000x300&cht=bvg&chco=3D7930&chds=0,"+Util.formatter.format(rMax)+"&chd=t:");
        j=0;
        for (float d : resolver.getFrequencies()) {
        	 d = enhance(d);
        	 html.append(Util.formatter.format(d)+(j==resolver.getFrequencies().length-1?"":","));
        	j++;
		}
        
        html.append("&chg=14.3,-1,1,1&chls=2,4,0&chm=B,C5D4B5BB,0,0,0\" width=\"1000\" height=\"300\" alt=\"\" />");
        html.append("<br/>Resolved movement : "+guess);				
		html.append("<br/>LOCALSPD: "+localSpeed);
		html.append("<br/>LONGSPD : "+longSpeed);
		html.append("<br/>peaks : "+resolver.getPeaks());
        html.append("<br/>peaks : "+resolver.getPeaks());
        if (location!=null)
        {
        	html.append("<br/><strong><h1>Location</h1></strong><br/>");
        	html.append("<br/>Age of location :"+(System.currentTimeMillis()-location.getTime())/1000);
        	html.append("<br/>location accuracy :"+location.getAccuracy());
        	html.append("<br/>location :"+location.toString());     
        	
        	
        	html.append("<br/><img src=\"http://maps.google.com/maps/api/staticmap?center="+location.getLatitude()+","+location.getLongitude()+"&zoom=12&size=1024x512&maptype=roadmap&markers=color:blue|label:SSS|"+location.getLatitude()+","+location.getLongitude()+"&sensor=false\" />");
        }
        return html.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

			
			//List<SmallSection> l = getSmallSectionsFromDirectory("/home/pstrand/Desktop/context/2010-11-01");
			String s = analyzeDate("/home/pstrand/Desktop/context/2010-10-28");
            System.out.println(s);
			
	}
	
	public static String createRouteMap(List<GenericLocation> chosenLocations, List<GenericLocation> allLocations, boolean drawroute)
	{
				
		String s = "<!DOCTYPE html>\n"+
		"<html>\n"+
		"<head>\n"+
		"<meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\" />\n"+
		"<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>\n"+
		"<title>Google Maps JavaScript API v3 Example: Polyline Simple</title>\n"+
		"<link href=\"http://code.google.com/apis/maps/documentation/javascript/examples/default.css\" rel=\"stylesheet\" type=\"text/css\" />\n"+
		"<script type=\"text/javascript\" src=\"http://maps.google.com/maps/api/js?sensor=false\"></script>\n"+
		
		"<script type=\"text/javascript\">\n"+
		"function Label(opt_options) {\n"+
		"	 // Initialization\n"+
		"	 this.setValues(opt_options);\n"+
		"	\n"+
		"	 // Label specific\n"+
		"	 var span = this.span_ = document.createElement('span');\n"+
		"	 span.style.cssText = 'position: relative; left: -50%; top: -8px; ' +\n"+
		"	                      'white-space: nowrap; border: 1px solid blue; ' +\n"+
		"	                      'padding: 2px; background-color: white';\n"+
		"\n"+
		"	 var div = this.div_ = document.createElement('div');\n"+
		"	 div.appendChild(span);\n"+
		"	 div.style.cssText = 'position: absolute; display: none';\n"+
		"	};\n"+
		"	Label.prototype = new google.maps.OverlayView;\n"+

		"// Implement onAdd\n"+
		"	Label.prototype.onAdd = function() {\n"+
		"	 var pane = this.getPanes().overlayLayer;\n"+
		"	 pane.appendChild(this.div_);\n"+

		"// Ensures the label is redrawn if the text or position is changed.\n"+
		"	 var me = this;\n"+
		"	 this.listeners_ = [\n"+
		"	   google.maps.event.addListener(this, 'position_changed',\n"+
		"	       function() { me.draw(); }),\n"+
		"	   google.maps.event.addListener(this, 'text_changed',\n"+
		"	       function() { me.draw(); })\n"+
		"	 ];\n"+
		"	};\n"+

		"	// Implement onRemove\n"+
		"	Label.prototype.onRemove = function() {\n"+
		"	 this.div_.parentNode.removeChild(this.div_);\n"+
		"\n"+
		"	 // Label is removed from the map, stop updating its position/text.\n"+
		"	 for (var i = 0, I = this.listeners_.length; i < I; ++i) {\n"+
		"	   google.maps.event.removeListener(this.listeners_[i]);\n"+
		"	 }\n"+
		"	};\n"+

		"	// Implement draw\n"+
		"	Label.prototype.draw = function() {\n"+
		"	 var projection = this.getProjection();\n"+
		"	 var position = projection.fromLatLngToDivPixel(this.get('position'));\n"+

		"	 var div = this.div_;\n"+
		"	 div.style.left = position.x + 'px';\n"+
		"	 div.style.top = position.y + 'px';\n"+
		"	 div.style.display = 'block';\n"+

		"	 this.span_.innerHTML = this.get('text').toString();\n"+
		"	};\n"+

		 " function initialize() {\n"+
		 "   var myLatLng = new google.maps.LatLng(55, 12);\n"+
		 "   var myOptions = {\n"+
		 "     zoom: 6,\n"+
		 "     center: myLatLng,\n"+
		 "     mapTypeId: google.maps.MapTypeId.ROADMAP\n"+
		 "   };\n"+

		 "   var map = new google.maps.Map(document.getElementById(\"map_canvas\"), myOptions);\n"+

		 "   var flightPlanCoordinates = [\n";
		 if (drawroute)
		 {
			 for (GenericLocation genericLocation : allLocations) {
				 boolean isUsedLocation = chosenLocations.contains(genericLocation);
				 if (isUsedLocation) 
					 s+="       new google.maps.LatLng(\n"+genericLocation.getLatitude()+", \n"+genericLocation.getLongitude()+"),";
			 }
		 }
		 s=s.substring(0,s.length()-1);
		 s+="   ];\n"+
		 "   var flightPath = new google.maps.Polyline({\n"+
		 "     path: flightPlanCoordinates,\n"+
		 "     strokeColor: \"#FF0000\",\n"+
		 "     strokeOpacity: 1.0,\n"+
		 "     strokeWeight: 2\n"+
		 "   });\n"+

		 "  flightPath.setMap(map);\n";
		 for (GenericLocation genericLocation : allLocations) {
			 long dt = (genericLocation.getTime()-(allLocations.get(0).getTime()))/(1000*60);
			 
			 boolean isUsedLocation = chosenLocations.contains(genericLocation);
			 String fillColor = isUsedLocation ? "#0000FF" : "FF0000";
			 String opacity = isUsedLocation ? "0.2" : "0.1";
			 
			 s+=	
	        "var cirle = new google.maps.Circle({"+
	        "  fillColor : '"+fillColor+"'," +
	        "  fillOpacity : "+opacity+","+
	        "  strokeWeight : 0,"+
	        "  map: map,"+
	        "  radius: "+(genericLocation.getAccuracy())+","+
	        "center: new google.maps.LatLng("+genericLocation.getLatitude()+", "+genericLocation.getLongitude()+")"+
	        " });\n"+
	
	
	        "var label = new Label({"+
	        "  map: map,"+
	        "position: new google.maps.LatLng("+genericLocation.getLatitude()+", "+genericLocation.getLongitude()+"),"+
	        "text : '"+dt+"'"+
	        " });\n";
			
		 }
		 
		 s+=
		 " }\n"+
		 " </script>\n"+
		 " </head>\n"+
		 "<body onload=\"initialize()\">\n"+
		 " <div id=\"map_canvas\"></div>\n"+
		 "	</body>\n"+

		 "</html>\n";
		 return s;
	}
	
	
	
	public static String analyzeDate(String directory)
	{
		try {
			List<GenericLocation> summaryLocations = new ArrayList<GenericLocation>();
			List<GenericLocation> allLocations = new ArrayList<GenericLocation>();
			List<SmallSection> l = getSmallSectionsFromDirectory(directory);
			GenericLocation firstLocation = null; 
			GenericLocation mostDistantLocation = null;
			GenericLocation lastPrintedLocation = null;
			GenericLocation lastAccurateLocation = null;
			GenericLocation lastLocation = null;
			long eventTravel = 0;
			long totalTravel = 0;
			long lastTimestamp = -1;
			long walkMsec = 0;
			long runMsec = 0;
			long vehicleMsec = 0;
			long totalWalkMsec = 0;
			long totalRunMsec = 0;
			long totalVehicleMsec = 0;
			
			for (int i = 1;i<l.size();i++)
			{
				
				List<SmallSection> l2 = l.subList(0, i);
				
				SmallSection section =  l2.get(l2.size()-1);
				if ((i==l.size()-1)||(lastTimestamp>-1 && lastTimestamp+(1000*60*5)<section.getStartTime()))
				{
					// switch event
					float travelledMin = Util.getMinDistance(lastLocation, firstLocation);
					System.out.println("Total travel (minimum, straight line): "+travelledMin+" meters");
					System.out.println("Total travel (points): "+eventTravel+" meters");
					System.out.println("Walk time 	: "+walkMsec/(1000*60));
					System.out.println("Run time  	: "+runMsec/(1000*60));
					System.out.println("Vehicle time: "+vehicleMsec/(1000*60));
					System.out.println("********************************");
					firstLocation = null;
					lastLocation = null;
					walkMsec = 0;
					runMsec = 0;
					vehicleMsec = 0;
					totalTravel +=eventTravel;
					if(summaryLocations.size()>1)
					{
						String summarymapText = createRouteMap(allLocations, allLocations,false);
						String filename = Util.datetimeFormat.format(summaryLocations.get(0).getTime())+" "+eventTravel+" meters_noroute.html";
						File f = new File(directory+File.separator+filename);
						FileWriter fw = new FileWriter(f);
						fw.write(summarymapText);
						fw.close();
						
						summarymapText = createRouteMap(summaryLocations, allLocations,true);
						filename = Util.datetimeFormat.format(summaryLocations.get(0).getTime())+" "+eventTravel+" meters_all.html";
						f = new File(directory+File.separator+filename);
						fw = new FileWriter(f);
						fw.write(summarymapText);
						fw.close();
						
						summarymapText = createRouteMap(summaryLocations, summaryLocations,true);
						filename = Util.datetimeFormat.format(summaryLocations.get(0).getTime())+" "+eventTravel+" meters.html";
						f = new File(directory+File.separator+filename);
						fw = new FileWriter(f);
						fw.write(summarymapText);
						fw.close();
						
					}
						
					eventTravel=0;
					summaryLocations.clear();
					allLocations.clear();
					
					
				}
				
				long fakeCurrentTime = section.getEndTime();
				float smallSpeed = Util.calcAvgSpeedFromSmallSections(l2, 	30, 	60*3,	fakeCurrentTime);
				float medSpeed  = Util.calcAvgSpeedFromSmallSections(l2, 	60*2, 	60*6,	fakeCurrentTime);
				float longSpeed  = Util.calcAvgSpeedFromSmallSections(l2, 	60*5, 	60*10,	fakeCurrentTime); 
				MovementStateManager.get().handleData(section.getGuess(), smallSpeed);
			
				// register first location
				if (firstLocation==null)
				{
					if (section.getLocation()!=null)
					{
						firstLocation = section.getLocation();
						mostDistantLocation = section.getLocation();
						lastAccurateLocation = section.getLocation();
					}
				}
				if (section.getLocation()!=null)
				{
					lastLocation = section.getLocation();
					if (Util.getMinDistance(mostDistantLocation, firstLocation)<(Util.getMinDistance(lastLocation, firstLocation)))
					{
						mostDistantLocation = lastLocation;
					}
					// use inaccurate locations if last accurate is older than 5 mins
					boolean isLastAccurateMeasureTooOld = (lastAccurateLocation.getTime()+(1000*60*5)<lastLocation.getTime());
					
					boolean isNewLocationAccurate = (lastLocation.getAccuracy()<HI_ACCURACY);

					double mindistToLastAccurate = Util.getMinDistance(lastAccurateLocation, lastLocation);
					//use accurate location if last accurate time is older than 2 mins OR 
					boolean shouldUseNewAccurateMeasure = (lastAccurateLocation.getTime()+(1000*60*2)<lastLocation.getTime())||
					//  ... the new location is accurate AND it is longer than 50 meters from the last accurate
														  (isNewLocationAccurate && mindistToLastAccurate>50 );
					
	
					if ((isNewLocationAccurate && shouldUseNewAccurateMeasure) || isLastAccurateMeasureTooOld)
					{
						/*if (lastAccurateLocation.getAccuracy()>=100 || section.getLocation().getAccuracy()>=100)
							//eventTravel+=Util.getMinDistance(lastAccurateLocation,  section.getLocation());
							eventTravel+=lastAccurateLocation.distanceTo(section.getLocation());	
						else
							*/
							
						eventTravel+=lastAccurateLocation.distanceTo(section.getLocation());	
						lastAccurateLocation= lastLocation;
						summaryLocations.add(lastAccurateLocation);
					}
					if (!allLocations.contains(lastLocation))
						allLocations.add(lastLocation);
							
				}
				SummarizedGuess sumGuess = MovementStateManager.get().getSummarizedGuess();
				
				long msec = section.getEndTime()-section.getStartTime();
				switch (sumGuess)
				{
					case WALKING 	: walkMsec+=msec;totalWalkMsec+=msec;break;
					case RUNNING 	: runMsec+=msec;totalRunMsec+=msec;break;
					case VEHICLE    : vehicleMsec+=msec;totalVehicleMsec+=msec;break;
					default			: break;
				}
				String locPrint= "";
				if (lastLocation!=null)
					if (lastPrintedLocation==null || lastPrintedLocation.distanceTo(lastLocation)>0)
					{
						lastPrintedLocation = lastLocation;
						locPrint = "accuracy : "+lastLocation.getAccuracy()+" ("+lastLocation.getProvider()+") "+Util.getMinDistance(lastPrintedLocation, firstLocation)+"m to start" ;
					}
				System.out.println(sumGuess+"\t\t("+l2.get(l2.size()-1).getGuess()+")\t\t"+Util.timeFormat.format(fakeCurrentTime)+" - " +i+" : 1-3 min : "+Util.formatter.format(smallSpeed*3.6)+" kmh \t\t 5-10 min :"+Util.formatter.format(longSpeed*3.6)+" kmh "+MovementStateManager.get()+" "+locPrint);
				lastTimestamp = section.getEndTime();
			}
			System.out.println("*****GRAND TOTAL***************************");
			String result = 
			"Walk time 		:"+totalWalkMsec/(1000*60)+" mins\n"+
			"Run time  		: "+totalRunMsec/(1000*60)+" mins\n"+
			"Vehicle time	: "+totalVehicleMsec/(1000*60)+" mins\n"+
			"Total travel	: "+totalTravel+" meters";
			
			return result;
			
			

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static List<SmallSection> getSmallSectionsFromDirectory(String directory) throws IOException
	{
		List<SmallSection> results = new ArrayList<SmallSection>();
		List<File> allFiles = new ArrayList<File>();
		File root = new File(directory);
		File[] dirs = root.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String filename) {
				if (!filename.contains("."))
				{
					System.out.println(dir+ " "+filename);
					return true;
				}
				return false;
			}
		}
		);
		
		for (File dir : dirs) {
			File[] files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					return filename.endsWith("fft.txt");
				}
			});
			
			for (File file : files) {
				allFiles.add(file);
			}
		}
		
		for (File file : allFiles) {
			Map<String,String> data = getKeyValuesFromFile(file);
			
			MovementGuess guess = MovementGuess.lookup(data.get("GUESS"));
			long startTime = Long.parseLong(data.get("STARTTIME"));
			long endTime = Long.parseLong(data.get("ENDTIME"));
			String locString = data.get("LOCATION");
			GenericLocation l=null;
			if (locString!=null)
			{
				String latString = getSnip("mLatitude", locString);
				String lonString = getSnip("mLongitude", locString);
				String accuracy = getSnip("mAccuracy", locString);
				String provider = getSnip("mProvider", locString);
				String time = getSnip("mTime", locString);
				l = new JavaLocation(provider);
				l.setLatitude(Double.parseDouble(latString));
				l.setLongitude(Double.parseDouble(lonString));
				l.setAccuracy(Float.parseFloat(accuracy));
				l.setTime(Long.parseLong(time));
			}
			SmallSection s = new SmallSection(startTime, endTime, guess, l);
			results.add(s);
		}
		Collections.sort(results, new Comparator<SmallSection>() {

			@Override
			public int compare(SmallSection object1, SmallSection object2) {
				if (object1.getStartTime()>object2.getStartTime())
					return 1;
				if (object1.getStartTime()<object2.getStartTime())
					return -1;
				return 0;
				
			}
		});
		return results;
		
	}

	private static String getSnip(String l, String locString) {
		int p = locString.indexOf(l+"=");
		String tmp = locString.substring(p+l.length()+1);
		return tmp.substring(0,tmp.indexOf(","));
	}

	private static Map<String, String> getKeyValuesFromFile(File file) throws IOException {
		Map<String, String> result = new HashMap<String,String>();
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		while ((line = in.readLine())!=null)
		{
			if (line.contains(":"))
			{
				String key = line.substring(0, line.indexOf(":")).trim();
				String val = line.substring(line.indexOf(":")+1).trim();
				result.put(key, val);
			}
		}
		return result;
 
	}
	
	
	
	
	
}
