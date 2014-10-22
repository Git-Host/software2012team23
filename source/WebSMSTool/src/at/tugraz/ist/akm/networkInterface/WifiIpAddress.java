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

package at.tugraz.ist.akm.networkInterface;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Locale;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import at.tugraz.ist.akm.environment.AppEnvironment;
import at.tugraz.ist.akm.trace.LogClient;

public class WifiIpAddress implements Closeable
{
    private LogClient mLog = new LogClient(
            WifiIpAddress.class.getCanonicalName());
    private ConnectivityManager mConnectivityManager = null;
    private WifiManager mWifiManager = null;


    public WifiIpAddress(Context appContext)
    {
        mConnectivityManager = (ConnectivityManager) appContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) appContext
                .getSystemService(Context.WIFI_SERVICE);

    }


    public String readLocalIpAddress()
    {

        NetworkInfo mobileNetInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (mWifiManager.isWifiEnabled())
        {
            return readWifiIP4Address();
        }
        if (AppEnvironment.isRunningOnEmulator() && mobileNetInfo != null
                && mobileNetInfo.isConnected())
        {
            try
            {
                return readIP4AddressOfEmulator();
            }
            catch (SocketException e)
            {
                mLog.error("not able to read local ip-address", e);
            }
        } else if (isWifiAPEnabled())
        {
            return readIp4ApAddress();
        }

        mLog.error("failed to determine correct ip address");
        return null;
    }


    public String readIp4ApAddress()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan"))
                {
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements();)
                    {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && (inetAddress.getAddress().length == 4))
                        {
                            mLog.debug("found AP address ["
                                    + inetAddress.getHostAddress() + "]");
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            mLog.debug("failed to read ip address in access point mode");
        }
        return null;
    }


    @SuppressWarnings("deprecation")
    private String readWifiIP4Address()
    {
        String ip4Address = "0.0.0.0";
        byte[] ipAddress = BigInteger.valueOf(
                mWifiManager.getConnectionInfo().getIpAddress()).toByteArray();
        try
        {
            InetAddress address = InetAddress.getByAddress(ipAddress);
            String concreteAddressString = address.getHostAddress()
                    .toUpperCase(Locale.getDefault());
            if (InetAddressUtils.isIPv4Address(concreteAddressString))
            {
                ip4Address = Formatter.formatIpAddress(mWifiManager
                        .getConnectionInfo().getIpAddress());
            }
        }
        catch (UnknownHostException e)
        {
            return ip4Address;
        }
        return ip4Address;
    }


    private String readIP4AddressOfEmulator() throws SocketException
    {
        String inet4Address = "0.0.0.0";

        for (Enumeration<NetworkInterface> iter = NetworkInterface
                .getNetworkInterfaces(); iter.hasMoreElements();)
        {
            NetworkInterface nic = iter.nextElement();

            if (nic.getName().startsWith("eth"))
            {
                Enumeration<InetAddress> addresses = nic.getInetAddresses();
                addresses.nextElement(); // skip first
                if (addresses.hasMoreElements())
                {
                    InetAddress address = addresses.nextElement();

                    String concreteAddressString = address.getHostAddress()
                            .toUpperCase(Locale.getDefault());
                    if (InetAddressUtils.isIPv4Address(concreteAddressString))
                    {
                        inet4Address = concreteAddressString;
                    }
                }
            }
        }
        return inet4Address;
    }


    public boolean isWifiEnabled()
    {
        return mWifiManager.isWifiEnabled();
    }


    public boolean isWifiAPEnabled()
    {
        String hiddenMethodToInvoke = "isWifiApEnabled";

        Method[] methods = mWifiManager.getClass().getDeclaredMethods();
        for (Method method : methods)
        {
            if (method.getName().equals(hiddenMethodToInvoke))
            {

                try
                {
                    return (Boolean) method.invoke(mWifiManager);
                }
                catch (IllegalArgumentException e)
                {
                    mLog.debug("illegal arguments for [" + hiddenMethodToInvoke
                            + "]");
                }
                catch (IllegalAccessException e)
                {
                    mLog.debug("failed to access [" + hiddenMethodToInvoke
                            + "]");
                }
                catch (InvocationTargetException e)
                {
                    mLog.debug("failed to invoke [" + hiddenMethodToInvoke
                            + "]");
                }
            }
        }
        return false;
    }


    @Override
    public void close() throws IOException
    {
        mWifiManager = null;
        mLog = null;
        mConnectivityManager = null;
    }

}
