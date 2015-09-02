package cn.net.xyd.videoaudiodemo.convert;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import cn.net.xyd.videoaudiodemo.R;

/**
 * 使用LAME将录制的wav转成MP3，存储空间只有wav的1/10
 * Created by Administrator on 2015/8/27 0027.
 */
public class MainActivity extends Activity implements LameConvert.OnProcessCallback{
    static {
        System.loadLibrary("mp3lame");
    }
    private ProgressDialog pd;
    private LameConvert lameConvert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convert_layout);
        pd = new ProgressDialog(this);
        lameConvert = new LameConvert(this);
    }

    /**
     * wav转换mp3
     */
    public void convert(View view) {
        final String wavPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FinalAudio.wav";
        final String mp3Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FinalAudio.mp3";
        File file = new File(wavPath);
        if (file.exists()) {
            int size = (int) file.length();
            System.out.println("文件大小 " + size);
            if ("".equals(mp3Path) || "".equals(wavPath) || size == 0) {
                Toast.makeText(MainActivity.this, "路径不能为空", Toast.LENGTH_LONG).show();
                return;
            }
            pd.setMessage("转换中....");
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMax(size); // 设置进度条的最大值
            pd.setCancelable(false);
            pd.show();
            // 转码是个耗时的操作，所以这里需要开启新线程去执行
            new Thread() {

                @Override
                public void run() {
                    lameConvert.convertmp3(wavPath, mp3Path);
                    pd.dismiss();
                }

            }.start();
        }
    }


    /**
     * 设置进度条的进度，提供给C语言调用
     *
     * @param progress
     */
    /*public void setConvertProgress(int progress) {
        pd.setProgress(progress);
    }*/

    /**
     * 获取LAME的版本号
     */
    public void getVersion(View view) {
        Toast.makeText(MainActivity.this, lameConvert.getLameVersion(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void callback(int process) {
        pd.setProgress(process);
    }
}
