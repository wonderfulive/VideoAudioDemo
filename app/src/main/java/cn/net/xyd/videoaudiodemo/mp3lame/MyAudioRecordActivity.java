package cn.net.xyd.videoaudiodemo.mp3lame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.net.xyd.videoaudiodemo.R;
import cn.net.xyd.videoaudiodemo.Utils;
import cn.net.xyd.videoaudiodemo.VolumeView;

/**
 * Created by Administrator on 2015/8/12 0012.
 */
public class MyAudioRecordActivity extends Activity {
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    recLen++;
                    /*int minute = recLen / 60;
                    int second = recLen % 60;
                    String min = minute >= 10 ? minute + "" : "0" + minute;
                    String sec = second >= 10 ? second + "" : "0" + second;
                    recTime.setText(min + ":" + sec);*/
                    recTime.setText(recLen+"秒");
                    if(recLen>=30){
                        try {
                            mRecorder.stopRecording();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    volumeView.changed(msg.what);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private int recLen=0;
    TimerTask task = new TimerTask(){
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };
    private Button mRecordButton = null;
    private Timer timer = new Timer(true);
    private TextView recTime;
    private TextView noticeText;
    private LinearLayout mVolumeLayout;
    private boolean isRecording;
    private String filePath;
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
   private Mp3Recorder mRecorder;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.audio_layout);
        filePath = getOutputMediaFilePath();
        mRecorder = new Mp3Recorder(filePath);
        mRecordButton = (Button)findViewById(R.id.audio_record_btn);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    try {
                        mRecorder.stopRecording();
                        noticeText.setText("录音已停止");
                        noticeText.setTextColor(Color.RED);
                        Intent intent = new Intent();
                        intent.setData(Uri.parse(filePath));
                        setResult(RESULT_OK, intent);
                        MyAudioRecordActivity.this.finish();
                        mRecordButton.setBackgroundResource(R.mipmap.audio_record_icon);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        mRecorder.startRecording();
                        timer.schedule(task, 1000, 1000); //延时1000ms后执行，1000ms执行一次
                        noticeText.setText("正在进行录音……");
                        noticeText.setTextColor(Color.GREEN);
                        mRecordButton.setBackgroundResource(R.mipmap.stop_record_icon);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                isRecording = !isRecording;
            }
        });
        noticeText = (TextView)findViewById(R.id.notice_text);
        recTime = (TextView)findViewById(R.id.time_text);
        mVolumeLayout = (LinearLayout)this.findViewById(R.id.volume_show_id);
        int[] location = new int[2];
        mVolumeLayout.getLocationOnScreen(location);
        volumeView = new VolumeView(this,location[1]+100);
        mVolumeLayout.removeAllViews();
        mVolumeLayout.addView(volumeView);
    }
    private VolumeView volumeView;

    @Override
    protected void onResume() {
        super.onResume();
        if(mRecorder==null){
            new Mp3Recorder(filePath);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
        if (mRecorder != null) {
            try {
                mRecorder.stopRecording();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecorder = null;
        }
    }
}
