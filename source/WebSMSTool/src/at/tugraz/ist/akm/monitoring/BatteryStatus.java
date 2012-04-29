package at.tugraz.ist.akm.monitoring;

import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import at.tugraz.ist.akm.R;

public class BatteryStatus {

	private int mBatteryLevel = 0;
	private int mBatteryIconId = 0;
	private boolean mIsCharging = false;
	private boolean mIsFull = false;
	private int mChargePlug = 0;
	private boolean mISUsbCharge = false;
	private boolean mIsAcCharge = false;
	private Context mContext = null;

	public BatteryStatus(Context c, Intent batteryStatus) {
		mContext = c;
		int extraStatus = batteryStatus.getIntExtra(
				BatteryManager.EXTRA_STATUS, -1);
		mIsCharging = (extraStatus == BatteryManager.BATTERY_STATUS_CHARGING || extraStatus == BatteryManager.BATTERY_STATUS_FULL);

		mIsFull = (extraStatus == BatteryManager.BATTERY_STATUS_FULL);

		mChargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED,
				-1);
		mISUsbCharge = (mChargePlug == BatteryManager.BATTERY_PLUGGED_USB);

		mIsAcCharge = (mChargePlug == BatteryManager.BATTERY_PLUGGED_AC);

		mBatteryIconId = batteryStatus.getIntExtra(
				BatteryManager.EXTRA_ICON_SMALL, -1);

		mBatteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,
				-1);
	}

	public int getBatteryLevel() {
		return mBatteryLevel;
	}

	public int getBatteryIconId() {
		return mBatteryIconId;
	}

	public byte[] getBatteryIconBytes() {

		int batteryId = R.drawable.battery_level0;
		if (mBatteryLevel >= 80) {
			batteryId = R.drawable.battery_level4;
		} else if (mBatteryLevel >= 60) {
			batteryId = R.drawable.battery_level3;
		} else if (mBatteryLevel >= 40) {
			batteryId = R.drawable.battery_level2;
		} else if (mBatteryLevel >= 20) {
			batteryId = R.drawable.battery_level1;
		}

		Drawable d = mContext.getResources().getDrawable(batteryId);
		Bitmap b = ((BitmapDrawable) d).getBitmap();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		return stream.toByteArray();
	}

	public boolean getIsCharging() {
		return mIsCharging;
	}

	public boolean getIsFull() {
		return mIsFull;
	}

	public int getChargePlug() {
		return mChargePlug;
	}

	public boolean getIsUsbCharge() {
		return mISUsbCharge;
	}

	public boolean getIsAcCharge() {
		return mIsAcCharge;
	}
}
