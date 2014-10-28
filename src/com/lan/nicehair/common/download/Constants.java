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

import android.content.Intent;

/**
 * Constants for download module
 * 
 * @author andrew
 * @date    2011-4-27
 * @since   Version 0.7.0
 */
public class Constants {
    
//    public static final String TAG = "aMarketDownloader";
    
    /** 从机锋市场客户端发起的下载任务 */
    public static final int DOWNLOAD_FROM_MARKET = 0;
    
    /** 从机锋社区发起的下载任务 */
    public static final int DOWNLOAD_FROM_BBS = 1;
    
    /** 推送的应用 */
    public static final int DOWNLOAD_FROM_CLOUD = 2;
    
    /** OTA任务 */
    public static final int DOWNLOAD_FROM_OTA = 3;
    
    /** 机锋市场（Web）以及（Client）文件下载 路径*/
    public static final String DEFAULT_MARKET_SUBDIR = "gfan/market";
    
    /**  */
    public static final String DEFAULT_CLOUD_SUBDIR = "gfan/cloud";
    
    /** 机锋社区文件下载 路径*/
    public static final String DEFAULT_BBS_SUBDIR = "gfan/bbs";
    
    /** 默认文件下载 路径*/
    public static final String DEFAULT_SUBDIR = "gfan/others";

    /** The default extension for html files if we can't get one at the HTTP level */
    public static final String DEFAULT_DL_HTML_EXTENSION = ".html";

    /** The default extension for text files if we can't get one at the HTTP level */
    public static final String DEFAULT_DL_TEXT_EXTENSION = ".txt";

    /** The default extension for binary files if we can't get one at the HTTP level */
    public static final String DEFAULT_DL_BINARY_EXTENSION = ".bin";
    
    /** A magic filename that is allowed to exist within the system cache */
    public static final String KNOWN_SPURIOUS_FILENAME = "lost+found";

    /** A magic filename that is allowed to exist within the system cache */
    public static final String RECOVERY_DIRECTORY = "recovery";
    
    /** The default base name for downloaded files if we can't get one at the HTTP level */
    public static final String DEFAULT_DL_FILENAME = "downloadfile";
    
    /**
     * When a number has to be appended to the filename, this string is used to separate the
     * base filename from the sequence number
     */
    public static final String FILENAME_SEQUENCE_SEPARATOR = "-";
    
    /** The intent that gets sent when the service must wake up for a retry */
    public static final String ACTION_RETRY = "gfan.intent.action.DOWNLOAD_WAKEUP";
    
    /** the intent that gets sent when clicking an incomplete/failed download  */
    public static final String ACTION_LIST = "gfan.intent.action.DOWNLOAD_LIST";
    
    /** the intent that gets sent when clicking a successful download */
    public static final String ACTION_OPEN = "gfan.intent.action.DOWNLOAD_OPEN";
    
    /** the intent that gets sent when deleting the notification of a completed download */
    public static final String ACTION_HIDE = "gfan.intent.action.DOWNLOAD_HIDE";
    
    /**
     * This download will be saved to the external storage. This is the
     * default behavior, and should be used for any file that the user
     * can freely access, copy, delete. Even with that destination,
     * unencrypted DRM files are saved in secure internal storage.
     * Downloads to the external destination only write files for which
     * there is a registered handler. The resulting files are accessible
     * by filename to all applications.
     */
    public static final int DESTINATION_EXTERNAL = 0;

    /**
     * This download will be saved to the download manager's private
     * partition. This is the behavior used by applications that want to
     * download private files that are used and deleted soon after they
     * get downloaded. All file types are allowed, and only the initiating
     * application can access the file (indirectly through a content
     * provider). This requires the
     * android.permission.ACCESS_DOWNLOAD_MANAGER_ADVANCED permission.
     */
    public static final int DESTINATION_CACHE_PARTITION = 1;
    
    /** The MIME type of APKs */
    public static final String MIMETYPE_APK = "application/vnd.android.package-archive";
    
    /** The MIME type of image */
    public static final String MIMETYPE_IMAGE = "image/*";
    
    /** The buffer size used to stream the data */
    public static final int BUFFER_SIZE = 4096;

    /** The minimum amount of progress that has to be done before the progress bar gets updated */
    public static final int MIN_PROGRESS_STEP = 4096;

    /** The minimum amount of time that has to elapse before the progress bar gets updated, in ms */
    public static final long MIN_PROGRESS_TIME = 1500;

    /** The maximum number of rows in the database (FIFO) */
    public static final int MAX_DOWNLOADS = 1000;
    
    /**
     * The number of times that the download manager will retry its network
     * operations when no progress is happening before it gives up.
     */
    public static final int MAX_RETRIES = 5;

    /**
     * The minimum amount of time that the download manager accepts for
     * a Retry-After response header with a parameter in delta-seconds.
     */
    public static final int MIN_RETRY_AFTER = 30; // 30s

    /**
     * The maximum amount of time that the download manager accepts for
     * a Retry-After response header with a parameter in delta-seconds.
     */
    public static final int MAX_RETRY_AFTER = 24 * 60 * 60; // 24h

    /**
     * The maximum number of redirects.
     */
    public static final int MAX_REDIRECTS = 5; // can't be more than 7.

    /**
     * The time between a failure and the first retry after an IOException.
     * Each subsequent retry grows exponentially, doubling each time.
     * The time is in seconds.
     */
    public static final int RETRY_FIRST_DELAY = 30;

	
}
