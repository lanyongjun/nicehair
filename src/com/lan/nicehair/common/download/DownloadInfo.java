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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.net.Uri;

import com.lan.nicehair.common.download.DownloadManager.Impl;
import com.lan.nicehair.utils.AppLog;
import com.lan.nicehair.utils.Utils;

/**
 * Stores information about an individual download.
 */
public class DownloadInfo {
    
    // the following NETWORK_* constants are used to indicates specfic reasons for disallowing a
    // download from using a network, since specific causes can require special handling
	private final String TAG="DownloadInfo";
    /**
     * The network is usable for the given download.
     */
    public static final int NETWORK_OK = 1;

    /**
     * There is no network connectivity.
     */
    public static final int NETWORK_NO_CONNECTION = 2;

    /**
     * For intents used to notify the user that a download exceeds a size threshold, if this extra
     * is true, WiFi is required for this download size; otherwise, it is only recommended.
     */
    public static final String EXTRA_IS_WIFI_REQUIRED = "isWifiRequired";

    // ID
    public long mId;
    // 下载链接
    public String mUri;
    // 提示
    public String mHint;
    // 文件名
    public String mFileName;
    // MIME TYPE
    public String mMimeType;
    // 存储路径
    public int mDestination;
    // 可见性
    public int mVisibility;
    // 下载控制（暂停、取消）
    public int mControl;
    // 下载状态
    public int mStatus;
    // 下载失败次数
    public int mNumFailed;
    // 重试时间
    public int mRetryAfter;
    // 重定向次数
    public int mRedirectCount;
    // 最后修改时间
    public long mLastMod;
    // 提醒包名
    public String mPackage;
    // 提醒类名
    public String mClass;
    // 用于提醒的额外信息
    public String mExtras;
    // 文件大小
    public long mTotalBytes;
    // 已经下载的大小
    public long mCurrentBytes;
    // 文件完整性ETAG
    public String mETag;
    // 是否删除此记录
    public boolean mDeleted;
    // 标题
    public String mTitle;
    // 描述信息
    public String mDescription;
    // 请求来源
    public int mSource;
    // MD5校验码
    public String mMD5;
    // 应用包名
    public String mPackageName;

    
    public int mFuzz;
    public volatile boolean mHasActiveThread;
    private Context mContext;

    private DownloadInfo(Context context) {
        mContext = context;
        mFuzz = Helper.rnd.nextInt(1001);
    }

    public void sendIntentIfRequested() {
        if (mPackage == null) {
            return;
        }

        if (mClass == null) {
            return;
        }
        // TODO
//        Intent intent = new Intent(DownloadManager.ACTION_DOWNLOAD_COMPLETED);
//        intent.setClassName(mPackage, mClass);
//        if (mExtras != null) {
//            intent.putExtra(DownloadManager.Impl.COLUMN_NOTIFICATION_EXTRAS, mExtras);
//        }
//        // We only send the content: URI, for security reasons. Otherwise, malicious
//        // applications would have an easier time spoofing download results by
//        // sending spoofed intents.
//        intent.setData(getMyDownloadsUri());
//        mContext.sendBroadcast(intent);
    }

    /**
     * Returns whether this download (which the download manager hasn't seen yet)
     * should be started.
     */
    private boolean isReadyToStart(long now) {
        if (mHasActiveThread) {
            // already running
            return false;
        }
        if (mControl == Impl.CONTROL_PAUSED) {
            // the download is paused, so it's not going to start
            return false;
        }
        switch (mStatus) {
            case 0: // status hasn't been initialized yet, this is a new download
            case Impl.STATUS_PAUSED_BY_APP: // download is paused by app
            case Impl.STATUS_PENDING: // download is explicit marked as ready to start
            case Impl.STATUS_RUNNING: // download interrupted (process killed etc) while
                                                // running, without a chance to update the database
                return true;

            case Impl.STATUS_WAITING_FOR_NETWORK:
            case Impl.STATUS_QUEUED_FOR_WIFI:
                return checkCanUseNetwork() == NETWORK_OK;

            case Impl.STATUS_WAITING_TO_RETRY:
                // download was waiting for a delayed restart
                return restartTime(now) <= now;
        }
        return false;
    }

    /**
     * Returns whether this download has a visible notification after
     * completion.
     */
    public boolean hasCompletionNotification() {
        if (!Impl.isStatusCompleted(mStatus)) {
            return false;
        }
        if (mVisibility == Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) {
            return true;
        }
        return false;
    }

