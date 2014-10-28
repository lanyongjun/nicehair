package com.lan.nicehair.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

public class AppManager {

	/**
	 * Holds the single instance of a ScrrenManager that is shared by the
	 * process.
	 */
	private static AppManager instance;

	public static AppManager Instance() {
		if (instance==null) {
			instance=new AppManager();
		}
		return instance;
	}

	public AppManager() {
		instance = this;
	}

	public static void updateUI(Handler uiHandler, int what) {
		updateUI(uiHandler, what, 0);
	}

	public static void updateUI(Handler uiHandler, int what, int arg1) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		if(null != uiHandler){
			uiHandler.sendMessage(msg);
		}
	}

	// 跳转系统的网络设置界面
	public void showNetSetting(Activity activity) {
		Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
		intent.addCategory("android.intent.category.DEFAULT");
		activity.startActivity(intent);
	}
}