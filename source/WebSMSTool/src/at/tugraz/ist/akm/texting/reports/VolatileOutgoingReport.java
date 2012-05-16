package at.tugraz.ist.akm.texting.reports;

public class VolatileOutgoingReport extends VolatileReport {
	private int mNumPending = 0;
	private int mNumErroneous = 0;

	public VolatileOutgoingReport(final VolatileOutgoingReport src) {
		super(src);
		mNumPending = src.mNumPending;
		mNumErroneous = src.mNumErroneous;
	}

	public VolatileOutgoingReport() {
	}

	public int getNumPending() {
		return mNumPending;
	}

	public void setNumPending(int numPending) {
		this.mNumPending = numPending;
		super.update();
	}

	public int getNumErroneous() {
		return mNumErroneous;
	}

	public void setNumErroneous(int numErroneous) {
		this.mNumErroneous = numErroneous;
		super.update();
	}
}
