package at.tugraz.ist.akm.sms;

import java.util.List;

import android.content.Context;

public interface SmsIOCallback {

	public void smsSentCallback(Context context, List<TextMessage> message);
	
	public void smsSentErrorCallback(Context context, List<TextMessage> message);

	public void smsDeliveredCallback(Context context, List<TextMessage> message);
	
	public void smsReceivedCallback(Context context, List<TextMessage> message);

}
