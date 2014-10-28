/*
 * Copyright (C) 2010 mAPPn.Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lan.nicehair.common.download;

import java.io.File;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.lan.nicehair.R;
import com.lan.nicehair.utils.AppLog;
import com.lan.nicehair.utils.AppToast;
import com.lan.nicehair.utils.BuildConfig;
import com.lan.nicehair.utils.LPhone;
import com.lan.nicehair.utils.Utils;

/**
 * Receives system broadcasts (boot, network connectivity)
 */
public class DownloadReceiver extends BroadcastReceiver {

    private static final String TAG = "DownloadReceiver";

	public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            startService(context);
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo info = (NetworkInfo)
                    intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (info != null && info.isConnected()) {
				startService(context);
			}
        } else if (action.equals(Constants.ACTION_RETRY)) {
            startService(context);
        } else if (action.equals(Constants.ACTION_OPEN)
                || action.equals(Constants.ACTION_LIST)
                || action.equals(Constants.ACTION_HIDE)) {
            handleNotificationBroadcast(context, intent);
        }
    }

    /**
     * Handle any broadcast related to a system notification.
     */
    private void handleNotificationBroadcast(Context context, Intent intent) {
        
        Uri uri = intent.getData();
        String action = intent.getAction();
        
        if (action.equals(Constants.ACTION_OPEN)) {
           AppLog.d(TAG,"Receiver open for " + uri);
        } else if (action.equals(Constants.ACTION_LIST)) {
           AppLog.d(TAG,"Receiver list for " + uri);
        } else { // ACTION_HIDE
           AppLog.d(TAG,"Receiver hide for " + uri);
        }

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return;
        }
        try {
            if (!cursor.moveToFirst()) {
                return;
            }

            if (action.equals(Constants.ACTION_OPEN)) {
                
                int status = intent.getIntExtra(DownloadManager.Impl.COLUMN_STATUS, 
                        DownloadManager.Impl.STATUS_UNKNOWN_ERROR); 
                if (status == DownloadManager.Impl.STATUS_SUCCESS) {
                    // download success
                    openDownload(context, cursor);
                } else {
                    // download failed
                    hideNotification(context, uri, cursor);
                }
            } else if (action.equals(Constants.ACTION_LIST)) {
                sendNotificationClickedIntent(context, intent, cursor);
            } else { // ACTION_HIDE
                hideNotification(context, uri, cursor);
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Hide a system notification for a download.
     * @param uri URI to update the download
     * @param cursor Cursor for reading the download's fields
     */
    private void hideNotification(Context context, Uri uri, Cursor cursor) {

        int statusColumn = cursor.getColumnIndexOrThrow(DownloadManager.Impl.COLUMN_STATUS);
        int status = cursor.getInt(statusColumn);
        int visibilityColumn = cursor.getColumnIndexOrThrow(DownloadManager.Impl.COLUMN_VISIBILITY);
        int visibility = cursor.getInt(visibilityColumn);
        if (DownloadManager.Impl.isStatusCompleted(status)
                && visibility == DownloadManager.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) {
            ContentValues values = new ContentValues();
            values.put(DownloadManager.Impl.COLUMN_VISIBILITY,
                    DownloadManager.Impl.VISIBILITY_VISIBLE);
            context.getContentResolver().update(uri, values, null, null);
        }
    }
    
    /**
     * Open the download that cursor is currently pointing to, since it's completed notification
     * has been clicked.
     */
    private void openDownload(Context context, Cursor cursor) {
        String filename = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.Impl.COLUMN_DATA));
        String mimetype = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.Impl.COLUMN_MIME_TYPE));
        int destination = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.Impl.COLUMN_DESTINATION));
        
        if (destination == Constants.DESTINATION_EXTERNAL && !LPhone.sdcard()) {
            // SDCard 没有挂载，无法进行安装操作
            AppToast.showShortText(context, context.getString(R.string.warning_sdcard_unmounted));
            return;
        }
        
        Uri path = Uri.parse(filename);
        // If there is no scheme, then it must be a file
        if (path.getScheme() == null) {
            path = Uri.fromFile(new File(filename));
        }

        Intent activityIntent = new Intent(Intent.ACTION_VIEW);
        activityIntent.setDataAndType(path, mimetype);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(activityIntent);
        } catch (ActivityNotFoundException ex) {
           AppLog.d(TAG,"no activity for " + mimetype);
        }
    }

//    /**
//     * Open the product detail page when download failed
//     */
//    private void openProductDetail(Context context, Intent intent) {
//        long id = Utils.getLong(intent.getStringExtra(DownloadManager.Impl.COLUMN_NOTIFICATION_EXTRAS));
//    }

    /**
     * Notify the owner of a running download that its notification was clicked.
     * @param intent the broadcast intent sent by the notification manager
     * @param cursor Cursor for reading the download's fields
     */
    private void sendNotificationClickedIntent(Context context, Intent intent, Cursor cursor) {
        context.sendBroadcast(new Intent(BuildConfig.BROADCAST_CLICK_INTENT));
    }

    private void startService(Context context) {
        context.startService(new Intent(context, DownloadService.class));
    }
}
