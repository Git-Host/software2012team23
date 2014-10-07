package at.tugraz.ist.akm.networkInterface;

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

public class WifiIpAddress
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
        String ip4Address = "0.0.0.0";

        NetworkInfo mobileNetInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (mWifiManager.isWifiEnabled())
        {
            ip4Address = readWifiIP4Address();
        } else if (AppEnvironment.isRunningOnEmulator()
                && mobileNetInfo != null && mobileNetInfo.isConnected())
        {
            try
            {
                ip4Address = readIP4AddressOfEmulator();
            }
            catch (SocketException e)
            {
                mLog.error("not able to read local ip-address", e);
            }
        }
        return ip4Address;
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
                // do not replace formatter by InetAddress here since this
                // returns "1.0.0.127" instead of "127.0.0.1"
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

}
