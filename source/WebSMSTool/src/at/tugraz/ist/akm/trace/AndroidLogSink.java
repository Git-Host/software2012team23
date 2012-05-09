package at.tugraz.ist.akm.trace;

import android.util.Log;

public class AndroidLogSink implements LogSink {

	@Override
	public void e(final String tag, final String message) {
		Log.e(tag, message);
	}

	@Override
	public void w(final String tag, final String message) {
		Log.w(tag, message);
	}

	@Override
	public void i(final String tag, final String message) {
		Log.i(tag, message);
	}

	@Override
	public void d(final String tag, final String message) {
		Log.d(tag, message);
	}

	@Override
	public void v(final String tag, final String message) {
		Log.v(tag, message);
	}
}
