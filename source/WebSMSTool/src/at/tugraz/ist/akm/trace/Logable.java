package at.tugraz.ist.akm.trace;

import android.util.Log;

public class Logable {

	private enum LogLevel {
		ERROR, WARNING, INFO, DEBUG, VERBOSE
	}

	protected String mTag = null;

	public Logable() {
		mTag = new String("<notag>");
	}
	
	public Logable(String tag) {
		mTag = new String(tag);
	}

	public void v(String message) {
		v(message, null);
	}

	public void v(String message, Throwable t) {
		distributeLog(LogLevel.VERBOSE, message, t);
	}

	public void e(String message) {
		v(message, null);
	}

	public void e(String message, Throwable t) {
		distributeLog(LogLevel.ERROR, message, t);
	}

	public void w(String message) {
		w(message, null);
	}

	public void w(String message, Throwable t) {
		distributeLog(LogLevel.WARNING, message, t);
	}

	public void i(String message) {
		i(message, null);
	}

	public void i(String message, Throwable t) {
		distributeLog(LogLevel.INFO, message, t);
	}

	public void d(String message) {
		d(message, null);
	}

	public void d(String message, Throwable t) {
		distributeLog(LogLevel.DEBUG, message, t);
	}

	private void distributeLog(LogLevel level, String message, Throwable t) {
		switch (level) {
		case ERROR:
			Log.e(mTag, getLogMessage(message, t));
			break;
		case WARNING:
			Log.w(mTag, getLogMessage(message, t));
			break;

		case INFO:
			Log.i(mTag, getLogMessage(message, t));
			break;

		case DEBUG:
			Log.d(mTag, getLogMessage(message, t));
			break;

		case VERBOSE:
			Log.v(mTag, getLogMessage(message, t));
			break;
		}
	}

	private String getLogMessage(String message, Throwable t) {
		StringBuilder sb = new StringBuilder(message);
		if (t != null) {
			sb.append(" => ").append("exception <").append(t.getMessage())
					.append(">");
		}
		return sb.toString();
	}

}
