#include "src/lame.h"
#include<stdio.h>
#include<jni.h>
#include<malloc.h>
#include<string.h>
#include<android/log.h>
#include "cn_net_xyd_videoaudiodemo_convert_LameConvert.h"

#define LOG_TAG "System.out.c"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
/**
 * 返回值 char* 这个代表char数组的首地址
 *  Jstring2CStr 把java中的jstring的类型转化成一个c语言中的char 字符串
 */
char* Jstring2CStr(JNIEnv* env,jstring jstr){
  //rtn一块能够存放char类型内存的首地址
  char* rtn = NULL;
  // 找到 jvm里面 String类
  jclass clsstring = (*env)->FindClass(env,"java/lang/String");
  // 创建一个jstring的字符串内容 GB2312
  jstring strencode = (*env)->NewStringUTF(env,"GB2312");
  // 寻找 String类的一个方法 getBytes,getBytes方法的参数 Ljava/lang/String; getBytes 方法的返回值[B
  jmethodID mid = (*env)->GetMethodID(env,clsstring,"getBytes","(Ljava/lang/String;)[B");
  //"String".getBytes("GB2312");
  jbyteArray barr = (jbyteArray)(*env)->CallObjectMethod(env,jstr,mid,strencode);
  //获取到barr的长度
  jsize alen = (*env)->GetArrayLength(env,barr);
  //获取到的jbyte 数据放入到以ba 为开始地址的内存空间中
  jbyte* ba = (*env)->GetByteArrayElements(env,barr,JNI_FALSE);

  if(alen > 0){
    //new char[alen+1]; "\0" 字符串的结束
    rtn = (char*)malloc(alen+1);
    //内存拷贝的操作(目标，源，拷贝数量)
    memcpy(rtn,ba,alen);
    //给字符数组最后一个元素,表示字符串结束
    rtn[alen]=0;
  }
  (*env)->ReleaseByteArrayElements(env,barr,ba,0);
  return rtn;
}
/**
 * 获取LAME的版本号
 */
JNIEXPORT jstring JNICALL Java_cn_net_xyd_videoaudiodemo_convert_LameConvert_getLameVersion
        (JNIEnv * env, jobject obj){
  return (*env)->NewStringUTF(env,get_lame_version());
}
/**
 * 调用java代码 更新程序的进度条
 */
void publishJavaProgress(JNIEnv * env, jobject obj, jint progress) {
  // 1.找到java的MainActivity的class
  jclass clazz = (*env)->FindClass(env, "cn/net/xyd/videoaudiodemo/convert/LameConvert");
  if (clazz == 0) {
    LOGI("can't find clazz");
  }
  LOGI(" find clazz");

  //2 找到class 里面的方法定义
  jmethodID methodid = (*env)->GetMethodID(env, clazz, "setConvertProgress","(I)V");
  if (methodid == 0) {
    LOGI("can't find methodid");
  }
  LOGI(" find methodid");

  //3 .调用方法
  (*env)->CallVoidMethod(env,obj, methodid, progress);
  LOGI(" CallVoidMethod methodid");
  (*env)->DeleteLocalRef(env,clazz);
}
int flag = 0;
/**
 * wav转换mp3
 */
JNIEXPORT void JNICALL Java_cn_net_xyd_videoaudiodemo_convert_LameConvert_convertmp3
(JNIEnv * env, jclass cls, jstring jwav, jstring jmp3){
  //把java的string类型的文件转化成c能识别的char 字符串
  char* cwav = Jstring2CStr(env,jwav);
  char* cmp3 = Jstring2CStr(env,jmp3);
  LOGI("wav = %s",cwav);
  LOGI("mp3 = %s",cmp3);
  //1.打开 wav,MP3文件
  FILE* fwav = fopen(cwav,"rb");
  FILE* fmp3 = fopen(cmp3,"wb");

  short int wav_buffer[8192*2];
  unsigned char mp3_buffer[8192];
  //1.初始化lame的编码器
  lame_t lame = lame_init();
  //2. 设置lame mp3编码的采样率
  lame_set_in_samplerate(lame,44100);
  lame_set_num_channels(lame,2);
  // 3. 设置MP3的编码方式
  lame_set_VBR(lame,vbr_default);
  lame_init_params(lame);
  LOGI("lame init finish");
  int read ; int write; //代表读了多少个次 和写了多少次
  int total=0; // 当前读的wav文件的byte数目
  do{
    if(flag==404){
      return;
    }
    read = fread(wav_buffer,sizeof(short int)*2, 8192,fwav);
    total +=  read* sizeof(short int)*2;
    LOGI("converting ....%d", total);
    publishJavaProgress(env,cls,total);
    // 调用java代码 完成进度条的更新
    if(read!=0){
    write = lame_encode_buffer_interleaved(lame,wav_buffer,read,mp3_buffer,8192);
    //把转化后的mp3数据写到文件里
    fwrite(mp3_buffer,sizeof(unsigned char),write,fmp3);
    }
    if(read==0){
      lame_encode_flush(lame,mp3_buffer,8192);
    }
  }while(read!=0);
  LOGI("convert  finish");

  lame_close(lame);
  fclose(fwav);
  fclose(fmp3);
}

