package at.tugraz.ist.akm.trace;

public interface LogSink {

	public void warning(final String tag, final String message);

	public void info(final String tag, final String message);

	public void debug(final String tag, final String message);

	public void error(final String tag, final String message);

	public void verbose(final String tag, final String message);

}
