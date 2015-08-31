package cn.net.xyd.videoaudiodemo.convert;

import android.content.Context;
import android.util.Log;

/**
 * Created by Administrator on 2015/8/27 0027.
 */
public class LameConvert {

    interface OnProcessCallback{
        void callback(int process);
    }
    private OnProcessCallback callback;
    public LameConvert(Context context){
        try {
            callback = (OnProcessCallback) context;
        }catch (ClassCastException e){
            Log.e("LameConvert",context.getClass().getName()+"must be implements the interface of OnProcessCallback");
        }
    }
    /**
     * 设置进度条的进度，提供给C语言调用
     *
     * @param process
     */
    public void setConvertProgress(int process) {
        callback.callback(process);
    }
    /**
     * wav转换成mp3的本地方法
     *
     * @param wav
     * @param mp3
     */
    public native void convertmp3(String wav, String mp3);

    /**
     * 获取LAME的版本信息
     *
     * @return
     */
    public native String getLameVersion();
}
