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
import java.util.Random;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.webkit.MimeTypeMap;

import com.lan.nicehair.utils.AppLog;
import com.lan.nicehair.utils.Utils;

/**
 * 下载服务工具类
 * 
 * @author andrew
 * @date    2011-4-27
 *
 */
public class Helper {

    private static final String TAG = null;
	public static Random rnd = new Random(SystemClock.uptimeMillis());
    
    /**
     * Exception thrown from methods called by generateSaveFile() for any fatal error.
     */
    public static class GenerateSaveFileError extends Exception {
        /** */
        private static final long serialVersionUID = 7750062109363258607L;
        int mStatus;
        String mMessage;

        public GenerateSaveFileError(int status, String message) {
            mStatus = status;
            mMessage = message;
        }
    }
    
    /**
     * Creates a filename (where the file should be saved) from info about a download.
     */
    public static String generateSaveFile(
            Context context, 
            String url, 
            String hint,
            String contentLocation, 
            String mimeType, 
            int destination, 
            long contentLength, 
            int source)
            throws GenerateSaveFileError {

        return chooseFullPath(context, url, hint, contentLocation, mimeType, destination,
                contentLength, source);
    }
    
    /**
     * 通过MIME TYPE判断文件的后缀名
     * @param mimeType MIME TYPE
     * @param useDefaults 是否使用默认后缀
     * @return 后缀名，可能为Null
     */
    private static String chooseExtensionFromMimeType(String mimeType, boolean useDefaults) {
        String extension = null;
        if (mimeType != null) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension != null) {
            	AppLog.d(TAG,"adding extension from MIME type.");
                extension = "." + extension;
                return extension;
            } else {
                AppLog.d(TAG,"couldn't find extension for " + mimeType);
            }

