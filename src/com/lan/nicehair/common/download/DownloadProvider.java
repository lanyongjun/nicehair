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
import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import com.lan.nicehair.utils.AppLog;
import com.lan.nicehair.utils.Utils;

/**
 * Allows application to interact with the download manager.
 */
public final class DownloadProvider extends ContentProvider {
    
	private final String TAG="DownloadProvider";
    /** Database filename */
    private static final String DB_NAME = "downloads.db";
    /** Current database version */
    private static final int DB_VERSION = 108;
    /** Name of table in the database */
    private static final String DB_TABLE = "downloads";

    /** MIME type for the entire download list */
    private static final String DOWNLOAD_LIST_TYPE = "vnd.android.cursor.dir/download";
    /** MIME type for an individual download */
    private static final String DOWNLOAD_TYPE = "vnd.android.cursor.item/download";

    /** URI matcher used to recognize URIs sent by applications */
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    /** URI matcher constant for the URI of all downloads belonging to the calling UID */
    private static final int MY_DOWNLOADS = 1;
    /** URI matcher constant for the URI of an individual download belonging to the calling UID */
    private static final int MY_DOWNLOADS_ID = 2;
    
    static {
        sURIMatcher.addURI("gfan_downloads", "my_downloads", MY_DOWNLOADS);
        sURIMatcher.addURI("gfan_downloads", "my_downloads/#", MY_DOWNLOADS_ID);
    }

    /** The database that lies underneath this content provider */
    private SQLiteOpenHelper mOpenHelper = null;

    /**
     * This class encapsulates a SQL where clause and its parameters.  It makes it possible for
     * shared methods (like {@link DownloadProvider#getWhereClause(Uri, String, String[], int)})
     * to return both pieces of information, and provides some utility logic to ease piece-by-piece
     * construction of selections.
     */
    private static class SqlSelection {
        public StringBuilder mWhereClause = new StringBuilder();
        public List<String> mParameters = new ArrayList<String>();

        public <T> void appendClause(String newClause, final T... parameters) {
            if (TextUtils.isEmpty(newClause)) {
                return;
            }
            if (mWhereClause.length() != 0) {
                mWhereClause.append(" AND ");
            }
            mWhereClause.append("(");
            mWhereClause.append(newClause);
            mWhereClause.append(")");
            if (parameters != null) {
                for (Object parameter : parameters) {
                    mParameters.add(parameter.toString());
                }
            }
        }

        public String getSelection() {
            return mWhereClause.toString();
        }

        public String[] getParameters() {
            String[] array = new String[mParameters.size()];
            return mParameters.toArray(array);
        }
    }

    /**
     * Creates and updated database on demand when opening it.
     * Helper class to create database the first time the provider is
     * initialized and upgrade it when a new version of the provider needs
     * an updated version of the database.
     */
    private final class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        /**
         * Creates database the first time we try to open it.
         */
        @Override
        public void onCreate(final SQLiteDatabase db) {
            AppLog.d(TAG,"populating new database");
            onUpgrade(db, 0, DB_VERSION);
        }

        /**
         * Updates the database format when a content provider is used
         * with a database that was created with a different format.
         *
         * Note: to support downgrades, creating a table should always drop it first if it already
         * exists.
         */
        @Override
        public void onUpgrade(final SQLiteDatabase db, int oldV, final int newV) {
        	createDownloadsTable(db);
        }

