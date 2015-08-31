package cn.net.xyd.videoaudiodemo;

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
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2015/8/12 0012.
 */
public class MyAudioRecordActivity extends Activity {


    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }



    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mFileName = Utils.getOutputMediaFile(Utils.MEDIA_TYPE_AUDIO).getAbsolutePath().toString();
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mRecordState = MediaRecorderState.RECORDING;
        mRecorder.start();
        timer.schedule(task, 1000, 1000); //延时1000ms后执行，1000ms执行一次
        Thread t = new Thread(new DbThread());
        t.start();
        noticeText.setText("正在进行录音……");
        noticeText.setTextColor(Color.GREEN);

    }

    private void stopRecording() {
        if(mRecorder != null && mRecordState != MediaRecorderState.STOPPED) {
            flag = false;
            mRecorder.stop();
            mRecorder.release();
            timer.cancel(); //退出计时器
            mRecorder = null;
            noticeText.setText("录音已停止");
            noticeText.setTextColor(Color.RED);
            Intent intent = new Intent();
            intent.setData(Uri.parse(mFileName));
            setResult(RESULT_OK, intent);
            this.finish();
        }
    }

    class RecordButton extends Button {
        boolean mStartRecording = true;
        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends Button {
        boolean mStartPlaying = true;
        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

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
                        stopRecording();
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
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    private Button mRecordButton = null;
    private MediaRecorder mRecorder = null;
    private Button mPlayButton = null;
    private MediaPlayer mPlayer = null;
    private Timer timer = new Timer(true);
    private TextView recTime;
    private boolean mStartRecording = true;
    private TextView noticeText;
    private LinearLayout mVolumeLayout;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.audio_layout);
        mRecordButton = (Button)findViewById(R.id.audio_record_btn);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    mRecordButton.setBackgroundResource(R.mipmap.stop_record_icon);
                } else {
                    mRecordButton.setBackgroundResource(R.mipmap.audio_record_icon);
                }
                mStartRecording = !mStartRecording;
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
    private enum MediaRecorderState
    {
        STOPPED, RECORDING, PAUSED
    }
    private boolean flag = true;
    private VolumeView volumeView;
    private MediaRecorderState mRecordState = MediaRecorderState.STOPPED;
    class DbThread implements Runnable {
        @Override
        public void run() {
            while (flag) {
                int db = 0;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mRecorder != null && mRecordState == MediaRecorderState.RECORDING) {
                    try {
                        double f = 10 * Math.log10(mRecorder.getMaxAmplitude());
                        if (f < 0) {
                            db = 0;
                        } else {
                            db = (int) (f * 2);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    Message msg = new Message();
                    msg.what = db;
                    MyAudioRecordActivity.this.handler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.what = db;
                    MyAudioRecordActivity.this.handler.sendMessage(msg);
                    break;
                }
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
