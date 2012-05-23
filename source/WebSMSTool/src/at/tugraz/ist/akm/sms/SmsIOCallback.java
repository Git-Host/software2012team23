package at.tugraz.ist.akm.sms;

import java.util.List;

import android.content.Context;

public interface SmsIOCallback {

	public void smsSentCallback(Context context, List<TextMessage> messages);
	
	public void smsSentErrorCallback(Context context, List<TextMessage> messages);

	public void smsDeliveredCallback(Context context, List<TextMessage> messagea);
	
	public void smsReceivedCallback(Context context, List<TextMessage> messages);

}
