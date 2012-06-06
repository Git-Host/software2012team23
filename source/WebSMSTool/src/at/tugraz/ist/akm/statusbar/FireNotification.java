package at.tugraz.ist.akm.statusbar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import at.tugraz.ist.akm.MainActivity;
import at.tugraz.ist.akm.R;

public class FireNotification {

	public static class NotificationInfo {
		public String title = null;
		public String text = null;
	}

	private static int NOTIFICATION_ID = 1;
	private Context mContext = null;
	private NotificationManager mNotificationManager = null;

	public FireNotification(Context context) {
		mContext = context;
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) mContext
				.getSystemService(ns);
	}

	public void fireStickyInfos(NotificationInfo wInfos) {
		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "Hello";
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);

		Intent notificationIntent = new Intent(mContext, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(mContext, wInfos.title, wInfos.text,
				contentIntent);
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.flags = Notification.FLAG_NO_CLEAR;
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	public void cancelAll() {
		mNotificationManager.cancelAll();
	}
}
