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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.util.Base64;
import at.tugraz.ist.akm.R;

public class BatteryStatus
{

    private int mBatteryLevel = 0;
    private int mBatteryIconId = 0;
    private boolean mIsCharging = false;
    private boolean mIsFull = false;
    private int mChargePlug = 0;
    private boolean mISUsbCharge = false;
    private boolean mIsAcCharge = false;
    private Context mContext = null;


    public BatteryStatus(Context context, Intent batteryStatus)
    {
        mContext = context;
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


    public int getBatteryLevel()
    {
        return mBatteryLevel;
    }


    public int getBatteryIconId()
    {
        return mBatteryIconId;
    }


    public byte[] getBatteryIconBytes()
    {

        int batteryIconId = R.raw.battery_level0;
        if (mBatteryLevel >= 80)
        {
            batteryIconId = R.raw.battery_level4;
        } else if (mBatteryLevel >= 60)
        {
            batteryIconId = R.raw.battery_level3;
        } else if (mBatteryLevel >= 40)
        {
            batteryIconId = R.raw.battery_level2;
        } else if (mBatteryLevel >= 20)
        {
            batteryIconId = R.raw.battery_level1;
        }

        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
                batteryIconId);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100,
                (OutputStream) os);
        bmp = null;
        byte[] byteArray = os.toByteArray();

        return Base64.encode(byteArray, Base64.DEFAULT);
    }


    public boolean getIsCharging()
    {
        return mIsCharging;
    }


    public boolean getIsFull()
    {
        return mIsFull;
    }


    public int getChargePlug()
    {
        return mChargePlug;
    }


    public boolean getIsUsbCharge()
    {
        return mISUsbCharge;
    }


    public boolean getIsAcCharge()
    {
        return mIsAcCharge;
    }


    public void onClose()
    {
        mContext = null;
    }
}
