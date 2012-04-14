package at.tugraz.ist.akm.sms;

import android.net.Uri;

public class SmsContent {
	public static class ContentUri {
		public final static Uri BASE_URI = Uri.parse("content://sms");
		public final static Uri INBOX_URI = Uri.withAppendedPath(BASE_URI,
				"inbox");
		public final static Uri OUTBOX_URI = Uri.withAppendedPath(BASE_URI,
				"outbox");
		public final static Uri SENT_URI = Uri.withAppendedPath(BASE_URI,
				"sent");
		public final static Uri DRAFT_URI = Uri.withAppendedPath(BASE_URI,
				"draft");
		public final static Uri UNDELIVERED_URI = Uri.withAppendedPath(
				BASE_URI, "undelivered");
		public final static Uri FAILED_URI = Uri.withAppendedPath(BASE_URI,
				"failed");
		public final static Uri QUEUED_URI = Uri.withAppendedPath(BASE_URI,
				"queued");
	}

	public static class Content {
		public final static String ID = "_id";
		public final static String THREAD_ID = "thread_id";
		public final static String ADDRESS = "address";
		public final static String PERSON = "person";
		public final static String DATE = "date";
		public final static String PROTOCOL = "protocol";
		public final static String READ = "read";
		public final static String STATUS = "status";
		public final static String TYPE = "type";
		public final static String REPLY_PATH_PRESENT = "reply_path_present";
		public final static String SUBJECT = "subject";
		public final static String BODY = "body";
		public final static String SERVICE_CENTER = "service_center";
		public final static String LOCKED = "locked";
		public final static String ERROR_CODE = "error_code";
		public final static String SEEN = "seen";
	}
}
