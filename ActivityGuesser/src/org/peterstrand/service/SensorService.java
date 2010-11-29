package org.peterstrand.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.peterstrand.FastFourierTransform;
import org.peterstrand.GenericLocation;
import org.peterstrand.MovementGuess;
import org.peterstrand.R;
import org.peterstrand.SmallSectionResolver;
import org.peterstrand.test.TestSmallSectionReciever;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

public class SensorService extends Service implements SensorEventListener, LocationListener {
	public static final String	LOGID	= "ContextResolver";
	
	private enum RecorderState {
		STARTING, RECORDING, STOPPING
	}
	
	private RecorderState		state;
	static final int			SMALL_SECTIONS_IN_LARGE		= 10;
	static final int			KEEP_SMALLSECTIONS			= 200;
	
	// last values recorded
	private float				lastX;
	private float				lastY;
	private float				lastZ;
	
	private WakeLock			wakeLock;
	private float[]				rawValues					= new float[FastFourierTransform.SMALLSECTION_SIZE];
	// private List<SmallSection> smallSections = new ArrayList<SmallSection>();
	private List<LargeSection>	largeSections				= new ArrayList<LargeSection>();
	private long				lastSmallSectionStartTime	= -1;
	private int					rawIndex;
	private SensorManager		sensorManager;
	private LocationManager		locationManager;
	private SharedPreferences	preferences;
	private long				lastGpsTrackerStart			= -1;
	private long				lastGoodLocationTime		= -1;
	private MovementSession		currentSession;
	
	public static void scheduleWakeup(long msecsFromNow, Context context) {
		scheduleWakeup(msecsFromNow, context, false);
	}
	
	public int getIdleTimeFromPrefs() {
		String val = preferences.getString("idleTimePref", "10");
		return Integer.parseInt(val);
	}
	
	public int getLocationWaitTimeFromPrefs() {
		String val = preferences.getString("locationTimePref", "60");
		return Integer.parseInt(val);
	}
	
	public boolean getUseLocationFromPrefs() {
		boolean val = preferences.getBoolean("useLocationPref", true);
		return val;
	}
	
	public boolean getUseGpsLocationFromPrefs() {
		return false;
		/*
		boolean val = preferences.getBoolean("useLocationPrefGps", true);
		return val;
		*/
	}
	
