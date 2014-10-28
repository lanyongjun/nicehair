package com.lan.nicehair.utils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

public class LPhone {

	Context context;
	public LPhone(Context context){
		this.context = context;
	}
	/**
	 * 判断是否有SD卡或SD卡是否可用。
	 * 
	 * @return
	 */
	public static boolean sdcard() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	/** 
     * 获得SD卡总大小 
     *  
     * @return 
     */  
    public String getSDTotalSize() {  
        File path = Environment.getExternalStorageDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long totalBlocks = stat.getBlockCount();  
        return Formatter.formatFileSize(context, blockSize * totalBlocks);  
    }  
  
    /** 
     * 获得sd卡剩余容量，即可用大小 
     *  
     * @return 
     */  
    public String getSDAvailableSize() {  
        File path = Environment.getExternalStorageDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long availableBlocks = stat.getAvailableBlocks();  
        return Formatter.formatFileSize(context, blockSize * availableBlocks);  
    }  
  
    /** 
     * 获得机身内容总大小 
     *  
     * @return 
     */  
    public String getRomTotalSize() {  
        File path = Environment.getDataDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long totalBlocks = stat.getBlockCount();  
        return Formatter.formatFileSize(context, blockSize * totalBlocks);  
    }  
  
    /** 
     * 获得机身可用内存 
     *  
     * @return 
     */  
    public String getRomAvailableSize() {  
        File path = Environment.getDataDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long availableBlocks = stat.getAvailableBlocks();  
        return Formatter.formatFileSize(context, blockSize * availableBlocks);  
    }  
  //判断email格式是否正确
  	public static boolean isEmail(String email) {
  		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
  		Pattern p = Pattern.compile(str);
  		Matcher m = p.matcher(email);
  		
  		return m.matches();
  	}
  	 public static int dp2px(Context context, float dpValue) {
         final float scale = context.getResources().getDisplayMetrics().density;
         return (int) (dpValue * scale + 0.5f);
     }
     public static int densityDPI(Context context){
     	return context.getResources().getDisplayMetrics().densityDpi;
     }
     /**
 	 * 得到设备屏幕的宽度
 	 */
 	public static int getScreenWidth(Context context) {
 		return context.getResources().getDisplayMetrics().widthPixels;
 	}

 	/**
 	 * 得到设备屏幕的高度
 	 */
 	public static int getScreenHeight(Context context) {
 		return context.getResources().getDisplayMetrics().heightPixels;
 	}
}
