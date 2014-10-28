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

import java.util.ArrayList;
import java.util.List;

import com.lan.nicehair.activity.MainActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

/**
 * The download manager is a system service that handles long-running HTTP downloads. Clients may
 * request that a URI be downloaded to a particular destination file. The download manager will
 * conduct the download in the background, taking care of HTTP interactions and retrying downloads
 * after failures or across connectivity changes and system reboots.
 * 
 * @author andrew
 * @date 2011-4-27
 * @since Version 0.7.0
 */
public class DownloadManager {
    
    /**
     * Value of {@link #COLUMN_STATUS} when the download is waiting to start.
     */
    public final static int STATUS_PENDING = 1 << 0;

    /**
     * Value of {@link #COLUMN_STATUS} when the download is currently running.
     */
    public final static int STATUS_RUNNING = 1 << 1;

    /**
     * Value of {@link #COLUMN_STATUS} when the download is waiting to retry or resume.
     */
    public final static int STATUS_PAUSED = 1 << 2;

    /**
     * Value of {@link #COLUMN_STATUS} when the download has successfully completed.
     */
    public final static int STATUS_SUCCESSFUL = 1 << 3;

    /**
     * Value of {@link #COLUMN_STATUS} when the download has failed (and will not be retried).
     */
    public final static int STATUS_FAILED = 1 << 4;
    
    private ContentResolver mResolver;
    private String mPackageName;
    private Uri mBaseUri = Impl.CONTENT_URI;
    
