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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;

import com.lan.nicehair.utils.AppLog;
import com.lan.nicehair.utils.Utils;


/**
 * Performs the background downloads requested by applications that use the Downloads provider.
 */
public class DownloadService extends Service {
	
    public static final String TAG = null;

	/** Observer to get notified when the content observer's data changes */
    private DownloadManagerContentObserver mObserver;

    /** Class to handle Notification Manager updates */
    private DownloadNotification mNotifier;

    /**
     * The Service's view of the list of downloads, mapping download IDs to the corresponding info
     * object. This is kept independently from the content provider, and the Service only initiates
     * downloads based on this data, so that it can deal with situation where the data in the
     * content provider changes or disappears.
     */
    private Map<Long, DownloadInfo> mDownloads = new HashMap<Long, DownloadInfo>();

    /**
     * The thread that updates the internal download list from the content
     * provider.
     */
    UpdateThread mUpdateThread;

    /**
     * Whether the internal download list should be updated from the content
     * provider.
     */
    private boolean mPendingUpdate;

    /**
     * Receives notifications when the data in the content provider changes
     */
    private class DownloadManagerContentObserver extends ContentObserver {

        public DownloadManagerContentObserver() {
            super(new Handler());
        }

        /**
         * Receives notification when the data in the observed content
         * provider changes.
         */
        public void onChange(final boolean selfChange) {
            AppLog.d(TAG,"Service ContentObserver received notification");
            updateFromProvider();
        }

    }

    /**
     * Returns an IBinder instance when someone wants to connect to this
     * service. Binding to this service is not allowed.
     *
     * @throws UnsupportedOperationException
     */
    public IBinder onBind(Intent i) {
        throw new UnsupportedOperationException("Cannot bind to Download Manager Service");
    }

    /**
     * Initializes the service when it is first created
     */
    public void onCreate() {
        super.onCreate();
        AppLog.d(TAG,"Service onCreate");

        mObserver = new DownloadManagerContentObserver();
        getContentResolver().registerContentObserver(DownloadManager.Impl.CONTENT_URI,
                true, mObserver);

        mNotifier = new DownloadNotification(this);
        mNotifier.clearAllNotification();

        updateFromProvider();
    }
    
    @Override
	public void onStart(Intent intent, int startId) {
    	AppLog.d(TAG,"Service onStart");
    	updateFromProvider();
	}

