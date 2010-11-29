package org.peterstrand.service;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MovementDetector implements SensorEventListener {
	
	private float[] values;
	private float lastX;
	private float lastY;
	private float lastZ;
	private AtomicInteger index = new AtomicInteger(0);
	private int collectionSize = 0;
	private AtomicBoolean isDone = new AtomicBoolean(false);
	private SensorManager sensorManager;
	
	public MovementDetector(SensorManager sensorManager, int collectionSize){
		super();
		this.collectionSize=collectionSize;
		this.sensorManager = sensorManager;
		values = new float[collectionSize];
	}
	
	public void restartMeasuring()
	{
		index.set(0);
		isDone.set(false);
	}
	

	public boolean isDeviceBeingMoved(){
		Runnable r = new Runnable(){

			@Override
			public void run() {
				sensorManager.registerListener(MovementDetector.this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
			}};
			
		Runnable r1 = new Runnable(){

				@Override
				public void run() {

					while(!isDone.get())
					{
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					this.notifyAll();
				}};
   		Thread t = new Thread(r);
   		t.start();	
				
   		Thread t1 = new Thread(r1);
   		t1.start();
			
		sensorManager.unregisterListener(this);
		synchronized(r1)
		{
			try {
				r1.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		float sum = 0.0f;
		int j=0;
		for (int i=3;i<collectionSize;i++)
		{
			sum=sum+values[i];
			j++;
		}
		float avg = sum/j;
		return avg>0.3;
		
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
		
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		if (isDone.get())
			return;
		
		if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	    	 float diffX = Math.abs(sensorEvent.values[0]-lastX);
	    	 float diffY = Math.abs(sensorEvent.values[1]-lastY);
	    	 float diffZ = Math.abs(sensorEvent.values[2]-lastZ);
	    	 float theVal = diffX+diffY+diffZ;
	    	 int idx = index.getAndIncrement();
	    	 values[idx] = theVal;
	  
	    	 if (idx==collectionSize)
	    		 isDone.set(true);
		}
		
	}
	
}
