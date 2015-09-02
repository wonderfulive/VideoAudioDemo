package com.javacv.recorder;

import android.os.Environment;

public class CONSTANTS {
	
	public final static String METADATA_REQUEST_BUNDLE_TAG = "requestMetaData";
	/**
	 * 视频文件名称开头
	 */
	public final static String FILE_START_NAME = "VMS_";
	/**
	 * 视频文件扩展名
	 */
	public final static String VIDEO_EXTENSION = ".mp4";
	/**
	 * DCIM文件夹
	 */
	public final static String DCIM_FOLDER = "/xinyidai";
	/**
	 * 视频存放相对路径
	 */
	public final static String CAMERA_FOLDER = "/video";
	/**
	 * 临时目录
	 */
	public final static String TEMP_FOLDER = "/Temp";
	/**
	 * 视频存放绝对路径
	 */
	public final static String CAMERA_FOLDER_PATH = Environment.getExternalStorageDirectory().toString() + CONSTANTS.DCIM_FOLDER + CONSTANTS.CAMERA_FOLDER;
	/**
	 * 视频存放临时路径（一个视频文件，一个音频文件）
	 */
	public final static String TEMP_FOLDER_PATH = Environment.getExternalStorageDirectory().toString() + CONSTANTS.DCIM_FOLDER + CONSTANTS.CAMERA_FOLDER + CONSTANTS.TEMP_FOLDER;
	
	public final static String  VIDEO_CONTENT_URI = "content://media/external/video/media";
	
	public final static String KEY_DELETE_FOLDER_FROM_SDCARD = "deleteFolderFromSDCard";
	
	public final static String  RECEIVER_ACTION_SAVE_FRAME = "com.javacv.recorder.intent.action.SAVE_FRAME";
	public final static String  RECEIVER_CATEGORY_SAVE_FRAME = "com.javacv.recorder";
	public final static String  TAG_SAVE_FRAME = "saveFrame";
	
	public final static int RESOLUTION_HIGH = 1300;
	public final static int RESOLUTION_MEDIUM = 500;
	public final static int RESOLUTION_LOW = 180;
	
	public final static int RESOLUTION_HIGH_VALUE = 2;
	public final static int RESOLUTION_MEDIUM_VALUE = 1;
	public final static int RESOLUTION_LOW_VALUE = 0;
}
