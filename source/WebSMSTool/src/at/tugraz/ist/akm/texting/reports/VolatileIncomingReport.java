package at.tugraz.ist.akm.texting.reports;

public class VolatileIncomingReport extends VolatileReport {

	public int mNumReceived = 0;

	public VolatileIncomingReport(final VolatileIncomingReport src) {
		super(src);
		mNumReceived = getNumReceived();
	}

	public VolatileIncomingReport() {
	}

	public int getNumReceived() {
		return mNumReceived;
	}

	public void setNumReceived(int num) {
		mNumReceived = num;
		super.update();
	}
}
