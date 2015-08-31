package cn.net.xyd.videoaudiodemo.mp3lame;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import cn.net.xyd.videoaudiodemo.R;

public class MainActivity extends Activity {
private boolean isRecording;
private String filePath;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mp3_lame);
		filePath = getOutputMediaFilePath();
		final Mp3Recorder recorder = new Mp3Recorder(filePath);
		
		(findViewById(R.id.record)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					if(!isRecording) {
						recorder.startRecording();
						isRecording = true;
					}else{
						recorder.stopRecording();
						isRecording = false;
					}
				} catch(IOException e) {
					Log.d("MainActivity", "Start error");
				}				
			}
		});
		
		(findViewById(R.id.stop)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					recorder.stopRecording();	
				} catch(IOException e) {
					Log.d("MainActivity", "Stop error");
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	public String getOutputMediaFilePath() {
		String externalPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		File directory = new File(externalPath + "/" + "xinyidai/audio");
		if (!directory.exists()) {
			directory.mkdirs();
		}
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String  mediaFilePath = directory.getPath() + File.separator + "AUDIO_" + timeStamp + ".mp3";

		return mediaFilePath;
	}
}
