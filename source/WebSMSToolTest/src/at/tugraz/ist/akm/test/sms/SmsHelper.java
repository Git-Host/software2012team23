package at.tugraz.ist.akm.test.sms;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.database.Cursor;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.trace.Logable;

public class SmsHelper {

	private static Logable mLog = new Logable(SmsHelper.class.getSimpleName());
	
	private static void log(final String message) {
		mLog.logVerbose(message);
	}
	
	public static String getDateNowString() {
		Date dateNow = new Date();
		SimpleDateFormat dateformat = new SimpleDateFormat("hh:mm:ss dd.MM.yyyy");
		StringBuilder now = new StringBuilder(dateformat.format(dateNow));
		return now.toString();

	}
	
	public static TextMessage getDummyTextMessage() {
		String methodName = Thread.currentThread().getStackTrace()[3]
				.getMethodName();
		TextMessage message = new TextMessage();
		message.setAddress("1357");
		message.setBody(methodName + ": Dummy texting generated on "
				+ getDateNowString() + ".");
		return message;
	}
	
	public static TextMessage getDummyMultiTextMessage() {
		String methodName = Thread.currentThread().getStackTrace()[3]
				.getMethodName();
		TextMessage message = new TextMessage();
		message.setAddress("13570");
		message.setBody(methodName + ": Dummy texting generated on "
				+ getDateNowString() + ". 1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		return message;
	}
	
	public static void logCursor(Cursor table) {
		table.moveToNext();
		StringBuffer cols = new StringBuffer();
		for (String col : table.getColumnNames()) {
			cols.append(col + " ");
		}
		log("cursor has [" + table.getCount() + "] entries and ["
				+ table.getColumnCount() + "] cols: " + cols.toString());
	}

	public static void logTextMessage(TextMessage message) {
		StringBuffer info = new StringBuffer();
		info.append("address: " + message.getAddress());
		info.append(" body: " + message.getBody());
		info.append(" date: " + message.getDate());
		info.append(" errorCode: " + message.getErrorCode());
		info.append(" locked: " + message.getLocked());
		info.append(" subject: " + message.getSubject());
		info.append(" person: " + message.getPerson());
		info.append(" protocol: " + message.getProtocol());
		info.append(" read: " + message.getRead());
		info.append(" replyPathPresent: " + message.getReplyPathPresent());
		info.append(" seen: " + message.getSeen());
		info.append(" serviceCenter: " + message.getServiceCenter());
		info.append(" status: " + message.getStatus());
		info.append(" id: " + message.getId());
		info.append(" threadId: " + message.getThreadId());
		info.append(" type: " + message.getType());
		log("test message {" + info.toString() + "}");
	}
}
