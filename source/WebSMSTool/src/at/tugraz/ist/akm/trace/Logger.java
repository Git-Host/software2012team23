package at.tugraz.ist.akm.trace;


public class Logger {

	private static Logger mLogger = null;
	private LogSink mSink = new AndroidLogSink();

	public enum LogLevel {
		ERROR, WARNING, INFO, DEBUG, VERBOSE
	}
	
	public static void log(final LogLevel level, final String tag, final String message, final Throwable t) {
		getInstance().distributeLog(level, tag, message, t);
	}

	public static void setSink(LogSink newSink) {
		getInstance().mSink = newSink;
	}
	
	private static Logger getInstance() {
		if (mLogger == null ) {
			mLogger = new Logger();
		}
		return mLogger;
	}
	
	private void distributeLog(final LogLevel level, final String tag,
			final String message, final Throwable t) {
		switch (level) {
		case ERROR:
			mSink.e(tag, formatMessage(message, t));
			break;
		case WARNING:
			mSink.w(tag, formatMessage(message, t));
			break;
	
		case INFO:
			mSink.i(tag, formatMessage(message, t));
			break;
	
		case DEBUG:
			mSink.d(tag, formatMessage(message, t));
			break;
	
		case VERBOSE:
			mSink.v(tag, formatMessage(message, t));
			break;
		}
	}

	private String formatMessage(final String message, final Throwable t) {
		StringBuilder sb = new StringBuilder(message);
		if (t != null) {
			sb.append(" => ").append("exception <").append(t.getMessage())
					.append(">");
		}
		return sb.toString();
	}

}