	public static void scheduleWakeup(long msecsFromNow, Context context, boolean forceStart) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean runWatcher = preferences.getBoolean("runWatcherPref", false);
		if (runWatcher || forceStart) {
			Log.i(LOGID, "schedule wakeup in " + msecsFromNow + " msecs");
			
			AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent i = new Intent(context, SensorServiceWakeupReciever.class);
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
			
			mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + msecsFromNow, pi);
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return myRemoteServiceStub;
	}
	
	private void changeState(RecorderState newState) {
		Log.i(LOGID, "changing state from " + state + " to " + newState);
		this.state = newState;
	}
	
	private ISensorService.Stub	myRemoteServiceStub	= new ISensorService.Stub() {
														public int getStatusCode() throws RemoteException {
															return 0;
														}
														
													};
	
	// .....
	@Override
	public void onCreate() {
		super.onCreate();
		changeState(RecorderState.STARTING);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		// Toast.makeText(this, "SensorService Created",
		// Toast.LENGTH_LONG).show();
		Log.i(LOGID, "SensorService Created");
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorService wakelock");
		wakeLock.acquire();
		
		int icon = R.drawable.icon; // icon from resources
		CharSequence tickerText = "ContextResolver"; // ticker-text
		long when = System.currentTimeMillis(); // notification time
		Context context = getApplicationContext(); // application Context
		CharSequence contentTitle = "ContextResolver"; // expanded message title
		CharSequence contentText = "Tracking your every move ..."; // expanded
		// message
		// text
		
		Intent notificationIntent = new Intent(this, SensorService.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		
		// the next two lines initialize the Notification, using the
		// configurations above
		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(42, notification);
		currentSession = null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(42);
		
		Log.i(LOGID, "SensorService destroyed");
		sensorManager.unregisterListener(this);
		locationManager.removeUpdates(this);
		wakeLock.release();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Log.i(LOGID, "SensorService onStart");
		super.onStart(intent, startId);
		currentSession = null;
		
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		try {
			
			// STOP gps tracker if it has been running for more than 3 minutes
			if (lastGpsTrackerStart > 0 && lastGpsTrackerStart < System.currentTimeMillis() - 1000 * 60 * 3) {
				stopGpsTracker();
			}
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				if (lastSmallSectionStartTime == -1)
					lastSmallSectionStartTime = System.currentTimeMillis();
				
				float diffX = Math.abs(sensorEvent.values[0] - lastX);
				float diffY = Math.abs(sensorEvent.values[1] - lastY);
				float diffZ = Math.abs(sensorEvent.values[2] - lastZ);
				float theVal = diffX + diffY + diffZ;
				
				rawValues[rawIndex] = theVal;
				rawIndex++;
				
				// check to see if we are at a data capture boundary
				if (rawIndex == FastFourierTransform.SMALLSECTION_SIZE) {
					handleNewSmallSection();
					
					if (state == RecorderState.STOPPING) {
						// write back session data HERE :
						
						scheduleWakeup(getIdleTimeFromPrefs() * 1000, this);
						stopSelf();
					}
				}
				
				lastX = sensorEvent.values[0];
				lastY = sensorEvent.values[1];
				lastZ = sensorEvent.values[2];
				
				// int s = Integer.parseInt("gylle");
			}
		} catch (Exception e) {
			Log.i(LOGID, "failed to get sensor data ", e);
			
			Log.i(LOGID, "stack trace is " + Log.getStackTraceString(e));
			File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "contextresolver" + File.separator + "/exceptions");
			dir.mkdirs();
			File f = new File(Environment.getExternalStorageDirectory() + File.separator + "contextresolver" + File.separator + "/exceptions/" + System.currentTimeMillis() + ".txt");
			FileWriter fw;
			try {
				fw = new FileWriter(f);
				fw.write("exception is " + e == null ? "" : e.toString());
				fw.write("stack trace is " + Log.getStackTraceString(e));
				fw.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
	}
	
	private void stopGpsTracker() {
		if (lastGpsTrackerStart > 0) {
			locationManager.removeUpdates(this);
			if (getUseLocationFromPrefs())
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, getLocationWaitTimeFromPrefs()*1000, 10, this);
			lastGpsTrackerStart = -1;
		}
	}
	
	private void handleNewSmallSection() throws IOException {
		SmallSectionResolver ssr = SmallSectionResolver.createSectionResolver(rawValues);
		MovementGuess latestGuess = ssr.calculateMovement();
		Log.i(LOGID, "movementguess is " + latestGuess);
		rawIndex = 0;
		
		
		// float speed = calcAvgSpeedFromSmallSections(smallSections);
		long now = System.currentTimeMillis();
		SmallSection smallSection = new SmallSection(lastSmallSectionStartTime, now, latestGuess, new LocationWrapper(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)));
		lastSmallSectionStartTime = now + 1;
		// smallSections.add(smallSection);
		
		float smallSpeed = currentSession==null?0.0f:currentSession.calcAvgSpeed(30, 60 * 3, System.currentTimeMillis());
		MovementStateManager.get().handleData(smallSection.getGuess(), smallSpeed);
		
		broadcastGuess(latestGuess, MovementStateManager.get().getSummarizedGuess());
		
		
		boolean stopSelf = false;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean runWatcher = preferences.getBoolean("runWatcherPref", false);
		if (!runWatcher) {
			Log.i(LOGID, "stopping service due to preferences");
			stopSelf = true;
			
		}
		// check for idleness
		if (currentSession == null && latestGuess == MovementGuess.IDLE) {
			// first is service start-idle. If the service has just started and
			// the device is idle, go ahead and kill the service again.
			Log.i(LOGID, "stopping service due to idleness");
			scheduleWakeup(getIdleTimeFromPrefs() * 1000, this);
			stopSelf = true;
			
		}
		
		// start location tracker if needed
		if (currentSession == null && latestGuess != MovementGuess.IDLE) {
			Log.i(LOGID, "starting location tracker");
			if (getUseLocationFromPrefs())
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, getLocationWaitTimeFromPrefs()*1000, 10, this);
			
			currentSession = SessionManager.createNewSession(this);
			changeState(RecorderState.RECORDING);
		}
		
		// only write if the smallsection is either not idle or we had others
		// non-idle in same current
		if (currentSession != null) {
			currentSession.addSmallSectionResult(smallSection, ssr);
			if (currentSession.getState() == SessionState.STOP)
				stopSelf = true;
		}
		
		if (stopSelf)
			changeState(RecorderState.STOPPING);
		
	}
	
	private void broadcastGuess(MovementGuess smallGuess, SummarizedGuess summarizedGuess) {
		Intent smallGuessIntent = new Intent();
		smallGuessIntent.setAction("org.peterstrand.movement.SMALL");
		smallGuessIntent.putExtra("guess", smallGuess.toString());
		Log.i(LOGID, "broadcasting intent " + smallGuessIntent);
		sendBroadcast(smallGuessIntent);
		
		Intent ongoingGuessIntent = new Intent();
		ongoingGuessIntent.setAction("org.peterstrand.movement.ONGOING");
		ongoingGuessIntent.putExtra("guess", summarizedGuess.toString());
		Log.i(LOGID, "broadcasting intent " + ongoingGuessIntent);
		sendBroadcast(ongoingGuessIntent);
		
		
		
	}
	
	// *******************************************************************************************
	// location API callbacks
	// *******************************************************************************************
	@Override
	public void onLocationChanged(Location location) {
		Log.i(LOGID, "location changed " + location.toString());
		
		// start GPS tracker if we get bad accuracy
		if (location.getAccuracy() > 200) {
			if (getUseGpsLocationFromPrefs()) {
				// if gps tracker is NOT running and it has been 10 minutes or
				// more since last accurate location, start GPS tracker
				if (lastGpsTrackerStart == -1 && lastGoodLocationTime < System.currentTimeMillis() - 1000 * 60 * 10L) {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, getLocationWaitTimeFromPrefs()*1000, 10, this);
					lastGpsTrackerStart = System.currentTimeMillis();
				}
			}
		} else {
			lastGoodLocationTime = System.currentTimeMillis();
			stopGpsTracker();
		}
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	public Location getLastKnownLocation() {
		return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	}
	
}
