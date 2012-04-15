package at.tugraz.ist.akm.sms;

public interface SmsReceivedCallback {

	/**
	 * informs that new sms has been received
	 * @param smsBox
	 */
	public void smsReceivedCallback();

}
