package at.tugraz.ist.akm.texting;

public class VolatileOutgoingReport {

	public VolatileOutgoingReport(final VolatileOutgoingReport src) {
		numPending = src.numPending;
		numErroneous = src.numErroneous;
	}

	public VolatileOutgoingReport() {
	}

	public int numPending = 0;

	public int numErroneous = 0;

}
