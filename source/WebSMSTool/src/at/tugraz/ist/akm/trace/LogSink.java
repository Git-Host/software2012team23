package at.tugraz.ist.akm.trace;

public interface LogSink {

	public void w(final String tag, final String message);

	public void i(final String tag, final String message);

	public void d(final String tag, final String message);

	public void e(final String tag, final String message);

	public void v(final String tag, final String message);

}
