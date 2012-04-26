package at.tugraz.ist.akm.texting.reports;

public class VolatilePhonebookReport extends VolatileReport {

	private int mNumChanges = 0;

	public VolatilePhonebookReport(final VolatilePhonebookReport src) {
		super(src);
		mNumChanges = src.mNumChanges;
	}
	
	public VolatilePhonebookReport() {
	}

	public void setNumChanges(int num) {
		super.update();
		mNumChanges = num;
	}

	public int getNumChanges() {
		return mNumChanges;
	}
}