    /**
     * Returns whether this download is allowed to use the network.
     * @return one of the NETWORK_* constants
     */
    public int checkCanUseNetwork() {
        Integer networkType = Helper.getActiveNetworkType(mContext);
        if (networkType == null) {
            return NETWORK_NO_CONNECTION;
        }
        return NETWORK_OK;
    }

    /**
     * @return a non-localized string appropriate for logging corresponding to one of the
     * NETWORK_* constants.
     */
    public String getLogMessageForNetworkError(int networkError) {
        switch (networkError) {
            case NETWORK_NO_CONNECTION:
                return "no network connection available";

            default:
                return "unknown error with network connectivity";
        }
    }

    /**
     * 如果符合下载条件马上开始下载
     */
    /*package*/ void startIfReady(long now) {
        
        if (!isReadyToStart(now)) {
            return;
        }

        AppLog.d(TAG,"Service spawning thread to handle download " + mId);
        
        if (mHasActiveThread) {
            throw new IllegalStateException("Multiple threads on same download");
        }
        if (mStatus != Impl.STATUS_RUNNING) {
            mStatus = Impl.STATUS_RUNNING;
            ContentValues values = new ContentValues();
            values.put(Impl.COLUMN_STATUS, mStatus);
            mContext.getContentResolver().update(getMyDownloadsUri(), values, null, null);
        }
        DownloadThread downloader = new DownloadThread(mContext, this);
        mHasActiveThread = true;
        downloader.start();
    }

    /**
     * 判断存储位置处于内部
     */
    public boolean isOnCache() {
        return (mDestination == Impl.DESTINATION_CACHE_PARTITION
                || mDestination == Impl.DESTINATION_CACHE_PARTITION_PURGEABLE);
    }

    public Uri getMyDownloadsUri() {
        return ContentUris.withAppendedId(DownloadManager.Impl.CONTENT_URI, mId);
    }

    /*
     * 打印下载项目详细信息
     */
    public void logVerboseInfo() {
    	AppLog.d(TAG,getVerboseInfo());
    }
    
    /*
     * 获取下载项目详细信息
     */
    public String getVerboseInfo() {
        StringBuilder info = new StringBuilder();
        info.append("ID      : " + mId + "\n");
        info.append("URI     : " + ((mUri != null) ? "yes" : "no") + "\n");
        info.append("HINT    : " + mHint + "\n");
        info.append("FILENAME: " + mFileName + "\n");
        info.append("MIMETYPE: " + mMimeType + "\n");
        info.append("DESTINAT: " + mDestination + "\n");
        info.append("VISIBILI: " + mVisibility + "\n");
        info.append("CONTROL : " + mControl + "\n");
        info.append("STATUS  : " + mStatus + "\n");
        info.append("FAILED_C: " + mNumFailed + "\n");
        info.append("RETRY_AF: " + mRetryAfter + "\n");
        info.append("REDIRECT: " + mRedirectCount + "\n");
        info.append("LAST_MOD: " + mLastMod + "\n");
        info.append("PACKAGE : " + mPackage + "\n");
        info.append("CLASS   : " + mClass + "\n");
        info.append("TOTAL   : " + mTotalBytes + "\n");
        info.append("CURRENT : " + mCurrentBytes + "\n");
        info.append("ETAG    : " + mETag + "\n");
        info.append("DELETED : " + mDeleted + "\n");
        return info.toString();
    }

    /**
     * Returns the amount of time (as measured from the "now" parameter)
     * at which a download will be active.
     * 0 = immediately - service should stick around to handle this download.
     * -1 = never - service can go away without ever waking up.
     * positive value - service must wake up in the future, as specified in ms from "now"
     */
    /*package*/long nextAction(long now) {
        if (Impl.isStatusCompleted(mStatus)) {
            return -1;
        }
        if (mStatus != Impl.STATUS_WAITING_TO_RETRY) {
            return 0;
        }
        long when = restartTime(now);
        if (when <= now) {
            return 0;
        }
        return when - now;
    }
    
    /**
     * Returns the time when a download should be restarted.
     */
    public long restartTime(long now) {
        if (mNumFailed == 0) {
            return now;
        }
        if (mRetryAfter > 0) {
            return mLastMod + mRetryAfter;
        }
        return mLastMod +
                Constants.RETRY_FIRST_DELAY *
                    (1000 + mFuzz) * (1 << (mNumFailed - 1));
    }
    