    /**
     * Cleans up when the service is destroyed
     */
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(mObserver);
        AppLog.d(TAG,"Service onDestroy");
        super.onDestroy();
    }

	/**
     * Parses data from the content provider into private array
     */
    private void updateFromProvider() {
        synchronized (this) {
            mPendingUpdate = true;
            if (mUpdateThread == null) {
                mUpdateThread = new UpdateThread();
                mUpdateThread.start();
            }
        }
    }

    private class UpdateThread extends Thread {
        public UpdateThread() {
            super("Download Service");
        }

        public void run() {
            
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            trimDatabase();
            removeSpuriousFiles();

            boolean keepService = false;
            // for each update from the database, remember which download is
            // supposed to get restarted soonest in the future
            long wakeUp = Long.MAX_VALUE;
            for (;;) {
                synchronized (DownloadService.this) {
                    if (mUpdateThread != this) {
                        throw new IllegalStateException(
                                "multiple UpdateThreads in DownloadService");
                    }
                    if (!mPendingUpdate) {
                        mUpdateThread = null;
                        if (!keepService) {
                            stopSelf();
                        }
                        if (wakeUp != Long.MAX_VALUE) {
                            scheduleAlarm(wakeUp);
                        }
                        return;
                    }
                    mPendingUpdate = false;
                }

                long now = System.currentTimeMillis();
                keepService = false;
                wakeUp = Long.MAX_VALUE;
                Set<Long> idsNoLongerInDatabase = new HashSet<Long>(mDownloads.keySet());

                Cursor cursor = getContentResolver().query(DownloadManager.Impl.CONTENT_URI,
                        null, null, null, null);
                if (cursor == null) {
                    continue;
                }
                try {
                    DownloadInfo.Reader reader = new DownloadInfo.Reader(cursor);
                    int idColumn = cursor.getColumnIndexOrThrow(DownloadManager.Impl._ID);

                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        long id = cursor.getLong(idColumn);
                        idsNoLongerInDatabase.remove(id);
                        DownloadInfo info = mDownloads.get(id);
                        if (info != null) {
                            updateDownload(reader, info, now);
                        } else {
                            info = insertDownload(reader, now);
                        }

                        if (info.hasCompletionNotification()) {
                            keepService = true;
                        }
                        long next = info.nextAction(now);
                        if (next == 0) {
                            keepService = true;
                        } else if (next > 0 && next < wakeUp) {
                            wakeUp = next;
                        }
                    }
                } finally {
                    cursor.close();
                }

                for (Long id : idsNoLongerInDatabase) {
                    deleteDownload(id);
                }
                mNotifier.updateNotification(mDownloads.values());

                // look for all rows with deleted flag set and delete the rows from the database
                // permanently
                for (DownloadInfo info : mDownloads.values()) {
                    if (info.mDeleted) {
                        getContentResolver().delete(DownloadManager.Impl.CONTENT_URI,
                                DownloadManager.Impl._ID + " = ? ",
                                new String[] { String.valueOf(info.mId) });
                    }
                }
            }
        }

        /*
         * Schedule the retry task
         */
        private void scheduleAlarm(long wakeUp) {
            AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarms == null) {
                AppLog.e(TAG, "couldn't get alarm manager");
                return;
            }

            AppLog.d(TAG,"scheduling retry in " + wakeUp + "ms");

            Intent intent = new Intent(Constants.ACTION_RETRY);
            intent.setClassName("com.mappn.gfan", DownloadReceiver.class.getName());
            alarms.set(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + wakeUp,
                    PendingIntent.getBroadcast(DownloadService.this, 0, intent,
                            PendingIntent.FLAG_ONE_SHOT));
        }
    }

    /**
     * Removes files that may have been left behind in the cache directory
     */
    private void removeSpuriousFiles() {
        File[] files = Environment.getDownloadCacheDirectory().listFiles();
        if (files == null) {
            // The cache folder doesn't appear to exist (this is likely the case
            // when running the simulator).
            return;
        }

        // 获取缓存文件夹下所有的文件
        HashSet<String> fileSet = new HashSet<String>();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(Constants.KNOWN_SPURIOUS_FILENAME)) {
                continue;
            }
            if (files[i].getName().equalsIgnoreCase(Constants.RECOVERY_DIRECTORY)) {
                continue;
            }
            fileSet.add(files[i].getPath());
        }

        // 筛选出可以废弃的文件
        Cursor cursor = getContentResolver().query(DownloadManager.Impl.CONTENT_URI,
                new String[] { DownloadManager.Impl.COLUMN_DATA }, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    fileSet.remove(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        Iterator<String> iterator = fileSet.iterator();
        while (iterator.hasNext()) {
            String filename = iterator.next();
            AppLog.d(TAG,"deleting spurious file " + filename);
            new File(filename).delete();
        }
    }
    
    /**
     * Drops old rows from the database to prevent it from growing too large
     */
    private void trimDatabase() {
        
        Cursor cursor = getContentResolver().query(DownloadManager.Impl.CONTENT_URI,
                new String[] { DownloadManager.Impl._ID },
                DownloadManager.Impl.COLUMN_STATUS + " >= '200'", null,
                DownloadManager.Impl.COLUMN_LAST_MODIFICATION);
        
        if (cursor == null) {
            // This isn't good - if we can't do basic queries in our database, nothing's gonna work
            AppLog.e(TAG,"null cursor in trimDatabase");
            return;
        }
        if (cursor.moveToFirst()) {
            int numDelete = cursor.getCount() - Constants.MAX_DOWNLOADS;
            int columnId = cursor.getColumnIndexOrThrow(DownloadManager.Impl._ID);
            while (numDelete > 0) {
                Uri downloadUri = ContentUris.withAppendedId(
                        DownloadManager.Impl.CONTENT_URI, cursor.getLong(columnId));
                getContentResolver().delete(downloadUri, null, null);
                if (!cursor.moveToNext()) {
                    break;
                }
                numDelete--;
            }
        }
        cursor.close();
    }

    /**
     * Keeps a local copy of the info about a download, and initiates the
     * download if appropriate.
     */
    private DownloadInfo insertDownload(DownloadInfo.Reader reader, long now) {
        DownloadInfo info = reader.newDownloadInfo(this);
        mDownloads.put(info.mId, info);

        info.logVerboseInfo();

        info.startIfReady(now);
        return info;
    }

    /**
     * Updates the local copy of the info about a download.
     */
    private void updateDownload(DownloadInfo.Reader reader, DownloadInfo info, long now) {
        int oldVisibility = info.mVisibility;
        int oldStatus = info.mStatus;

        reader.updateFromDatabase(info);

        boolean lostVisibility =
                oldVisibility == DownloadManager.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                && info.mVisibility != DownloadManager.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                && DownloadManager.Impl.isStatusCompleted(info.mStatus);
        boolean justCompleted =
                !DownloadManager.Impl.isStatusCompleted(oldStatus)
                && DownloadManager.Impl.isStatusCompleted(info.mStatus);
        if (lostVisibility || justCompleted) {
        	mNotifier.cancelNotification(info.mId);
        }
        info.startIfReady(now);
    }

    /**
     * Removes the local copy of the info about a download.
     */
    private void deleteDownload(long id) {
        DownloadInfo info = mDownloads.get(id);
        if (info.mStatus == DownloadManager.Impl.STATUS_RUNNING) {
            info.mStatus = DownloadManager.Impl.STATUS_CANCELED;
        }
        if (info.mDestination != DownloadManager.Impl.DESTINATION_EXTERNAL 
        		&& info.mFileName != null) {
            new File(info.mFileName).delete();
        }
        mNotifier.cancelNotification(info.mId);
        mDownloads.remove(info.mId);
    }

}