        /**
         * Creates the table that'll hold the download information.
         */
        private void createDownloadsTable(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
                db.execSQL("CREATE TABLE " + DB_TABLE + "(" +
                        DownloadManager.Impl._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        DownloadManager.Impl.COLUMN_URI + " TEXT, " +
                        DownloadManager.Impl.COLUMN_RETRY_AFTER_REDIRECT_COUNT + " INTEGER, " +
                        DownloadManager.Impl.COLUMN_APP_DATA + " TEXT, " +
                        DownloadManager.Impl.COLUMN_FILE_NAME_HINT + " TEXT, " +
                        DownloadManager.Impl.COLUMN_DATA + " TEXT, " +
                        DownloadManager.Impl.COLUMN_MIME_TYPE + " TEXT, " +
                        DownloadManager.Impl.COLUMN_DESTINATION + " INTEGER, " +
                        DownloadManager.Impl.COLUMN_VISIBILITY + " INTEGER, " +
                        DownloadManager.Impl.COLUMN_CONTROL + " INTEGER, " +
                        DownloadManager.Impl.COLUMN_STATUS + " INTEGER, " +
                        DownloadManager.Impl.COLUMN_FAILED_CONNECTIONS + " INTEGER, " +
                        DownloadManager.Impl.COLUMN_LAST_MODIFICATION + " BIGINT, " +
                        DownloadManager.Impl.COLUMN_NOTIFICATION_PACKAGE + " TEXT, " +
                        DownloadManager.Impl.COLUMN_NOTIFICATION_CLASS + " TEXT, " +
                        DownloadManager.Impl.COLUMN_NOTIFICATION_EXTRAS + " TEXT, " +
                        DownloadManager.Impl.COLUMN_TOTAL_BYTES + " INTEGER DEFAULT -1, " +
                        DownloadManager.Impl.COLUMN_CURRENT_BYTES + " INTEGER DEFAULT 0, " +
                        DownloadManager.Impl.COLUMN_ETAG + " TEXT, " +
                        DownloadManager.Impl.COLUMN_MD5 + " TEXT, " +
                        DownloadManager.Impl.COLUMN_PACKAGE_NAME + " TEXT, " +
                        DownloadManager.Impl.COLUMN_TITLE + " TEXT, " +
                        DownloadManager.Impl.COLUMN_DESCRIPTION + " TEXT, " +
                        DownloadManager.Impl.COLUMN_DELETED + " BOOLEAN NOT NULL DEFAULT 0, " +
                        DownloadManager.Impl.COLUMN_SOURCE + " INTEGER);");
            } catch (SQLException ex) {
            	AppLog.e(TAG,"couldn't create table in downloads database");
                throw ex;
            }
        }
    }
    
    /**
     * Initializes the content provider when it is created.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * Returns the content-provider-style MIME types of the various
     * types accessible through this content provider.
     */
    @Override
    public String getType(final Uri uri) {
        int match = sURIMatcher.match(uri);
        switch (match) {
            case MY_DOWNLOADS: {
                return DOWNLOAD_LIST_TYPE;
            }
            case MY_DOWNLOADS_ID: {
                return DOWNLOAD_TYPE;
            }
            default: {
            	AppLog.d(TAG,"calling getType on an unknown URI: " + uri);
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
    }

    /**
     * Inserts a row in the database
     */
    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        ContentValues filteredValues = new ContentValues();
        copyString(DownloadManager.Impl.COLUMN_URI, values, filteredValues);
        copyString(DownloadManager.Impl.COLUMN_APP_DATA, values, filteredValues);
        copyString(DownloadManager.Impl.COLUMN_FILE_NAME_HINT, values, filteredValues);
        copyString(DownloadManager.Impl.COLUMN_MIME_TYPE, values, filteredValues);
        copyString(DownloadManager.Impl.COLUMN_PACKAGE_NAME, values, filteredValues);
        copyString(DownloadManager.Impl.COLUMN_MD5, values, filteredValues);
        copyInteger(DownloadManager.Impl.COLUMN_DESTINATION, values, filteredValues);
//        Integer dest = values.getAsInteger(DownloadManager.Impl.COLUMN_DESTINATION);
//        if(dest == DownloadManager.Impl.DESTINATION_EXTERNAL) {
//        	filteredValues.put(DownloadManager.Impl.COLUMN_VISIBILITY,
//        			DownloadManager.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        } else {
//        	filteredValues.put(DownloadManager.Impl.COLUMN_VISIBILITY,
//        			DownloadManager.Impl.VISIBILITY_HIDDEN);
//        }
        copyInteger(DownloadManager.Impl.COLUMN_VISIBILITY, values, filteredValues);
        copyInteger(DownloadManager.Impl.COLUMN_CONTROL, values, filteredValues);
        copyInteger(DownloadManager.Impl.COLUMN_SOURCE, values, filteredValues);
        filteredValues.put(DownloadManager.Impl.COLUMN_STATUS, DownloadManager.Impl.STATUS_PENDING);
        filteredValues.put(DownloadManager.Impl.COLUMN_LAST_MODIFICATION,
                           System.currentTimeMillis());
        String pckg = values.getAsString(DownloadManager.Impl.COLUMN_NOTIFICATION_PACKAGE);
        String clazz = values.getAsString(DownloadManager.Impl.COLUMN_NOTIFICATION_CLASS);
        if (pckg != null) {
            filteredValues.put(DownloadManager.Impl.COLUMN_NOTIFICATION_PACKAGE, pckg);
            if (clazz != null) {
                filteredValues.put(DownloadManager.Impl.COLUMN_NOTIFICATION_CLASS, clazz);
            }
        }
        copyString(DownloadManager.Impl.COLUMN_NOTIFICATION_EXTRAS, values, filteredValues);
        copyStringWithDefault(DownloadManager.Impl.COLUMN_TITLE, values, filteredValues, "");
        copyStringWithDefault(DownloadManager.Impl.COLUMN_DESCRIPTION, values, filteredValues, "");
        filteredValues.put(DownloadManager.Impl.COLUMN_TOTAL_BYTES, -1);
        filteredValues.put(DownloadManager.Impl.COLUMN_CURRENT_BYTES, 0);

        Context context = getContext();
        context.startService(new Intent(context, DownloadService.class));

        long rowID = db.insert(DB_TABLE, null, filteredValues);
        if (rowID == -1) {
        	AppLog.d(TAG,"couldn't insert into downloads database");
            return null;
        }

        context.startService(new Intent(context, DownloadService.class));
        notifyContentChanged(uri, sURIMatcher.match(uri));
        return ContentUris.withAppendedId(DownloadManager.Impl.CONTENT_URI, rowID);
    }

//    /**
//     * Check that the file URI provided for DESTINATION_FILE_URI is valid.
//     */
//    private void checkFileUriDestination(ContentValues values) {
//        String fileUri = values.getAsString(DownloadManager.Impl.COLUMN_FILE_NAME_HINT);
//        if (fileUri == null) {
//            throw new IllegalArgumentException(
//                    "DESTINATION_FILE_URI must include a file URI under COLUMN_FILE_NAME_HINT");
//        }
//        Uri uri = Uri.parse(fileUri);
//        String scheme = uri.getScheme();
//        if (scheme == null || !scheme.equals("file")) {
//            throw new IllegalArgumentException("Not a file URI: " + uri);
//        }
//        String path = uri.getPath();
//        if (path == null) {
//            throw new IllegalArgumentException("Invalid file URI: " + uri);
//        }
//        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        if (!path.startsWith(externalPath)) {
//            throw new SecurityException("Destination must be on external storage: " + uri);
//        }
//    }

    /**
     * Starts a database query
     */
    @Override
    public Cursor query(final Uri uri, String[] projection,
             final String selection, final String[] selectionArgs,
             final String sort) {

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        int match = sURIMatcher.match(uri);
        if (match == -1) {
        	AppLog.d(TAG,"querying unknown URI: " + uri);
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SqlSelection fullSelection = getWhereClause(uri, selection, selectionArgs, match);

        // print the query info
        logVerboseQueryInfo(projection, selection, selectionArgs, sort, db);

        Cursor ret = db.query(DB_TABLE, projection, fullSelection.getSelection(),
                fullSelection.getParameters(), null, null, sort);

        if (ret != null) {
           ret = new ReadOnlyCursorWrapper(ret);
        }

        if (ret != null) {
            ret.setNotificationUri(getContext().getContentResolver(), uri);
        } else {
        	AppLog.d(TAG,"query failed in downloads database");
        }
        return ret;
    }

    /*
     * print the query info
     */
    private void logVerboseQueryInfo(String[] projection, final String selection,
            final String[] selectionArgs, final String sort, SQLiteDatabase db) {
        java.lang.StringBuilder sb = new java.lang.StringBuilder();
        sb.append("starting query, database is ");
        if (db != null) {
            sb.append("not ");
        }
        sb.append("null; ");
        if (projection == null) {
            sb.append("projection is null; ");
        } else if (projection.length == 0) {
            sb.append("projection is empty; ");
        } else {
            for (int i = 0; i < projection.length; ++i) {
                sb.append("projection[");
                sb.append(i);
                sb.append("] is ");
                sb.append(projection[i]);
                sb.append("; ");
            }
        }
        sb.append("selection is ");
        sb.append(selection);
        sb.append("; ");
        if (selectionArgs == null) {
            sb.append("selectionArgs is null; ");
        } else if (selectionArgs.length == 0) {
            sb.append("selectionArgs is empty; ");
        } else {
            for (int i = 0; i < selectionArgs.length; ++i) {
                sb.append("selectionArgs[");
                sb.append(i);
                sb.append("] is ");
                sb.append(selectionArgs[i]);
                sb.append("; ");
            }
        }
        sb.append("sort is ");
        sb.append(sort);
        sb.append(".");
        AppLog.d(TAG,sb.toString());
    }

    private String getDownloadIdFromUri(final Uri uri) {
        return uri.getPathSegments().get(1);
    }

    /**
     * Updates a row in the database
     */
    @Override
    public int update(final Uri uri, final ContentValues values,
            final String where, final String[] whereArgs) {

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count;
        boolean startService = false;

        if (values.containsKey(DownloadManager.Impl.COLUMN_DELETED)) {
            if (values.getAsInteger(DownloadManager.Impl.COLUMN_DELETED) == 1) {
                // some rows are to be 'deleted'. need to start DownloadService.
                startService = true;
            }
        }

        ContentValues filteredValues  = values;
        String filename = values.getAsString(DownloadManager.Impl.COLUMN_DATA);
        if (filename != null) {
            // update the download item titile
            Cursor c = query(uri, new String[] { DownloadManager.Impl.COLUMN_TITLE }, null, null,
                    null);
            if (!c.moveToFirst() || TextUtils.isEmpty(c.getString(0))) {
                values.put(DownloadManager.Impl.COLUMN_TITLE, new File(filename).getName());
            }
            c.close();
        }

        Integer status = values.getAsInteger(DownloadManager.Impl.COLUMN_STATUS);
        boolean isRestart = status != null && status == DownloadManager.Impl.STATUS_PENDING;
        if (isRestart) {
            startService = true;
        }

        int match = sURIMatcher.match(uri);
        switch (match) {
            case MY_DOWNLOADS:
            case MY_DOWNLOADS_ID:
                SqlSelection selection = getWhereClause(uri, where, whereArgs, match);
                if (filteredValues.size() > 0) {
                    
                	AppLog.d(TAG,"update database values  : " + filteredValues);
                    count = db.update(DB_TABLE, filteredValues, selection.getSelection(),
                            selection.getParameters());
                    if(count > 0) {
                    	startService = true;
                    }
                } else {
                    count = 0;
                }
                break;

            default:
            	AppLog.d(TAG,"updating unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot update URI: " + uri);
        }

        notifyContentChanged(uri, match);
        if (startService) {
            Context context = getContext();
            context.startService(new Intent(context, DownloadService.class));
        }
        return count;
    }

    /**
     * Notify of a change through both URIs (/my_downloads and /all_downloads)
     * @param uri either URI for the changed download(s)
     * @param uriMatch the match ID from {@link #sURIMatcher}
     */
    private void notifyContentChanged(final Uri uri, int uriMatch) {
        Long downloadId = null;
        if (uriMatch == MY_DOWNLOADS_ID) {
            downloadId = Long.parseLong(getDownloadIdFromUri(uri));
        }
        Uri uriToNotify = DownloadManager.Impl.CONTENT_URI;
        if (downloadId != null) {
            uriToNotify = ContentUris.withAppendedId(uriToNotify, downloadId);
        }
        getContext().getContentResolver().notifyChange(uriToNotify, null);
    }

    private SqlSelection getWhereClause(final Uri uri, final String where, final String[] whereArgs,
            int uriMatch) {
        SqlSelection selection = new SqlSelection();
        selection.appendClause(where, whereArgs);
        if (uriMatch == MY_DOWNLOADS_ID) {
            selection.appendClause(DownloadManager.Impl._ID + " = ?", getDownloadIdFromUri(uri));
        }
        return selection;
    }

    /**
     * Deletes a row in the database
     */
    @Override
    public int delete(final Uri uri, final String where,
            final String[] whereArgs) {

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        int match = sURIMatcher.match(uri);
        switch (match) {
            case MY_DOWNLOADS:
            case MY_DOWNLOADS_ID:
                SqlSelection selection = getWhereClause(uri, where, whereArgs, match);
                count = db.delete(DB_TABLE, selection.getSelection(), selection.getParameters());
                break;

            default:
            	AppLog.d(TAG,"deleting unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot delete URI: " + uri);
        }
        notifyContentChanged(uri, match);
        return count;
    }

    private static final void copyInteger(String key, ContentValues from, ContentValues to) {
        Integer i = from.getAsInteger(key);
        if (i != null) {
            to.put(key, i);
        }
    }

    private static final void copyString(String key, ContentValues from, ContentValues to) {
        String s = from.getAsString(key);
        if (s != null) {
            to.put(key, s);
        }
    }

    private static final void copyStringWithDefault(String key, ContentValues from,
            ContentValues to, String defaultValue) {
        copyString(key, from, to);
        if (!to.containsKey(key)) {
            to.put(key, defaultValue);
        }
    }

    private class ReadOnlyCursorWrapper extends CursorWrapper implements CrossProcessCursor {
        public ReadOnlyCursorWrapper(Cursor cursor) {
            super(cursor);
            mCursor = (CrossProcessCursor) cursor;
        }

//        public boolean deleteRow() {
//            throw new SecurityException("Download manager cursors are read-only");
//        }
//
//        public boolean commitUpdates() {
//            throw new SecurityException("Download manager cursors are read-only");
//        }

        public void fillWindow(int pos, CursorWindow window) {
            mCursor.fillWindow(pos, window);
        }

        public CursorWindow getWindow() {
            return mCursor.getWindow();
        }

        public boolean onMove(int oldPosition, int newPosition) {
            return mCursor.onMove(oldPosition, newPosition);
        }

        private CrossProcessCursor mCursor;
    }

}