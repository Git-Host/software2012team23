package at.tugraz.ist.akm.sms;

public class SmsContent{
	public class Uri {
		public static final String SMS = "content://sms";
		public static final String SMS_INBOX = "content://sms/inbox";
		public static final String SMS_OUTBOX = "content://sms/sent";
	};

	public class MessageColumns {
		public static final String ID = "_id";
		public static final String THREAD_ID = "thread_id";
		public static final String PERSON = "person";
		public static final String DATE = "date";
		public static final String ADDRESS = "address";
		public static final String SEEN = "seen";
		public static final String READ = "read";
		public static final String BODY = "body";
		public static final String PROTOCOL = "protocol";
		public static final String STATUS = "status";
		public static final String TYPE = "type";
		public static final String SERVICE_CENTER = "service_center";
		public static final String LOCKED = "locked";
	};
};
