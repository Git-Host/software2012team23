package at.tugraz.ist.akm.trace;


public class Logable {

	private String mTag = "<notag>";

	public Logable() {
	}

	public Logable(String tag) {
		mTag = new String(tag);
	}

	public void logV(String message) {
		logV(message, null);
	}

	public void logV(String message, Throwable t) {
		Logger.log(Logger.LogLevel.VERBOSE, mTag, message, t);
	}

	public void logE(String message) {
		logE(message, null);
	}

	public void logE(String message, Throwable t) {
		Logger.log(Logger.LogLevel.ERROR, mTag, message, t);
	}

	public void logW(String message) {
		logW(message, null);
	}

	public void logW(String message, Throwable t) {
		Logger.log(Logger.LogLevel.WARNING, mTag, message, t);
	}

	public void logI(String message) {
		logI(message, null);
	}

	public void logI(String message, Throwable t) {
		Logger.log(Logger.LogLevel.INFO, mTag, message, t);
	}

	public void logD(String message) {
		logD(message, null);
	}

	public void logD(String message, Throwable t) {
		Logger.log(Logger.LogLevel.DEBUG, mTag, message, t);
	}
}
