package com.lan.nicehair.common.download;

import java.util.Collection;

import android.app.NotificationManager;
import android.content.Context;

public class DownloadNotification {

	NotificationManager mNotificationManager;

    static final String LOGTAG = "DownloadNotification";
	public DownloadNotification(Context context) {
		mNotificationManager = (NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	 /*
     * Update the notification ui.
     */
    public void updateNotification(Collection<DownloadInfo> downloads) {
    
    }
	 /*
     * Update the completed notification item                                                                                
     */
    private void updateCompletedNotification(DownloadInfo download) {

    }
	 /*
     * Clear all notifications
     */
    public void clearAllNotification() {
    	if(mNotificationManager != null) {
    		mNotificationManager.cancelAll();
    	}
    }
    
    /*
     * Cancel notification use id 
     */
    public void cancelNotification(long id) {
    	if(mNotificationManager != null) {
			mNotificationManager.cancel((int) id);
    	}
    }
}
