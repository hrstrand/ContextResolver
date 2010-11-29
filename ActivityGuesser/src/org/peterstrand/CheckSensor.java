package org.peterstrand;

import java.util.Calendar;

import org.peterstrand.R.id;
import org.peterstrand.activities.Preferences;
import org.peterstrand.service.Util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

public class CheckSensor extends Activity implements OnDateSetListener{
	
	

	ProgressDialog				pd;
	private String				analyzeResult;
	private Handler				handler				= new Handler() {
														
														public void handleMessage(Message msg) {
															pd.dismiss();
															AlertDialog alertDialog = new AlertDialog.Builder(CheckSensor.this).create();
															alertDialog.setTitle("analyzer result");
															alertDialog.setMessage(analyzeResult);
															alertDialog.show();
														}
														
													};
	
	private void analyzeDate(final String todayDate) {
		pd = ProgressDialog.show(CheckSensor.this, "Working..", "analyzing " + todayDate, true, false);
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				String result = "";
				try {
					result = FttTest.analyzeDate("/sdcard/contextresolver/" + todayDate);
				} catch (Exception e)
				{
					result = "failed to get data";
				}
				analyzeResult = todayDate+"\n"+result;
				handler.sendEmptyMessage(0);
			}
		});
		
		thread.start();
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		TextView header = (TextView) findViewById(id.header);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.introanim);
		Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.introanim2);
		header.startAnimation(anim);
		
		Button analyzeButton = (Button) findViewById(id.analyzebutton);
		analyzeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final String todayDate = Util.dateFormat.format(System.currentTimeMillis());
				
				analyzeDate(todayDate);
				

				
			}


		}
				);
		Button pbutton = (Button) findViewById(id.prefbutton);
		
		pbutton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(CheckSensor.this, Preferences.class));
				
			}
			
		});
		
		Button pickDateButton = (Button) findViewById(id.pickDaybutton);
		pickDateButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Calendar c = Calendar.getInstance();
				int cyear = c.get(Calendar.YEAR);
				int cmonth = c.get(Calendar.MONTH);
				int cday = c.get(Calendar.DAY_OF_MONTH);

				DatePickerDialog dialog =  new DatePickerDialog(CheckSensor.this,CheckSensor.this,cyear, cmonth, cday);
				dialog.show();
			}
			
		});
		analyzeButton.startAnimation(anim2);
		pbutton.startAnimation(anim2);
		pickDateButton.startAnimation(anim2);
	}
	
	
	

	@Override
	protected void onResume() {
		super.onResume();
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("org.peterstrand.service.SensorService");
		startService(serviceIntent);
		
	}




	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		String mAdd=monthOfYear+1<10?"0":"";
		String dAdd=dayOfMonth<10?"0":"";
		analyzeDate(year+"-"+mAdd+(monthOfYear+1)+"-"+dAdd+(dayOfMonth));
		
	}
	

	

}