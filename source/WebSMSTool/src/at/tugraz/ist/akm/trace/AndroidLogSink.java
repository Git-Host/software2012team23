package at.tugraz.ist.akm.trace;

import android.util.Log;

public class AndroidLogSink implements LogSink {

	@Override
	public void error(final String tag, final String message) {
		Log.e(tag, message);
	}

	@Override
	public void warning(final String tag, final String message) {
		Log.w(tag, message);
	}

	@Override
	public void info(final String tag, final String message) {
		Log.i(tag, message);
	}

	@Override
	public void debug(final String tag, final String message) {
		Log.d(tag, message);
	}

	@Override
	public void verbose(final String tag, final String message) {
		Log.v(tag, message);
	}
}
