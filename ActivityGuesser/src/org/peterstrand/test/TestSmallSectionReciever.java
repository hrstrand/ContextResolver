package org.peterstrand.test;

import java.util.Locale;

import org.peterstrand.service.SensorService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

public class TestSmallSectionReciever  extends BroadcastReceiver {
	private Object waitForMe = new Object();
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(SensorService.LOGID,"TestSmallSectionReciever onRecieve "+intent);

	}

}
