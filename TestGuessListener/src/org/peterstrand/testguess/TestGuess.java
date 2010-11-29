package org.peterstrand.testguess;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class TestGuess extends Activity implements OnInitListener {

	private enum SpeakMode { ONGOING, SMALL, NONE }; 
	private TextToSpeech mTts;
	private String lastSmallSaid ="";
	private String lastOngoingSaid ="";
	
	private ToggleButton currentToggle;
	private ToggleButton ongoingToggle;
	
	private SpeakMode speakmode = SpeakMode.NONE;
    private final BroadcastReceiver broadCastRecSmall = new BroadcastReceiver()
    {

		@Override
		public void onReceive(Context context, Intent intent) {
		
	        TextView smallText   = (TextView)findViewById(R.id.smallText);
	        TextView ongoingText = (TextView)findViewById(R.id.ongoingText);
			
			if (intent.getAction().equals("org.peterstrand.movement.SMALL"))
			{
				String guess = intent.getExtras().getString("guess");
				smallText.setText("-");
				Log.i("gylle","GOT SMALL: "+intent.getExtras().getString("guess"));
				if (guess.equals("RUNNING"))
				{
					if (speakmode == SpeakMode.SMALL)
					{
						saySomething("running");
						lastSmallSaid = "running";
					}
					smallText.setText("Running");
				}
				
				if (guess.equals("WALKING"))
				{
					if (speakmode == SpeakMode.SMALL)
					{
						saySomething("walking");
						lastSmallSaid = "walking";
					}
					smallText.setText("walking");
				}
				
				if (guess.equals("IDLE"))
				{
					if (speakmode == SpeakMode.SMALL)
					{
						if (!lastSmallSaid.equals("idle"))	
							saySomething("idle");
						lastSmallSaid ="idle";
					}
					
				}
			}
			
			if (intent.getAction().equals("org.peterstrand.movement.ONGOING"))
			{
				String guess = intent.getExtras().getString("guess");
				Log.i("gylle","GOT ONGOING: "+intent.getExtras().getString("guess"));
				if (guess.equals("RUNNING"))
				{
					if (speakmode == SpeakMode.ONGOING)
					{
						if (!lastOngoingSaid.equals("running"))
							saySomething("running");
						lastOngoingSaid = "running";
					}
					ongoingText.setText("running");
				}
				
				if (guess.equals("WALKING"))
				{
					if (speakmode == SpeakMode.ONGOING)
					{
						if (!lastOngoingSaid.equals("walking"))
							saySomething("walking");
						lastOngoingSaid = "walking";
					}
					ongoingText.setText("walking");
				}
				
				if (guess.equals("VEHICLE"))
				{
					if (speakmode == SpeakMode.ONGOING)
					{
						if (!lastOngoingSaid.equals("driving"))
							saySomething("driving");
						lastOngoingSaid = "driving";
					}
					ongoingText.setText("driving");
				}
								
				if (guess.equals("IDLE"))
				{
					if (speakmode == SpeakMode.ONGOING)
					{
						if (!lastOngoingSaid.equals("idle") && !	lastOngoingSaid.equals(""))	
							saySomething("stopped "+lastOngoingSaid);
						lastOngoingSaid ="idle";
					}
					ongoingText.setText("-");
				}
			}
			
			
			//saySomething(intent.getExtras().getString("guess"));
		}
    };

    private void saySomething(String something) {

        mTts.speak(something,
            TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
            null);
      
    }

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mTts = new TextToSpeech(this,
                this  // TextToSpeech.OnInitListener
                );
        
        setContentView(R.layout.main);
        currentToggle = (ToggleButton)findViewById(R.id.smallToggleButton);
        ongoingToggle = (ToggleButton)findViewById(R.id.ongoingToggleButton);
        
        currentToggle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				speakmode = SpeakMode.SMALL;
				ongoingToggle.setChecked(false);
				
			}
		});
        
        ongoingToggle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				speakmode = SpeakMode.ONGOING;
				currentToggle.setChecked(false);
				
			}
		});
        
        IntentFilter intentFilter = new IntentFilter("org.peterstrand.movement.SMALL");
        intentFilter.addAction("org.peterstrand.movement.ONGOING");
        registerReceiver(broadCastRecSmall, intentFilter);   
    }

	@Override
	protected void onDestroy() {
		unregisterReceiver(broadCastRecSmall);
		super.onDestroy();
	}

	@Override
	public void onInit(int status) {
		//saySomething("hello there!");
		
	}
    
    
}