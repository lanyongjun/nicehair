package com.lan.nicehair.utils;

import com.lan.nicehair.R;
import com.lan.nicehair.widget.CustomProgressDialog;

import android.app.ProgressDialog;
import android.content.Context;


public class HProgress {
	private static CustomProgressDialog dialog = null;

	public static void show(Context context, String message) {
		try {
			if(context==null)return;
			if (dialog != null)
				dialog.dismiss();
			dialog = new CustomProgressDialog(context,null);
			// dialog.setCancelable(false);
			dialog.show();
		} catch (Exception e) {

		}
	}

	public static void dismiss() {
		try {
			if (dialog != null)
				dialog.dismiss();
		} catch (Exception e) {

		}
	}
}
