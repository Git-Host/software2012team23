package at.tugraz.ist.akm.monitoring;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import at.tugraz.ist.akm.trace.Logable;

public class SystemMonitor extends PhoneStateListener {

	private Context mContext = null;
	private TelephonyManager mTel = null;
	private SignalStrength mSingalStrength = null;
	private Logable mLog = new Logable(SystemMonitor.class.getSimpleName());

	public SystemMonitor(Context c) {
		mContext = c;
		mTel = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);

	}

	public void start() {
		mLog.logV("start: registering phone state listener");
		mTel.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
	}

	public void stop() {
		mLog.logV("stop: unregistering phone state listener");
		mTel.listen(this, PhoneStateListener.LISTEN_NONE);
	}

	public BatteryStatus getBatteryStatus() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryInfos = mContext.registerReceiver(null, filter);
		return new BatteryStatus(mContext, batteryInfos);
	}

	public synchronized TelephonySignalStrength getSignalStrength() {
		if (null != mSingalStrength) {
			mLog.logV("passing signal strenth to external caller");
			return new TelephonySignalStrength(mContext, mSingalStrength);
		} else {
			mLog.logE("FAILED to pass SignalStrength - no info available at the moment");	
			return null;
		}
	}

	private synchronized void setSignalStrength(SignalStrength signalStrength) {
		mLog.logV("signal strength changed");
		mSingalStrength = signalStrength;
	}

	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		super.onSignalStrengthsChanged(signalStrength);
		setSignalStrength(signalStrength);
	}
	
	@Override
	public void onSignalStrengthChanged(int asu) {
		super.onSignalStrengthChanged(asu);
	}
}
