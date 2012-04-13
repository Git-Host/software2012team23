package at.tugraz.ist.akm.sms;

import android.content.Context;
import android.content.Intent;

public interface SmsSendCallback {

	public void smsSentCallback(Context context, Intent intent);

	public void smsDeliveredCallback(Context context, Intent intent);

}