    /**
     * Get a parameterized SQL WHERE clause to select a bunch of IDs.
     */
    static String getWhereClauseForIds(long[] ids) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                whereClause.append("OR ");
            }
            whereClause.append(Impl._ID);
            whereClause.append(" = ? ");
        }
        whereClause.append(")");
        return whereClause.toString();
    }
    
    /**
     * Get the selection args for a clause returned by {@link #getWhereClauseForIds(long[])}.
     */
    static String[] getWhereArgsForIds(long[] ids) {
        String[] whereArgs = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            whereArgs[i] = Long.toString(ids[i]);
        }
        return whereArgs;
    }
    
    public DownloadManager(ContentResolver resolver, String packageName) {
        mResolver = resolver;
        mPackageName = packageName;
    }

    /**
     * Enqueue a new download.  The download will start automatically once the download manager is
     * ready to execute it and connectivity is available.
     *
     * @param request the parameters specifying this download
     * @return an ID for the download, unique across the system.  This ID is used to make future
     * calls related to this download.
     */
    public long enqueue(Request request) {
        ContentValues values = request.toContentValues(mPackageName);

        // use the title as the file's name
        values.put(Impl.COLUMN_FILE_NAME_HINT, (String) request.mTitle);
        values.put(Impl.COLUMN_PACKAGE_NAME, request.mPackageName);
        values.put(Impl.COLUMN_NOTIFICATION_CLASS, MainActivity.class.getName());
        values.put(Impl.COLUMN_MD5, request.mMD5);
        if (request.mSourceType == Constants.DOWNLOAD_FROM_OTA) {
            values.put(Impl.COLUMN_DESTINATION, DownloadManager.Impl.DESTINATION_CACHE_PARTITION);
        } else {
            values.put(Impl.COLUMN_DESTINATION, request.mDestination);
        }
        Uri downloadUri = mResolver.insert(Impl.CONTENT_URI, values);
        if (downloadUri == null) {
            return -1;
        }
        long id = Long.parseLong(downloadUri.getLastPathSegment());
        return id;
    }
    
    public long enqueueWaitRequest(Request request) {
        ContentValues values = request.toContentValues(mPackageName);

        // use the title as the file's name
        values.put(Impl.COLUMN_FILE_NAME_HINT, (String) request.mTitle);
        values.put(Impl.COLUMN_PACKAGE_NAME, request.mPackageName);
//  TODO      values.put(Impl.COLUMN_NOTIFICATION_CLASS, LocalManagerActivity.class.getName());
        values.put(Impl.COLUMN_MD5, request.mMD5);
        values.put(Impl.COLUMN_DESTINATION, request.mDestination);
        values.put(Impl.COLUMN_CONTROL, Impl.CONTROL_PENDING);
        Uri downloadUri = mResolver.insert(Impl.CONTENT_URI, values);
        if (downloadUri == null) {
            return -1;
        }
        long id = Long.parseLong(downloadUri.getLastPathSegment());
        return id;
        
    }
    
    /**
     * 隐藏已经下载完成的OTA任务
     */
    public int hideDownload(long... ids) {
        if (ids == null || ids.length == 0) {
            // called with nothing to remove!
            throw new IllegalArgumentException("input param 'ids' can't be null");
        }

        ContentValues values = new ContentValues();
        values.put(Impl.COLUMN_VISIBILITY, Impl.VISIBILITY_HIDDEN);
        return mResolver.update(mBaseUri, values, getWhereClauseForIds(ids),
                getWhereArgsForIds(ids));
    }
    
    /**
     * Marks the specified download as 'paused'.
     *
     * @param ids the IDs of the downloads to be marked 'paused'
     * @return the number of downloads actually updated
     */
    public int pauseDownload(long... ids) {
        if (ids == null || ids.length == 0) {
            // called with nothing to remove!
            throw new IllegalArgumentException("input param 'ids' can't be null");
        }
        
        ContentValues values = new ContentValues();
        values.put(Impl.COLUMN_CONTROL, Impl.CONTROL_PAUSED);
        return mResolver.update(mBaseUri, values, getWhereClauseForIds(ids),
                getWhereArgsForIds(ids));
    }
    
    /**
     * Marks the specified download as 'resumed'.
     *
     * @param ids the IDs of the downloads to be marked 'paused'
     * @return the number of downloads actually updated
     */
    public int resumeDownload(long... ids) {
        if (ids == null || ids.length == 0) {
            // called with nothing to remove!
            throw new IllegalArgumentException("input param 'ids' can't be null");
        }
        
        ContentValues values = new ContentValues();
        values.put(Impl.COLUMN_CONTROL, Impl.CONTROL_RUN);
        return mResolver.update(mBaseUri, values, getWhereClauseForIds(ids),
                getWhereArgsForIds(ids));
    }
    
    public int cancelDownload(long... ids) {
        if (ids == null || ids.length == 0) {
            // called with nothing to remove!
            throw new IllegalArgumentException("input param 'ids' can't be null");
        }
        
        ContentValues values = new ContentValues();
        values.put(Impl.COLUMN_STATUS, Impl.STATUS_CANCELED);
        return mResolver.update(mBaseUri, values, getWhereClauseForIds(ids),
                getWhereArgsForIds(ids));
    }
    
    /**
     * 更新软件的状态为已经安装
     * @param packageName 包名
     */
    public void completeInstallation(final String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        
        ContentValues values = new ContentValues();
        values.put(Impl.COLUMN_STATUS, Impl.STATUS_INSTALLED);
        values.put(Impl.COLUMN_VISIBILITY, Impl.VISIBILITY_HIDDEN);
        values.put(Impl.COLUMN_DELETED, 1);
        mResolver.update(mBaseUri, values,
                getWhereClauseForPackageName(),
                getWhereArgsForPackageName(packageName));
    }
    
    static String getWhereClauseForPackageName() {

        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        whereClause.append(Impl.COLUMN_STATUS);
        whereClause.append(" = ? AND ");
        whereClause.append(Impl.COLUMN_PACKAGE_NAME);
        whereClause.append(" = ? )");
        return whereClause.toString();
    }

    static String[] getWhereArgsForPackageName(String packageName) {
        return new String[] { String.valueOf(Impl.STATUS_SUCCESS), packageName };
    }
    
    /**
     * 获取下载中的任务（包括下载完成、下载取消）<br>
     */
    public Cursor getDownloadingApks() {
        return mResolver.query(mBaseUri, new String[]{
                Impl._ID,
                Impl.COLUMN_DATA, 
                Impl.COLUMN_TITLE, 
                Impl.COLUMN_DESCRIPTION, 
                Impl.COLUMN_CURRENT_BYTES, 
                Impl.COLUMN_TOTAL_BYTES,
                Impl.COLUMN_STATUS,
                Impl.COLUMN_PACKAGE_NAME,
                Impl.COLUMN_NOTIFICATION_EXTRAS
        }, "(((" + Impl.COLUMN_STATUS + " >= '190' AND " 
               + Impl.COLUMN_STATUS + " <= '200') OR "
               + Impl.COLUMN_STATUS + " = '" + Impl.STATUS_CANCELED + "') AND "
               + Impl.COLUMN_DESTINATION + " = '" + Impl.DESTINATION_EXTERNAL + "' AND "
               + Impl.COLUMN_MIME_TYPE + " = '" + Constants.MIMETYPE_APK + "')", null, null);
    }
    
    public Cursor getDownloadErrorApks() {
        return mResolver.query(mBaseUri, new String[]{
                Impl._ID,
                Impl.COLUMN_TITLE,
                Impl.COLUMN_STATUS
        }, "((" + Impl.COLUMN_STATUS + " >= '400' AND " 
               + Impl.COLUMN_STATUS + " < '500') AND "
               + Impl.COLUMN_DESTINATION + " = '" + Impl.DESTINATION_EXTERNAL + "' AND "
               + Impl.COLUMN_MIME_TYPE + " = '" + Constants.MIMETYPE_APK + "')", null, null);
    }

    /**
     * Marks the specified download as 'to be deleted'. This is done when a completed download
     * is to be removed but the row was stored without enough info to delete the corresponding
     * metadata from Mediaprovider database. Actual cleanup of this row is done in DownloadService.
     *
     * @param ids the IDs of the downloads to be marked 'deleted'
     * @return the number of downloads actually updated
     */
    public int markRowDeleted(long... ids) {
        if (ids == null || ids.length == 0) {
            // called with nothing to remove!
            throw new IllegalArgumentException("input param 'ids' can't be null");
        }
        ContentValues values = new ContentValues();
        values.put(Impl.COLUMN_DELETED, 1);
        return mResolver.update(mBaseUri, values, getWhereClauseForIds(ids),
                getWhereArgsForIds(ids));
    }

    /**
     * Cancel downloads and remove them from the download manager.  Each download will be stopped if
     * it was running, and it will no longer be accessible through the download manager.  If a file
     * was already downloaded to external storage, it will not be deleted.
     *
     * @param ids the IDs of the downloads to remove
     * @return the number of downloads actually removed
     */
    public int remove(long... ids) {
        if (ids == null || ids.length == 0) {
            // called with nothing to remove!
            throw new IllegalArgumentException("input param 'ids' can't be null");
        }
        return mResolver.delete(mBaseUri, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
    }

    /**
     * Query the download manager about downloads that have been requested.
     * @param query parameters specifying filters for this query
     * @return a Cursor over the result set of downloads, with columns consisting of all the
     * COLUMN_* constants.
     */
    public Cursor query(Query query) {
        return query.runQuery(mResolver, null, mBaseUri);
    }

    /**
     * Restart the given downloads, which must have already completed (successfully or not).  This
     * method will only work when called from within the download manager's process.
     * @param ids the IDs of the downloads
     */
    public void restartDownload(long... ids) {
        Cursor cursor = query(new Query().setFilterById(ids));
        try {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int status = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_STATUS));
                if (status != STATUS_SUCCESSFUL && status != STATUS_FAILED) {
                    throw new IllegalArgumentException("Cannot restart incomplete download: "
                            + cursor.getLong(cursor.getColumnIndex(Impl.COLUMN_ID)));
                }
            }
        } finally {
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(Impl.COLUMN_CURRENT_BYTES, 0);
        values.put(Impl.COLUMN_TOTAL_BYTES, -1);
        values.putNull(Impl.COLUMN_DATA);
        values.put(Impl.COLUMN_STATUS, Impl.STATUS_PENDING);
        mResolver.update(mBaseUri, values, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
    }
    
    /**
     * This class contains all the information necessary to request a new download. The URI is the
     * only required parameter.
     *
     * Note that the default download destination is a shared volume where the system might delete
     * your file if it needs to reclaim space for system use. If this is a problem, use a location
     * on external storage (see {@link #setDestinationUri(Uri)}.
     */
    public static class Request {

        private Uri mUri;
        private CharSequence mTitle;
        private CharSequence mDescription;
        private boolean mShowNotification = true;
        private String mPackageName;
        private String mMD5;
        private int mSourceType;
        private String mMimeType = Constants.MIMETYPE_APK;
        private int mDestination = Impl.DESTINATION_EXTERNAL;
        private String mIconUrl;

        /**
         * @param uri the HTTP URI to download.
         */
        public Request(Uri uri) {
            if (uri == null) {
                throw new NullPointerException();
            }
            String scheme = uri.getScheme();
            if (scheme == null || !scheme.equals("http")) {
                throw new IllegalArgumentException("Can only download HTTP URIs: " + uri);
            }
            mUri = uri;
        }
        
        /**
         * Set the download destination
         */
        public Request setDestination(int dest) {
            mDestination = dest;
            return this;
        }

        /**
         * Set the title of this download, to be displayed in notifications (if enabled).  If no
         * title is given, a default one will be assigned based on the download filename, once the
         * download starts.
         * @return this object
         */
        public Request setTitle(CharSequence title) {
            mTitle = title;
            return this;
        }
        
        public Request setSourceType(int sourceType) {
            mSourceType = sourceType;
            return this;
        }
        
        public Request setMimeType(String mimeType) {
            mMimeType = mimeType;
            return this;
        }

        /**
         * Set a description of this download, to be displayed in notifications (if enabled)
         * @return this object
         */
        public Request setDescription(CharSequence description) {
            mDescription = description;
            return this;
        }
        
        /**
         * Set the md5 code of the file
         */
        public Request setMD5(String md5) {
            mMD5 = md5;
            return this;
        }
        
        /**
         * Set the Package Name of the APK file.
         * @param packageName
         */
        public Request setPackageName(String packageName) {
            mPackageName = packageName;
            return this;
        }
        
        public Request setIconUrl(String url) {
            mIconUrl = url;
            return this;
        }

        /**
         * Control whether a system notification is posted by the download manager while this
         * download is running. If enabled, the download manager posts notifications about downloads
         * through the system {@link android.app.NotificationManager}. By default, a notification is
         * shown.
         *
         * If set to false, this requires the permission
         * android.permission.DOWNLOAD_WITHOUT_NOTIFICATION.
         *
         * @param show whether the download manager should show a notification for this download.
         * @return this object
         */
        public Request setShowRunningNotification(boolean show) {
            mShowNotification = show;
            return this;
        }

        /**
         * @return ContentValues to be passed to DownloadProvider.insert()
         */
        ContentValues toContentValues(String packageName) {
            ContentValues values = new ContentValues();
            assert mUri != null;
            values.put(Impl.COLUMN_URI, mUri.toString());
            values.put(Impl.COLUMN_NOTIFICATION_PACKAGE, packageName);
            values.put(Impl.COLUMN_MIME_TYPE, mMimeType);
            values.put(Impl.COLUMN_NOTIFICATION_EXTRAS, mIconUrl);
            values.put(Impl.COLUMN_SOURCE, mSourceType);
            putIfNonNull(values, Impl.COLUMN_TITLE, mTitle);
            putIfNonNull(values, Impl.COLUMN_DESCRIPTION, mDescription);
            
            values.put(Impl.COLUMN_VISIBILITY,
                    mShowNotification ? Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                            : Impl.VISIBILITY_HIDDEN);
            if (mSourceType == Constants.DOWNLOAD_FROM_OTA
                    && Constants.MIMETYPE_APK.equals(mMimeType)) {
                values.put(Impl.COLUMN_VISIBILITY, Impl.VISIBILITY_VISIBLE);
            }
            return values;
        }

        private void putIfNonNull(ContentValues contentValues, String key, Object value) {
            if (value != null) {
                contentValues.put(key, value.toString());
            }
        }
    }
    
    /**
     * This class may be used to filter download manager queries.
     */
    public static class Query {
        /**
         * Constant for use with {@link #orderBy}
         */
        public static final int ORDER_ASCENDING = 1;

        /**
         * Constant for use with {@link #orderBy}
         */
        public static final int ORDER_DESCENDING = 2;

        private long[] mIds = null;
        private Integer mStatusFlags = null;
        private String mOrderByColumn = Impl.COLUMN_LAST_MODIFICATION;
        private int mOrderDirection = ORDER_DESCENDING;

        /**
         * Include only the downloads with the given IDs.
         * @return this object
         */
        public Query setFilterById(long... ids) {
            mIds = ids;
            return this;
        }

        /**
         * Include only downloads with status matching any the given status flags.
         * @param flags any combination of the STATUS_* bit flags
         * @return this object
         */
        public Query setFilterByStatus(int flags) {
            mStatusFlags = flags;
            return this;
        }

        /**
         * Change the sort order of the returned Cursor.
         *
         * @param column one of the COLUMN_* constants; currently, only
         *         {@link #COLUMN_LAST_MODIFIED_TIMESTAMP} and {@link #COLUMN_TOTAL_SIZE_BYTES} are
         *         supported.
         * @param direction either {@link #ORDER_ASCENDING} or {@link #ORDER_DESCENDING}
         * @return this object
         */
        public Query orderBy(String column, int direction) {
            if (direction != ORDER_ASCENDING && direction != ORDER_DESCENDING) {
                throw new IllegalArgumentException("Invalid direction: " + direction);
            }

            if (column.equals(Impl.COLUMN_LAST_MODIFICATION)) {
                mOrderByColumn = Impl.COLUMN_LAST_MODIFICATION;
            } else if (column.equals(Impl.COLUMN_TOTAL_BYTES)) {
                mOrderByColumn = Impl.COLUMN_TOTAL_BYTES;
            } else {
                throw new IllegalArgumentException("Cannot order by " + column);
            }
            mOrderDirection = direction;
            return this;
        }

        /**
         * Run this query using the given ContentResolver.
         * @param projection the projection to pass to ContentResolver.query()
         * @return the Cursor returned by ContentResolver.query()
         */
        Cursor runQuery(ContentResolver resolver, String[] projection, Uri baseUri) {
            Uri uri = baseUri;
            List<String> selectionParts = new ArrayList<String>();
            String[] selectionArgs = null;

            if (mIds != null) {
                selectionParts.add(getWhereClauseForIds(mIds));
                selectionArgs = getWhereArgsForIds(mIds);
            }

            if (mStatusFlags != null) {
                List<String> parts = new ArrayList<String>();
                if ((mStatusFlags & STATUS_PENDING) != 0) {
                    parts.add(statusClause("=", Impl.STATUS_PENDING));
                }
                if ((mStatusFlags & STATUS_RUNNING) != 0) {
                    parts.add(statusClause("=", Impl.STATUS_RUNNING));
                }
                if ((mStatusFlags & STATUS_PAUSED) != 0) {
                    parts.add(statusClause("=", Impl.STATUS_PAUSED_BY_APP));
                    parts.add(statusClause("=", Impl.STATUS_WAITING_TO_RETRY));
                    parts.add(statusClause("=", Impl.STATUS_WAITING_FOR_NETWORK));
                    parts.add(statusClause("=", Impl.STATUS_QUEUED_FOR_WIFI));
                }
                if ((mStatusFlags & STATUS_SUCCESSFUL) != 0) {
                    parts.add(statusClause("=", Impl.STATUS_SUCCESS));
                }
                if ((mStatusFlags & STATUS_FAILED) != 0) {
                    parts.add("(" + statusClause(">=", 400)
                              + " AND " + statusClause("<", 600) + ")");
                }
                selectionParts.add(joinStrings(" OR ", parts));
            }

            // only return rows which are not marked 'deleted = 1'
            selectionParts.add(Impl.COLUMN_DELETED + " != '1'");

            String selection = joinStrings(" AND ", selectionParts);
            String orderDirection = (mOrderDirection == ORDER_ASCENDING ? "ASC" : "DESC");
            String orderBy = mOrderByColumn + " " + orderDirection;

            return resolver.query(uri, projection, selection, selectionArgs, orderBy);
        }

        private String joinStrings(String joiner, Iterable<String> parts) {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (String part : parts) {
                if (!first) {
                    builder.append(joiner);
                }
                builder.append(part);
                first = false;
            }
            return builder.toString();
        }

        private String statusClause(String operator, int value) {
            return Impl.COLUMN_STATUS + operator + "'" + value + "'";
        }
    }

    
    /**
     * Implementation details
     * 
     * Exposes constants used to interact with the download manager's content provider. The
     * constants URI ... STATUS are the names of columns in the downloads table.
     * 
     */
    public static final class Impl implements BaseColumns {
        private Impl() {
        }
        
        /**
         * An identifier for a particular download, unique across the system.  Clients use this ID to
         * make subsequent calls related to the download.
         */
         public final static String COLUMN_ID = BaseColumns._ID;
        
        /**
         * The name of the column containing the URI of the data being downloaded.
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_URI = "uri";

        /**
         * The name of the column containing application-specific data.
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read/Write</P>
         */
        public static final String COLUMN_APP_DATA = "entity";

        /**
         * The name of the column containing the filename that the initiating
         * application recommends. When possible, the download manager will attempt
         * to use this filename, or a variation, as the actual name for the file.
         * <P>Type: TEXT</P>
         * <P>Owner can Init</P>
         */
        public static final String COLUMN_FILE_NAME_HINT = "hint";

        /**
         * The name of the column containing the filename where the downloaded data
         * was actually stored.
         * <P>Type: TEXT</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_DATA = "_data";

        /**
         * The name of the column containing the MIME type of the downloaded data.
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_MIME_TYPE = "mimetype";

        /**
         * The name of the column containing the flag that controls the destination
         * of the download. See the DESTINATION_* constants for a list of legal values.
         * <P>Type: INTEGER</P>
         * <P>Owner can Init</P>
         */
        public static final String COLUMN_DESTINATION = "destination";

        /**
         * The name of the column containing the flags that controls whether the
         * download is displayed by the UI. See the VISIBILITY_* constants for
         * a list of legal values.
         * <P>Type: INTEGER</P>
         * <P>Owner can Init/Read/Write</P>
         */
        public static final String COLUMN_VISIBILITY = "visibility";

        /**
         * The name of the column containing the current control state  of the download.
         * Applications can write to this to control (pause/resume) the download.
         * the CONTROL_* constants for a list of legal values.
         * <P>Type: INTEGER</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_CONTROL = "control";

        /**
         * The name of the column containing the current status of the download.
         * Applications can read this to follow the progress of each download. See
         * the STATUS_* constants for a list of legal values.
         * <P>Type: INTEGER</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_STATUS = "status";
        
        /**
         * The times of trying to get contact with server but failed
         * <P>Type: INTEGER</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_FAILED_CONNECTIONS = "numfailed";
        
        /**
         * The times of request meet the HTTP redirect directives 
         * <P>Type: INTEGER</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_RETRY_AFTER_REDIRECT_COUNT = "redirectcount";
        
        /** 
         * The column that is used for the downloads's ETag(Guarantee the file integrity) 
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_ETAG = "etag";
        
        /** 
         * The column that is used to remember where download request comes from
         *  <P>Type: INTEGER</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_SOURCE = "source";
        
        /** 
         * The column that is used for the initiating app's MD5 
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_MD5 = "md5";

        /**
         * The name of the column containing the date at which some interesting
         * status changed in the download. Stored as a System.currentTimeMillis()
         * value.
         * <P>Type: BIGINT</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_LAST_MODIFICATION = "lastmod";

        /**
         * The name of the column containing the package name of the application
         * that initiating the download. The download manager will send
         * notifications to a component in this package when the download completes.
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_NOTIFICATION_PACKAGE = "notificationpackage";

        /**
         * The name of the column containing the component name of the class that
         * will receive notifications associated with the download. The
         * package/class combination is passed to
         * Intent.setClassName(String,String).
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read</P>
         */
        public static final String COLUMN_NOTIFICATION_CLASS = "notificationclass";

        /**
         * If extras are specified when requesting a download they will be provided in the intent that
         * is sent to the specified class and package when a download has finished.
         * <P>Type: TEXT</P>
         * <P>Owner can Init</P>
         */
        public static final String COLUMN_NOTIFICATION_EXTRAS = "notificationextras";

        /**
         * The name of the column containing the total size of the file being
         * downloaded.
         * <P>Type: INTEGER</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_TOTAL_BYTES = "total_bytes";

        /**
         * The name of the column containing the size of the part of the file that
         * has been downloaded so far.
         * <P>Type: INTEGER</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_CURRENT_BYTES = "current_bytes";

        /**
         * The name of the column where the initiating application can provided the
         * title of this download. The title will be displayed ito the user in the
         * list of downloads.
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read/Write</P>
         */
        public static final String COLUMN_TITLE = "title";

        /**
         * The name of the column where the initiating application can provide the
         * description of this download. The description will be displayed to the
         * user in the list of downloads.
         * <P>Type: TEXT</P>
         * <P>Owner can Init/Read/Write</P>
         */
        public static final String COLUMN_DESCRIPTION = "description";

        /**
         * Set to true if this download is deleted. It is completely removed from the database
         * when MediaProvider database also deletes the metadata asociated with this downloaded file.
         * <P>Type: BOOLEAN</P>
         * <P>Owner can Read</P>
         */
        public static final String COLUMN_DELETED = "deleted";

        /**
         * The package name of the APK file. This not necessary for other files.
         */
        public static final String COLUMN_PACKAGE_NAME = "package_name";

        /*
         * Lists the destinations that an application can specify for a download.
         */
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

        /**
         * This download will be saved to the download manager's private
         * partition and will be purged as necessary to make space. This is
         * for private files (similar to CACHE_PARTITION) that aren't deleted
         * immediately after they are used, and are kept around by the download
         * manager as long as space is available.
         */
        public static final int DESTINATION_CACHE_PARTITION_PURGEABLE = 2;

        /**
         * This download will be saved to the location given by the file URI in
         * {@link #COLUMN_FILE_NAME_HINT}.
         */
        public static final int DESTINATION_FILE_URI = 3;

        /**
         * This download is allowed to run.
         */
        public static final int CONTROL_RUN = 0;

        /**
         * This download must pause at the first opportunity.
         */
        public static final int CONTROL_PAUSED = 1;
        
        /**
         * This download is in the task waiting queue 
         */
        public static final int CONTROL_PENDING = 2;

        /*
         * Lists the states that the download manager can set on a download
         * to notify applications of the download progress.
         * The codes follow the HTTP families:<br>
         * 1xx: informational<br>
         * 2xx: success<br>
         * 3xx: redirects (not used by the download manager)<br>
         * 4xx: client errors<br>
         * 5xx: server errors
         */

        /**
         * Returns whether the status is informational (i.e. 1xx).
         */
        public static boolean isStatusInformational(int status) {
            return (status >= 100 && status < 200);
        }
        
        /**
         * 下载进行中
         */
        public static boolean isStatusRunning(int status) {
            return status == STATUS_RUNNING;
        }
        
        /**
         * 下载等待中
         */
        public static boolean isStatusPending(int status) {
            return status == STATUS_PENDING 
                    || status == STATUS_PAUSED_BY_APP
                    || status == STATUS_WAITING_TO_RETRY 
                    || status == STATUS_WAITING_FOR_NETWORK
                    || status == STATUS_QUEUED_FOR_WIFI;
        }

        /**
         * Returns whether the status is a success (i.e. 2xx).
         */
        public static boolean isStatusSuccess(int status) {
            return (status >= 200 && status < 300);
        }

        /**
         * Returns whether the status is an error (i.e. 4xx or 5xx).
         */
        public static boolean isStatusError(int status) {
            return (status >= 400 && status < 600);
        }

        /**
         * Returns whether the status is a client error (i.e. 4xx).
         */
        public static boolean isStatusClientError(int status) {
            return (status >= 400 && status < 500);
        }

        /**
         * Returns whether the status is a server error (i.e. 5xx).
         */
        public static boolean isStatusServerError(int status) {
            return (status >= 500 && status < 600);
        }

        /**
         * Returns whether the download has completed (either with success or
         * error).
         */
        public static boolean isStatusCompleted(int status) {
            return (status >= 200 && status < 300) || (status >= 400 && status < 600);
        }

        /**
         * This download hasn't stated yet
         */
        public static final int STATUS_PENDING = 190;

        /**
         * This download has started
         */
        public static final int STATUS_RUNNING = 192;

        /**
         * This download has been paused by the owning app.
         */
        public static final int STATUS_PAUSED_BY_APP = 193;

        /**
         * This download encountered some network error and is waiting before retrying the request.
         */
        public static final int STATUS_WAITING_TO_RETRY = 194;

        /**
         * This download is waiting for network connectivity to proceed.
         */
        public static final int STATUS_WAITING_FOR_NETWORK = 195;

        /**
         * This download exceeded a size limit for mobile networks and is waiting for a Wi-Fi
         * connection to proceed.
         */
        public static final int STATUS_QUEUED_FOR_WIFI = 196;

        /**
         * This download has successfully completed.
         * Warning: there might be other status values that indicate success
         * in the future.
         * Use isSucccess() to capture the entire category.
         */
        public static final int STATUS_SUCCESS = 200;
        
        /**
         * This downloaded apk has successfully installed.
         */
        public static final int STATUS_INSTALLED = 260;

        /**
         * This request couldn't be parsed. This is also used when processing
         * requests with unknown/unsupported URI schemes.
         */
        public static final int STATUS_BAD_REQUEST = 400;

        /**
         * This download can't be performed because the content type cannot be
         * handled.
         */
        public static final int STATUS_NOT_ACCEPTABLE = 406;

        /**
         * This download cannot be performed because the length cannot be
         * determined accurately. This is the code for the HTTP error "Length
         * Required", which is typically used when making requests that require
         * a content length but don't have one, and it is also used in the
         * client when a response is received whose length cannot be determined
         * accurately (therefore making it impossible to know when a download
         * completes).
         */
        public static final int STATUS_LENGTH_REQUIRED = 411;

        /**
         * This download was interrupted and cannot be resumed.
         * This is the code for the HTTP error "Precondition Failed", and it is
         * also used in situations where the client doesn't have an ETag at all.
         */
        public static final int STATUS_PRECONDITION_FAILED = 412;

        /**
         * the MD5 check error 
         */
        public static final int STATUS_FILE_MD5_ERROR = 486;
        
        /**
         * The lowest-valued error status that is not an actual HTTP status code.
         */
        public static final int STATUS_MIN_ARTIFICIAL_ERROR_STATUS = 487;

        /**
         * The requested destination file already exists.
         */
        public static final int STATUS_FILE_ALREADY_EXISTS_ERROR = 488;

        /**
         * Some possibly transient error occurred, but we can't resume the download.
         */
        public static final int STATUS_CANNOT_RESUME = 489;

        /**
         * This download was canceled
         */
        public static final int STATUS_CANCELED = 490;

        /**
         * This download has completed with an error.
         * Warning: there will be other status values that indicate errors in
         * the future. Use isStatusError() to capture the entire category.
         */
        public static final int STATUS_UNKNOWN_ERROR = 491;

        /**
         * This download couldn't be completed because of a storage issue.
         * Typically, that's because the filesystem is missing or full.
         * Use the more specific {@link #STATUS_INSUFFICIENT_SPACE_ERROR}
         * and {@link #STATUS_DEVICE_NOT_FOUND_ERROR} when appropriate.
         */
        public static final int STATUS_FILE_ERROR = 492;

        /**
         * This download couldn't be completed because of an HTTP
         * redirect response that the download manager couldn't
         * handle.
         */
        public static final int STATUS_UNHANDLED_REDIRECT = 493;

        /**
         * This download couldn't be completed because of an
         * unspecified unhandled HTTP code.
         */
        public static final int STATUS_UNHANDLED_HTTP_CODE = 494;

        /**
         * This download couldn't be completed because of an
         * error receiving or processing data at the HTTP level.
         */
        public static final int STATUS_HTTP_DATA_ERROR = 495;

        /**
         * This download couldn't be completed because of an
         * HttpException while setting up the request.
         */
        public static final int STATUS_HTTP_EXCEPTION = 496;

        /**
         * This download couldn't be completed because there were
         * too many redirects.
         */
        public static final int STATUS_TOO_MANY_REDIRECTS = 497;

        /**
         * This download couldn't be completed due to insufficient storage
         * space.  Typically, this is because the SD card is full.
         */
        public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 498;

        /**
         * This download couldn't be completed because no external storage
         * device was found.  Typically, this is because the SD card is not
         * mounted.
         */
        public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 499;

        /**
         * This download is visible but only shows in the notifications
         * while it's in progress.
         */
        public static final int VISIBILITY_VISIBLE = 0;

        /**
         * This download is visible and shows in the notifications while
         * in progress and after completion.
         */
        public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;

        /**
         * This download doesn't show in the UI or in the notifications.
         */
        public static final int VISIBILITY_HIDDEN = 2;
        
        /**
         * The content:// URI to access downloads owned by the caller's UID.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://gfan_downloads/my_downloads");
        
    }
}