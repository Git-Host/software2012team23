/*
 * Copyright 2012 software2012team23
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	public SystemMonitor(Context context) {
		mContext = context;
		mTel = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);

	}

	public void start() {
		mLog.logVerbose("start: registering phone state listener");
		mTel.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
	}

	public void stop() {
		mLog.logVerbose("stop: unregistering phone state listener");
		mTel.listen(this, PhoneStateListener.LISTEN_NONE);
	}

	public BatteryStatus getBatteryStatus() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryInfos = mContext.registerReceiver(null, filter);
		return new BatteryStatus(mContext, batteryInfos);
	}

	public synchronized TelephonySignalStrength getSignalStrength() {
		if (null != mSingalStrength) {
			mLog.logVerbose("passing signal strenth to external caller");
			return new TelephonySignalStrength(mContext, mSingalStrength);
		} else {
			mLog.logError("FAILED to pass SignalStrength - no info available at the moment");	
			return null;
		}
	}

	private synchronized void setSignalStrength(SignalStrength signalStrength) {
		mLog.logVerbose("signal strength changed");
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
