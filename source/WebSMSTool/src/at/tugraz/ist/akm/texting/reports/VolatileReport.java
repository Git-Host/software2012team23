package at.tugraz.ist.akm.texting.reports;

import java.util.Date;

public class VolatileReport {

	private long mTimestamp = 0;

	public VolatileReport(final VolatileReport src) {
		mTimestamp = src.mTimestamp;
	}

	public VolatileReport() {
		update();
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	protected void update() {
		mTimestamp = millisecondNow();
	}

	private long millisecondNow() {
		return (new Date().getTime());
	}

}