    /**
     * 持久化数据（DownloadInfo）的读取工具类
     */
    public static class Reader {
        private Cursor mCursor;
        private CharArrayBuffer mOldChars;
        private CharArrayBuffer mNewChars;

        public Reader(Cursor cursor) {
            mCursor = cursor;
        }

        public DownloadInfo newDownloadInfo(Context context) {
            DownloadInfo info = new DownloadInfo(context);
            updateFromDatabase(info);
            return info;
        }

        public void updateFromDatabase(DownloadInfo info) {
            info.mId = getLong(Impl._ID);
            info.mUri = getString(info.mUri, Impl.COLUMN_URI);
            info.mHint = getString(info.mHint, Impl.COLUMN_FILE_NAME_HINT);
            info.mFileName = getString(info.mFileName, Impl.COLUMN_DATA);
            info.mMimeType = getString(info.mMimeType, Impl.COLUMN_MIME_TYPE);
            info.mDestination = getInt(Impl.COLUMN_DESTINATION);
            info.mVisibility = getInt(Impl.COLUMN_VISIBILITY);
            info.mStatus = getInt(Impl.COLUMN_STATUS);
            info.mNumFailed = getInt(Impl.COLUMN_FAILED_CONNECTIONS);
            int retryRedirect = getInt(Impl.COLUMN_RETRY_AFTER_REDIRECT_COUNT);
            info.mRetryAfter = retryRedirect & 0xfffffff;
            info.mRedirectCount = retryRedirect >> 28;
            info.mLastMod = getLong(Impl.COLUMN_LAST_MODIFICATION);
            info.mPackage = getString(info.mPackage, Impl.COLUMN_NOTIFICATION_PACKAGE);
            info.mClass = getString(info.mClass, Impl.COLUMN_NOTIFICATION_CLASS);
            info.mExtras = getString(info.mExtras, Impl.COLUMN_NOTIFICATION_EXTRAS);
            info.mTotalBytes = getLong(Impl.COLUMN_TOTAL_BYTES);
            info.mCurrentBytes = getLong(Impl.COLUMN_CURRENT_BYTES);
            info.mETag = getString(info.mETag, Impl.COLUMN_ETAG);
            info.mDeleted = getInt(Impl.COLUMN_DELETED) == 1;
            info.mTitle = getString(info.mTitle, Impl.COLUMN_TITLE);
            info.mDescription = getString(info.mDescription, Impl.COLUMN_DESCRIPTION);
            info.mSource = getInt(Impl.COLUMN_SOURCE);
            info.mPackageName = getString(info.mPackageName, Impl.COLUMN_PACKAGE_NAME);
            info.mMD5 = getString(info.mPackageName, Impl.COLUMN_MD5);
            
            synchronized (this) {
                info.mControl = getInt(Impl.COLUMN_CONTROL);
            }
        }

        /**
         * Returns a String that holds the current value of the column, optimizing for the case
         * where the value hasn't changed.
         */
        private String getString(String old, String column) {
            int index = mCursor.getColumnIndexOrThrow(column);
            if (old == null) {
                return mCursor.getString(index);
            }
            if (mNewChars == null) {
                mNewChars = new CharArrayBuffer(128);
            }
            mCursor.copyStringToBuffer(index, mNewChars);
            int length = mNewChars.sizeCopied;
            if (length != old.length()) {
                return new String(mNewChars.data, 0, length);
            }
            if (mOldChars == null || mOldChars.sizeCopied < length) {
                mOldChars = new CharArrayBuffer(length);
            }
            char[] oldArray = mOldChars.data;
            char[] newArray = mNewChars.data;
            old.getChars(0, length, oldArray, 0);
            for (int i = length - 1; i >= 0; --i) {
                if (oldArray[i] != newArray[i]) {
                    return new String(newArray, 0, length);
                }
            }
            return old;
        }

        private Integer getInt(String column) {
            return mCursor.getInt(mCursor.getColumnIndexOrThrow(column));
        }

        private Long getLong(String column) {
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(column));
        }
    }
    
    /**
     * 当Wi-Fi网络转换到手机网络时，并且还有批量下载任务，提醒用户可以暂停
     */
    /*package*/ void notifyNetworkChanged() {
        
        // TODO 去应用管理页面，提供一键暂停功能
    }
}