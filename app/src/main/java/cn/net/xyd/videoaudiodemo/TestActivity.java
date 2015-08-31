package cn.net.xyd.videoaudiodemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by Administrator on 2015/8/12 0012.
 */
public class TestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.video_btn:
                TestActivity.this.startActivityForResult(new Intent(TestActivity.this,VideoRecordActivity.class),REQUEST_CODE_RECORD_VIDEO);
                break;
            case R.id.audio_btn:
                TestActivity.this.startActivityForResult(new Intent(TestActivity.this,MyAudioRecordActivity.class),REQUEST_CODE_RECORD_AUDIO);
                break;
        }
    }
private static final int REQUEST_CODE_RECORD_VIDEO = 1001;
    private static final int REQUEST_CODE_RECORD_AUDIO = 1002;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode == REQUEST_CODE_RECORD_VIDEO){
                Log.d("video", data.getData().toString());
            }else if(requestCode == REQUEST_CODE_RECORD_AUDIO){
                Log.d("audio",data.getData().toString());
            }
        }
    }
}
