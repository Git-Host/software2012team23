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

package at.tugraz.ist.akm.test.monitoring;

import android.test.IsolatedContext;
import at.tugraz.ist.akm.monitoring.BatteryStatus;
import at.tugraz.ist.akm.monitoring.SystemMonitor;
import at.tugraz.ist.akm.monitoring.TelephonySignalStrength;
import at.tugraz.ist.akm.test.base.WebSMSToolActivityTestcase;

public class SystemMonitorTest extends WebSMSToolActivityTestcase
{

    private SystemMonitor mSystemMonitor = null;
    public SystemMonitorTest()
    {
        super(SystemMonitorTest.class.getSimpleName());
    }

    @Override
    protected void setUp() throws Exception 
    {
        super.setUp();
        mSystemMonitor = new SystemMonitor(mContext);
        mSystemMonitor.start();
    };
    
    
    @Override 
    public void tearDown()
    {
        mSystemMonitor.stop();
    }
    

    public void testSystemMonitor_batteryState()
    {
        try
        {
            BatteryStatus bStats = mSystemMonitor.getBatteryStatus();
            logBStats(bStats);
            assertTrue(bStats.getBatteryIconId() > 0);
            byte[] bytes = bStats.getBatteryIconBytes();
            assertTrue(bytes.length > 0);
        } catch (Exception ex)
        {
            assertTrue(false);
        }
    }


    /* skipped test due to necessary cell signal
    public void testSystemMonitor_signalStrength()
    {
        try
        {
            TelephonySignalStrength sStats = null;
            
            while (null == sStats ) {
                sStats = mSystemMonitor.getSignalStrength();
            }
            assertTrue(null != sStats);
            logSStats(sStats);
            assertTrue(sStats.getSignalStrength() > 0);
            assertTrue(sStats.getSignalIconId() > 0);
            byte[] bytes = sStats.getSignalStrengthIconBytes();
            assertTrue(bytes.length > 0);
        } catch (Exception e)
        {
            assertTrue(false);
        }
    }
    */
    
    private void logBStats(BatteryStatus bStats)
    {
        logVerbose(" BatteryStatus: batteryIconId ["
                + bStats.getBatteryIconId() + "] isCharging ["
                + bStats.getIsCharging() + "] isFull[" + bStats.getIsFull()
                + "] chargePlug[" + bStats.getChargePlug() + "]usbCharge ["
                + bStats.getIsUsbCharge() + "] acCharge["
                + bStats.getIsAcCharge() + "]");
    }


    @SuppressWarnings("unused")
    private void logSStats(TelephonySignalStrength sStats)
    {
        logVerbose(" TelephonySignalStrength: signal icon id ["
                + sStats.getSignalIconId() + "] signal strength ["
                + sStats.getSignalStrength() + "%]");
    }

}
