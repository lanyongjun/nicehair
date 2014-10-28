package com.lan.nicehair.app;

import java.util.LinkedList;

import android.app.Activity;
import android.app.Application;

import com.lan.nicehair.utils.BuildConfig;

public class InitApplication extends Application {
	private static final String TAG = "InitApplication";
	private static boolean isLogged = false;     
	private static InitApplication instance;
	private static LinkedList<Activity> activityList;
	private Activity activity;
	@Override
	public void onCreate()
	{
		instance = this;
		super.onCreate();
		if (!BuildConfig.DEBUG) {
			/* 全局异常崩溃处理 */
			CrashHandler catchExcep = new CrashHandler(this);
			Thread.setDefaultUncaughtExceptionHandler(catchExcep);  
		}
		activityList = new LinkedList<Activity>();
		super.onCreate();
	}
	public Activity getActivity() {
		return activity;
	}
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	public static InitApplication getInstance()
	{
		return instance;
	}
	/** 
	 * Activity关闭时，删除Activity列表中的Activity对象*/  
	public void removeActivity(Activity a){  
		activityList.remove(a);  
	}  
	
	/** 
	 * 向Activity列表中添加Activity对象*/  
	public void addActivity(Activity a){  
		activityList.add(a);  
	}  
	
	/** 
	 * 关闭Activity列表中的所有Activity*/  
	public void finishActivity(){  
		for (Activity activity : activityList) {    
			if (null != activity) {    
				activity.finish();    
			}    
		}
		activityList.clear();
		//杀死该应用进程  
		android.os.Process.killProcess(android.os.Process.myPid());    
	}
	public static boolean isLogged() {
		return isLogged;
	}
	public static void setLogged(boolean isLogged) {
		InitApplication.isLogged = isLogged;
	}   

}
