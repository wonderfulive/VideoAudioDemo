package cn.net.xyd.videoaudiodemo.audio;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import cn.net.xyd.videoaudiodemo.R;
import cn.net.xyd.videoaudiodemo.Utils;
import cn.net.xyd.videoaudiodemo.VolumeView;
import cn.net.xyd.videoaudiodemo.convert.LameConvert;

/**
 * Created by Administrator on 2015/8/12 0012.
 */
public class AudioRecordActivity extends Activity implements LameConvert.OnProcessCallback{
    static {
        System.loadLibrary("mp3lame");
    }
    private ProgressDialog pd;
    private LameConvert lameConvert;

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    int mResult = -1;
    private void startRecording() {
        if(mState != -1){
            Message msg = new Message();
            Bundle b = new Bundle();// 存放数据
            b.putInt("cmd",CMD_RECORDFAIL);
            b.putInt("msg", ErrorCode.E_STATE_RECODING);
            msg.setData(b);
            uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
            return;
        }
        AudioRecordFunc mRecord = AudioRecordFunc.getInstance();
        mResult = mRecord.startRecordAndFile();
        mRecord.getVoice(this);
        if (mResult == ErrorCode.SUCCESS) {
            noticeText.setText("正在进行录音……");
            noticeText.setTextColor(Color.GREEN);
            timer.schedule(task, 1000, 1000); //延时1000ms后执行，1000ms执行一次
            mState = 0;
        } else {
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putInt("cmd", CMD_RECORDFAIL);
            msg.setData(bundle);
            uiHandler.sendMessage(msg); // 向Handler发送消息,更新UI
        }


    }
    private int mState = -1;    //-1:没再录制
    private final static int CMD_RECORDING_TIME = 2000;
    private final static int CMD_RECORDFAIL = 2001;
    private final static int CMD_STOP = 2002;
    public final static int CMD_VOICE_VOLUME = 2003;
    public final static int CMD_CONVERT = 2004;

    private void stopRecording() {
        if (mState != -1) {
            AudioRecordFunc mRecord = AudioRecordFunc.getInstance();
            mRecord.stopRecordAndFile();
        }
        Message msg = new Message();
        Bundle b = new Bundle();// 存放数据
        b.putInt("cmd", CMD_STOP);
        b.putInt("msg", mState);
        msg.setData(b);
        uiHandler.sendMessageDelayed(msg, 1000); // 向Handler发送消息,更新UI
        mState = -1;
    }


    public UIHandler uiHandler;

    public class UIHandler extends Handler {
        public UIHandler() {
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d("MyHandler", "handleMessage......");
            super.handleMessage(msg);
            Bundle b = msg.getData();
            int vCmd = b.getInt("cmd");
            switch (vCmd) {
                case CMD_RECORDING_TIME:
                    recLen++;
                    /*int minute = recLen / 60;
                    int second = recLen % 60;
                    String min = minute >= 10 ? minute + "" : "0" + minute;
                    String sec = second >= 10 ? second + "" : "0" + second;
                    recTime.setText(min + ":" + sec);*/
                    recTime.setText(recLen + "秒");
                    if (recLen >= 30) {
                        stopRecording();
                    }
                    break;
                case CMD_RECORDFAIL:
                    noticeText.setText("录音失败");
                    noticeText.setTextColor(Color.RED);
                    break;
                case CMD_STOP:
                    //int vFileType = b.getInt("msg");
                    //AudioRecordFunc mRecord_1 = AudioRecordFunc.getInstance();
                    //long mSize = mRecord_1.getRecordFileSize();
                    final String wavPath = AudioFileFunc.getWavFilePath();

                    noticeText.setText("录音已停止");
                    noticeText.setTextColor(Color.RED);


                    mFileName = Utils.getOutputMediaFile(Utils.MEDIA_TYPE_AUDIO).getAbsolutePath().toString();
                    File file = new File(wavPath);
                    if (file.exists()) {
                        int size = (int) file.length();
                        System.out.println("文件大小 " + size);
                        if ("".equals(mFileName) || "".equals(wavPath) || size == 0) {
                            Toast.makeText(AudioRecordActivity.this, "压缩出错,检查sd卡是否存在", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        pd.setMessage("压缩中....");
                        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        pd.setMax(size); // 设置进度条的最大值
                        pd.setCancelable(false);
                        pd.show();
                        // 转码是个耗时的操作，所以这里需要开启新线程去执行
                        new Thread() {

                            @Override
                            public void run() {
                                lameConvert.convertmp3(wavPath, mFileName);

                                Message msg = new Message();
                                Bundle b = new Bundle();// 存放数据
                                b.putInt("cmd",CMD_CONVERT);
                                msg.setData(b);
                                uiHandler.sendMessage(msg);
                            }

                        }.start();
                    }

                    break;
                case CMD_VOICE_VOLUME:
                    volumeView.changed(b.getInt("msg"));
                    break;
                case CMD_CONVERT:
                    pd.dismiss();
                    Intent intent = new Intent();
                    intent.setData(Uri.parse(mFileName));
                    setResult(RESULT_OK, intent);
                    AudioRecordActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    public void callback(int process) {
        pd.setProgress(process);
    }
    private int recLen = 0;
    TimerTask task = new TimerTask() {
        public void run() {
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putInt("cmd", CMD_RECORDING_TIME);
            message.setData(bundle);
            uiHandler.sendMessage(message);
        }
    };
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    private Button mRecordButton = null;
    private Timer timer = new Timer(true);
    private TextView recTime;
    private boolean mStartRecording = true;
    private TextView noticeText;
    private LinearLayout mVolumeLayout;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.audio_layout);
        mRecordButton = (Button) findViewById(R.id.audio_record_btn);

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
        noticeText = (TextView) findViewById(R.id.notice_text);
        recTime = (TextView) findViewById(R.id.time_text);
        mVolumeLayout = (LinearLayout) this.findViewById(R.id.volume_show_id);
        int[] location = new int[2];
        mVolumeLayout.getLocationOnScreen(location);
        volumeView = new VolumeView(this, location[1] + 100);
        mVolumeLayout.removeAllViews();
        mVolumeLayout.addView(volumeView);

        uiHandler = new UIHandler();

        pd = new ProgressDialog(this);
        lameConvert = new LameConvert(this);
    }

    private VolumeView volumeView;

    @Override
    protected void onResume() {
        super.onResume();
        if(timer==null){
            timer= new Timer(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
        timer.purge();
        timer = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mState!=-1){
            mState = -1;
            AudioRecordFunc.getInstance().stopRecordAndFile();
        }
    }
}
