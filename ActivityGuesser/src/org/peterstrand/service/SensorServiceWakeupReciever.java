package org.peterstrand.service;

import java.util.concurrent.Future;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

public class SensorServiceWakeupReciever  extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(SensorService.LOGID,"SensorServiceWakeupReciever onRecieve");
		
		Intent serviceIntent = new Intent(); 
		serviceIntent.setAction("org.peterstrand.service.SensorService");
		serviceIntent.putExtra("action", "checkMotion");
		context.startService(serviceIntent);
		
		/*Log.i(SensorService.LOGID,"SensorServiceWakeupReciever onReceive");
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		final WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorService wakelock");
		wl.acquire();
		try{
			SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		//	MovementDetector movDetect = new MovementDetector(sensorManager,50);
		//	boolean isMoving = movDetect.isDeviceBeingMoved();
			// are we moving or idle ?
			if (true)
			{
				Toast.makeText(context, "device active", Toast.LENGTH_LONG).show();
				// we are moving -- tell the service to start detailed sensing
				ServiceConnection conn = new ServiceConnection() {
					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
						ISensorService sensorService = ISensorService.Stub.asInterface(service);
	
						try {
							sensorService.startDetailedSensing();
							wl.release();
						} catch (RemoteException e) {
							Log.e(SensorService.LOGID, e.toString());
						}
					}
	
					@Override
					public void onServiceDisconnected(ComponentName name) {
						// TODO Auto-generated method stub
						
					}
				};
		
	
			}
			else
			{
				Toast.makeText(context, "device idle", Toast.LENGTH_LONG).show();
				SensorService.scheduleWakeup(1000, context);
				wl.release();
			}
		}
		catch (Exception e)
		{
			Log.i(SensorService.LOGID,e.toString());
			wl.release();
		}
		 */
		
	}


}