            if (mimeType.toLowerCase().startsWith("text/")) {
                if (mimeType.equalsIgnoreCase("text/html")) {
                    AppLog.d(TAG,"adding default html extension");
                    extension = Constants.DEFAULT_DL_HTML_EXTENSION;
                    return extension;
                } else if (useDefaults) {
                    AppLog.d(TAG,"adding default text extension");
                    extension = Constants.DEFAULT_DL_TEXT_EXTENSION;
                    return extension;
                }
            }
        } else if (useDefaults) {
            AppLog.d(TAG,"adding default binary extension");
            extension = Constants.DEFAULT_DL_BINARY_EXTENSION;
        }
        return extension;
    }
    
    /**
     * 通过文件名判断后缀名 
     * @param mimeType MIME TYPE
     * @param filename 文件名
     * @param dotIndex '.'下标
     * @return 后缀名
     */
    private static String chooseExtensionFromFilename(String mimeType, String filename, int dotIndex) {
        String extension = null;
        if (mimeType != null) {
            // Compare the last segment of the extension against the mime type.
            // If there's a mismatch, discard the entire extension.
            int lastDotIndex = filename.lastIndexOf('.');
            String typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    filename.substring(lastDotIndex + 1));
            if (typeFromExt == null || !typeFromExt.equalsIgnoreCase(mimeType)) {
                extension = chooseExtensionFromMimeType(mimeType, false);
                if (extension != null) {
                    AppLog.d(TAG,"substituting extension from type");
                } else {
                    AppLog.d(TAG,"couldn't find extension for " + mimeType);
                }
            }
        }
        if (extension == null) {
            AppLog.d(TAG,"keeping extension");
            extension = filename.substring(dotIndex);
        }
        return extension;
    }
    
    /**
     * 外部存储设备是否就绪
     */
    public static boolean isExternalMediaMounted() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // No SD card found.
            AppLog.d(TAG,"no external storage");
            return false;
        }
        return true;
    }
    
    /**
     * 获取内部存储地址
     * 
     * @throws GenerateSaveFileError 如果存储区域大小不足，会抛出此异常
     */
    private static File getCacheDestination(Context context, long contentLength)
            throws GenerateSaveFileError {
        File base = context.getCacheDir();
        if (getAvailableBytes(base) < contentLength) {
            // No files to purge, give up.
            AppLog.d(TAG,"download aborted - not enough internal free space");
            throw new GenerateSaveFileError(
                DownloadManager.Impl.STATUS_INSUFFICIENT_SPACE_ERROR,
                "not enough free space in internal download storage, unable to free any "
                        + "more");
        }
        return base;
    }
    
    /**
     * 获取外部存储地址
     * @param source 请求来源
     * @param mimeType 文件MIME TYPE
     * 
     * @throws GenerateSaveFileError 如果外部存储区域没有被加载、只读、存储大小不足，会抛出此异常
     */
    private static File getExternalDestination(long contentLength, int source, String mimeType)
            throws GenerateSaveFileError {

        if (!isExternalMediaMounted()) {
            throw new GenerateSaveFileError(DownloadManager.Impl.STATUS_DEVICE_NOT_FOUND_ERROR,
                    "external media not mounted");
        }

        File root = Environment.getExternalStorageDirectory();
        if (getAvailableBytes(root) < contentLength) {
            // Insufficient space.
            AppLog.d(TAG,"download aborted - not enough external free space");
            throw new GenerateSaveFileError(DownloadManager.Impl.STATUS_INSUFFICIENT_SPACE_ERROR,
                    "insufficient space on external media");
        }

        // 通过下载请求来源确定文件路径（机锋市场，云推送，升级...）
        File base = null;
        if(Constants.DOWNLOAD_FROM_MARKET == source) {
            base = new File(root.getPath(), Constants.DEFAULT_MARKET_SUBDIR);
        } else if(Constants.DOWNLOAD_FROM_BBS == source) {
            base = new File(root.getPath(), Constants.DEFAULT_BBS_SUBDIR);
        } else if(Constants.DOWNLOAD_FROM_CLOUD == source) {
            base = new File(root.getPath(), Constants.DEFAULT_CLOUD_SUBDIR);
        }
        if (!base.isDirectory() && !base.mkdirs()) {
            // Can't create download directory, e.g. because a file called "download"
            // already exists at the root level, or the SD card filesystem is read-only.
            throw new GenerateSaveFileError(DownloadManager.Impl.STATUS_FILE_ERROR,
                    "unable to create external downloads directory " + base.getPath());
        }
        return base;
    }
    
    /**
     * @return the number of bytes available on the filesystem rooted at the given File
     */
    public static long getAvailableBytes(File root) {
        StatFs stat = new StatFs(root.getPath());
        // put a bit of margin (in case creating the file grows the system by a few blocks)
        long availableBlocks = (long) stat.getAvailableBlocks() - 4;
        return stat.getBlockSize() * availableBlocks;
    }
    
    /**
     * 获取网络类型
     */
    public static Integer getActiveNetworkType(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            AppLog.d(TAG,"couldn't get connectivity manager");
            return null;
        }

        NetworkInfo activeInfo = connectivity.getActiveNetworkInfo();
        if (activeInfo == null) {
            AppLog.d(TAG,"network is not available");
            return null;
        }
        return activeInfo.getType();
    }
    
    /**
     * Returns whether the network is available
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            AppLog.d(TAG,"couldn't get connectivity manager");
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        AppLog.d(TAG,"network is available");
                        return true;
                    }
                }
            }
        }
        AppLog.d(TAG,"network is not available");
        return false;
    }
    
    /**
     * Checks whether the filename looks legitimate
     */
    public static boolean isFilenameValid(String filename, int sourceType) {
        File dir = new File(filename).getParentFile();

        if (Constants.DOWNLOAD_FROM_MARKET == sourceType) {
            return dir.equals(new File(Environment.getExternalStorageDirectory(),
                    Constants.DEFAULT_MARKET_SUBDIR));
        } else if (Constants.DOWNLOAD_FROM_BBS == sourceType) {
            return dir.equals(new File(Environment.getExternalStorageDirectory(),
                    Constants.DEFAULT_BBS_SUBDIR));
        } else if (Constants.DOWNLOAD_FROM_CLOUD == sourceType) {
            return dir.equals(new File(Environment.getExternalStorageDirectory(),
                    Constants.DEFAULT_CLOUD_SUBDIR));
        }
        return dir.equals(new File(Environment.getExternalStorageDirectory()
                + Constants.DEFAULT_SUBDIR));
    }
    
    /**
     * 获取文件存储的完全路径
     */
    private static String chooseFullPath(Context context, String url,
            String hint, String contentLocation,
            String mimeType, int destination, long contentLength, int source)
            throws GenerateSaveFileError {
        File base = locateDestinationDirectory(context, mimeType, source, destination,
                contentLength);
        String filename = chooseFilename(url, hint, contentLocation, destination);

        // Split filename between base and extension
        // Add an extension if filename does not have one
        String extension = null;
        int dotIndex = filename.indexOf('.');
        if (dotIndex < 0) {
            extension = chooseExtensionFromMimeType(mimeType, true);
        } else {
            extension = chooseExtensionFromFilename(mimeType, filename, dotIndex);
            filename = filename.substring(0, dotIndex);
        }

        boolean recoveryDir = Constants.RECOVERY_DIRECTORY
                .equalsIgnoreCase(filename + extension);

        filename = base.getPath() + File.separator + filename;

        AppLog.d(TAG,"target file: " + filename + extension);

        return chooseUniqueFilename(destination, filename, extension,
                recoveryDir);
    }
    
    private static File locateDestinationDirectory(Context context,
            String mimeType, int source, int destination, long contentLength)
            throws GenerateSaveFileError {
        if (destination == DownloadManager.Impl.DESTINATION_CACHE_PARTITION) {
            return getCacheDestination(context, contentLength);
        }
        return getExternalDestination(contentLength, source, mimeType);
    }
    
    private static String chooseFilename(String url, String hint, String contentLocation,
            int destination) {
        String filename = null;

        // First, try to use the hint from the application, if there's one
        if (filename == null && hint != null && !hint.endsWith("/")) {
        	AppLog.i(TAG,"getting filename from hint");
            int index = hint.lastIndexOf('/') + 1;
            if (index > 0) {
                filename = hint.substring(index);
            } else {
                filename = hint;
            }
        }

        // If we still have nothing at this point, try the content location
        if (filename == null && contentLocation != null) {
            String decodedContentLocation = Uri.decode(contentLocation);
            if (decodedContentLocation != null
                    && !decodedContentLocation.endsWith("/")
                    && decodedContentLocation.indexOf('?') < 0) {
            	AppLog.i(TAG,"getting filename from content-location");
                int index = decodedContentLocation.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = decodedContentLocation.substring(index);
                } else {
                    filename = decodedContentLocation;
                }
            }
        }

        // If all the other http-related approaches failed, use the plain uri
        if (filename == null) {
            String decodedUrl = Uri.decode(url);
            if (decodedUrl != null
                    && !decodedUrl.endsWith("/") && decodedUrl.indexOf('?') < 0) {
                int index = decodedUrl.lastIndexOf('/') + 1;
                if (index > 0) {
                	AppLog.i(TAG,"getting filename from uri");
                    filename = decodedUrl.substring(index);
                }
            }
        }

        // Finally, if couldn't get filename from URI, get a generic filename
        if (filename == null) {
        	AppLog.i(TAG,"using default filename");
            filename = Constants.DEFAULT_DL_FILENAME;
        }

        // The VFAT file system is assumed as target for downloads.
        // Replace invalid characters according to the specifications of VFAT.
        filename = replaceInvalidVfatCharacters(filename);

        return filename;
    }
    
    private static String chooseUniqueFilename(int destination, String filename,
            String extension, boolean recoveryDir) {
        String fullFilename = filename + extension;
        if (!new File(fullFilename).exists()
                && (!recoveryDir ||
                destination != DownloadManager.Impl.DESTINATION_CACHE_PARTITION)) {
            return fullFilename;
        }
        filename = filename + Constants.FILENAME_SEQUENCE_SEPARATOR;
        /*
        * This number is used to generate partially randomized filenames to avoid
        * collisions.
        * It starts at 1.
        * The next 9 iterations increment it by 1 at a time (up to 10).
        * The next 9 iterations increment it by 1 to 10 (random) at a time.
        * The next 9 iterations increment it by 1 to 100 (random) at a time.
        * ... Up to the point where it increases by 100000000 at a time.
        * (the maximum value that can be reached is 1000000000)
        * As soon as a number is reached that generates a filename that doesn't exist,
        *     that filename is used.
        * If the filename coming in is [base].[ext], the generated filenames are
        *     [base]-[sequence].[ext].
        */
        int sequence = 1;
        for (int magnitude = 1; magnitude < 1000000000; magnitude *= 10) {
            for (int iteration = 0; iteration < 9; ++iteration) {
                fullFilename = filename + sequence + extension;
                if (!new File(fullFilename).exists()) {
                    return fullFilename;
                }
                AppLog.i(TAG,"file with sequence number " + sequence + " exists");
                sequence += rnd.nextInt(magnitude) + 1;
            }
        }
        return null;
    }
    
    /**
     * Replace invalid filename characters according to
     * specifications of the VFAT.
     * @note Package-private due to testing.
     */
    private static String replaceInvalidVfatCharacters(String filename) {
        final char START_CTRLCODE = 0x00;
        final char END_CTRLCODE = 0x1f;
        final char QUOTEDBL = 0x22;
        final char ASTERISK = 0x2A;
        final char SLASH = 0x2F;
        final char COLON = 0x3A;
        final char LESS = 0x3C;
        final char GREATER = 0x3E;
        final char QUESTION = 0x3F;
        final char BACKSLASH = 0x5C;
        final char BAR = 0x7C;
        final char DEL = 0x7F;
        final char UNDERSCORE = 0x5F;

        StringBuffer sb = new StringBuffer();
        char ch;
        boolean isRepetition = false;
        for (int i = 0; i < filename.length(); i++) {
            ch = filename.charAt(i);
            if ((START_CTRLCODE <= ch &&
                ch <= END_CTRLCODE) ||
                ch == QUOTEDBL ||
                ch == ASTERISK ||
                ch == SLASH ||
                ch == COLON ||
                ch == LESS ||
                ch == GREATER ||
                ch == QUESTION ||
                ch == BACKSLASH ||
                ch == BAR ||
                ch == DEL){
                if (!isRepetition) {
                    sb.append(UNDERSCORE);
                    isRepetition = true;
                }
            } else {
                sb.append(ch);
                isRepetition = false;
            }
        }
        return sb.toString();
    }
}