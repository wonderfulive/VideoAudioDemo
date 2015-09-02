package cn.net.xyd.videoaudiodemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 调用android原生api录制视频
 * Created by Administrator on 2015/8/12 0012.
 */
public class VideoRecordActivity extends Activity implements
        SurfaceHolder.Callback, View.OnClickListener {
    private final static String TAG = "VideoRecordActivity";
    private Button videoRecord;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    private int recLen = 0;
    private Timer timer = new Timer(true);
    private TimerTask timerTask;
    private TextView recTime;
    private PowerManager.WakeLock mWakeLock;
    private final static String CLASS_LABEL = "VideoRecordActivity";

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:

                    recTime.setText(++recLen + "秒");
                    if (recLen >= 30) {
                        if (timerTask != null)
                            timerTask.cancel();
                        startRecord();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_layout);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, CLASS_LABEL);
        mWakeLock.acquire();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, CLASS_LABEL);
            mWakeLock.acquire();
        }
        recLen = 0;
        recTime.setText(recLen + "秒");
        videoRecord.setBackgroundResource(R.mipmap.video_record_icon);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder(); // if you are using MediaRecorder, release it
        // first
        releaseCamera(); // release the camera immediately on pause event
        reset();
    }

    private void reset() {
        if (timerTask != null)
            timerTask.cancel();
        isRecording = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void initView() {
        recTime = (TextView) findViewById(R.id.time_text);
        videoRecord = (Button) findViewById(R.id.button_capture);
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);
       /* if (checkCameraHardware(this)) {
            Toast.makeText(this, "no camera on this device", Toast.LENGTH_SHORT).show();
            finish();
        }*/
        videoRecord.setOnClickListener(this);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void initCameraParam() {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            parameters.setRotation(90);
            // 设置保存的方向
            //parameters.set("rotation", 360);
            mCamera.setParameters(parameters);
            // 设置surfaceview的方向
            mCamera.setDisplayOrientation(0);
        }
    }

    private void stopCamera() {
        if (mCamera != null) {
            try {
                /* 停止预览 */
                mCamera.stopPreview();
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_capture:
                startRecord();
                videoRecord.setBackgroundResource(R.mipmap.stop_record_icon);
                break;
            default:
                break;
        }

    }

    private void startRecord() {
        if (isRecording) {
            if (mMediaRecorder != null) {
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setPreviewDisplay(null);
            }
            try {
                // stop recording and release camera
                mMediaRecorder.stop(); // stop the recording
            } catch (IllegalStateException e) {
                Log.w(TAG, "stopRecord", e);
            } catch (RuntimeException e) {
                Log.w(TAG, "stopRecord", e);
            } catch (Exception e) {
                Log.w(TAG, "stopRecord", e);
            }
            releaseMediaRecorder(); // release the MediaRecorder object

            // inform the user that recording has stopped

            isRecording = false;
            videoRecord.setEnabled(true);
            Intent intent = new Intent();
            intent.setData(Uri.parse(filePath));
            setResult(RESULT_OK, intent);
            this.finish();
        } else {
            Thread prepareVideo = new Thread(new Runnable() {

                @Override
                public void run() {
                    // initialize video camera
                    if (prepareVideoRecorder()) {
                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording
                        mMediaRecorder.start();
                        if (timer == null)
                            timer = new Timer(true);
                        timer.schedule(timerTask = new MyTimerTask(), 1000, 1000);
                        // inform the user that recording has started
                        // setCaptureButtonText("Stop");
                        isRecording = true;
                    } else {
                        // prepare didn't work, release the camera
                        releaseMediaRecorder();
                        // inform user
                    }
                }
            });
            prepareVideo.start();
            try {
                prepareVideo.join();
            } catch (Exception e) {
            }
            videoRecord.setEnabled(true);
        }
    }

    private String filePath = null;

    private boolean prepareVideoRecorder() {
        // mCamera = getCameraInstance();
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        // Step 1: Unlock and set camera to MediaRecorder
        if (mCamera != null) {
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
        }

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        //设置视频输出的格式和编码
        CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        //                mMediaRecorder.setProfile(mProfile);
        mMediaRecorder.setVideoSize(640, 480);//after setVideoSource(),after setOutFormat()
        mMediaRecorder.setAudioEncodingBitRate(44100);
        if (mProfile.videoBitRate > 2 * 1024 * 1024)
            mMediaRecorder.setVideoEncodingBitRate(2 * 1024 * 1024);
        else
            mMediaRecorder.setVideoEncodingBitRate(mProfile.videoBitRate);
        mMediaRecorder.setVideoFrameRate(mProfile.videoFrameRate);//after setVideoSource(),after setOutFormat()

        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//after setOutputFormat()
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//after setOutputFormat()
        // Step 4: Set output file

        filePath = Utils
                .getOutputMediaFile(Utils.MEDIA_TYPE_VIDEO).getAbsolutePath()
                .toString();
        mMediaRecorder.setOutputFile(filePath);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(90);
        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG,
                    "IllegalStateException preparing MediaRecorder: "
                            + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }


    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset(); // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            if (mCamera != null) {
                mCamera.lock(); // lock camera for later use
            }
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        mHolder = holder;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        mCamera = Utils.getCameraInstance();
        initCameraParam();
        // The Surface has been created, now tell the camera where to draw the
        // preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            mCamera.release();
        } catch (Exception e) {
            mCamera.release();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
