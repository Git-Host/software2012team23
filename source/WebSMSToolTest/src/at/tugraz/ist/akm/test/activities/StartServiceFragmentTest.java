package at.tugraz.ist.akm.test.activities;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.test.ActivityInstrumentationTestCase2;
import android.text.format.Formatter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.MainActivity;
import at.tugraz.ist.akm.test.trace.ExceptionThrowingLogSink;
import at.tugraz.ist.akm.trace.LogClient;
import at.tugraz.ist.akm.trace.TraceService;
import at.tugraz.ist.akm.webservice.service.WebSMSToolService.ServiceRunningStates;
import at.tugraz.ist.akm.webservice.service.interProcessMessges.ClientMessageBuilder;
import at.tugraz.ist.akm.webservice.service.interProcessMessges.ServiceConnectionMessageTypes;

public class StartServiceFragmentTest extends
        ActivityInstrumentationTestCase2<MainActivity>
{

    private LogClient mLog = new LogClient(
            StartServiceFragmentTest.class.getCanonicalName());
    private Messenger mClientMessenger = null;
    private MockServer mServer = new MockServer();
    private long mWaitForMessengerDealayMs = 200;

    private static class MockServer
    {
        public String getServerProtocol()
        {
            return "https";
        }


        public String getServerAddress()
        {
            return "192.168.1.1";
        }


        public String getServerPort()
        {
            return "8888";
        }


        public String getHttpUsername()
        {
            return "mrFoo";
        }


        public String getMaskedHttpPassword()
        {
            return "*****";
        }


        public boolean isHttpAccessRestrictionEnabled()
        {
            return true;
        }
    };


    public StartServiceFragmentTest()
    {
        super(MainActivity.class);
        TraceService.setSink(new ExceptionThrowingLogSink());
    }


    public void test_incoming_sms_counter()
    {
        int counter = 5;
        sendSMSMessageReceived(counter);
        waitMs(mWaitForMessengerDealayMs);

        TextView smsCounter = (TextView) getActivity().findViewById(
                R.id.main_fragment_sms_recieved);
        assertEquals(Integer.toString(counter), smsCounter.getText());
    }


    public void test_outgoing_sms_counter()
    {
        int counter = 500;
        sendSMSMessageSent(counter);
        waitMs(mWaitForMessengerDealayMs);

        TextView smsCounter = (TextView) getActivity().findViewById(
                R.id.main_fragment_sms_sent);
        assertEquals(Integer.toString(counter), smsCounter.getText());
    }


    public void test_send_username()
    {
        sendHttpUsername();
        waitMs(mWaitForMessengerDealayMs);

        TextView usernameView = (TextView) getActivity().findViewById(
                R.id.main_fragment_http_access_username);
        assertEquals(mServer.getHttpUsername(), usernameView.getText());
    }


    public void test_send_password()
    {
        sendtHttpPassword();
        waitMs(mWaitForMessengerDealayMs);

        TextView usernameView = (TextView) getActivity().findViewById(
                R.id.main_fragment_http_access_password);
        assertEquals(mServer.getMaskedHttpPassword(), usernameView.getText());
    }


    public void test_send_connection_url()
    {
        sendConnectionUrl();
        waitMs(mWaitForMessengerDealayMs);

        TextView view = (TextView) getActivity().findViewById(
                R.id.adress_data_field);
        assertEquals(formatConnectionUrl(), view.getText());
    }


    public void test_send_is_http_auth_enabled()
    {
        sendIsHttpAccessRestrictionEnabled();
        waitMs(mWaitForMessengerDealayMs);

        LinearLayout view = (LinearLayout) getActivity().findViewById(
                R.id.main_fragment_access_restriction_info);

        assertTrue(view.getAlpha() >= 1.0);
    }


    public void test_send_rx_bytes()
    {
        int bytesCount = 123456789;
        String humanReadableBytes = Formatter.formatFileSize(
                getInstrumentation().getContext(), bytesCount);

        sendReceivedBytesCount(bytesCount);
        waitMs(mWaitForMessengerDealayMs);

        TextView view = (TextView) getActivity().findViewById(
                R.id.main_fragment_total_bytes_recieved);

        assertEquals(humanReadableBytes, view.getText());
    }


    public void test_send_tx_bytes()
    {
        int bytesCount = 987654321;
        String humanReadableBytes = Formatter.formatFileSize(
                getInstrumentation().getContext(), bytesCount);

        sendSentBytesCount(bytesCount);
        waitMs(mWaitForMessengerDealayMs);

        TextView view = (TextView) getActivity().findViewById(
                R.id.main_fragment_total_bytes_sent);

        assertEquals(humanReadableBytes, view.getText());
    }


    public void test_send_server_starting()
    {
        sendCurrentRunningState(ServiceRunningStates.STARTING);
        waitMs(mWaitForMessengerDealayMs);

        ProgressBar view = (ProgressBar) getActivity().findViewById(
                R.id.start_stop_server_progress_bar);

        assertTrue(view.isIndeterminate());
    }


    public void test_send_server_stopped()
    {
        sendCurrentRunningState(ServiceRunningStates.STARTING);
        waitMs(mWaitForMessengerDealayMs);

        ProgressBar view = (ProgressBar) getActivity().findViewById(
                R.id.start_stop_server_progress_bar);

        assertTrue(view.isIndeterminate());

        sendCurrentRunningState(ServiceRunningStates.STOPPED);
        waitMs(mWaitForMessengerDealayMs);

        assertFalse(view.isIndeterminate());
    }


    private void waitMs(long msecs)
    {
        synchronized (this)
        {
            try
            {
                wait(msecs);
            }
            catch (InterruptedException e)
            {
                mLog.error("interrupted diring wait", e);
            }
        }
    }


    private boolean setMessenger(MainActivity mainActivity)
    {
        Messenger messenger = mainActivity.getStartServiceFragmentMessenger();
        if (messenger == null)
        {
            return false;
        }
        mClientMessenger = messenger;
        return true;
    }


    private void sendSMSMessageReceived(int smsDeliveredCounter)
    {
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.SMS_RECEIVED,
                smsDeliveredCounter);
    }


    private void sendSMSMessageSent(int smsDeliveredCounter)
    {
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.SMS_SENT,
                smsDeliveredCounter);
    }


    private void sendtHttpPassword()
    {
        sendMessageToClient(ServiceConnectionMessageTypes.Service.Response.HTTP_PASSWORD);
    }


    private void sendHttpUsername()
    {
        sendMessageToClient(ServiceConnectionMessageTypes.Service.Response.HTTP_USERNAME);
    }


    private void sendIsHttpAccessRestrictionEnabled()
    {
        int isRestrictionEnabled = 0;

        if (mServer.isHttpAccessRestrictionEnabled())
        {
            isRestrictionEnabled = 1;
        }

        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.HTTP_ACCESS_RESCRICTION_ENABLED,
                isRestrictionEnabled);
    }


    private void sendConnectionUrl()
    {
        sendMessageToClient(ServiceConnectionMessageTypes.Service.Response.CONNECTION_URL);
    }


    protected void sendSentBytesCount(int txBytes)
    {
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.NETWORK_TRAFFIC_TX_BYTES,
                txBytes);
    }


    protected void sendReceivedBytesCount(int rxBytes)
    {
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.NETWORK_TRAFFIC_RX_BYTES,
                rxBytes);
    }


    private void sendMessageToClient(int what)
    {
        sendMessageToClient(what, 0);
    }


    private void sendMessageToClient(int what, int arg1)
    {

        String messageName = ServiceConnectionMessageTypes.getMessageName(what);
        String messageValue = ServiceConnectionMessageTypes
                .getMessageName(arg1);
        if (messageValue == null)
        {
            messageValue = Integer.toString(arg1);
        }

        if (mClientMessenger != null)
        {
            try
            {
                Message message = Message.obtain(null, what, arg1, 0);
                appendDataToMessage(message, what);
                mClientMessenger.send(message);
            }
            catch (RemoteException e)
            {
                mLog.error("failed sending to client [" + messageName + "]", e);
            }
        } else
        {
            mLog.debug("failed sending [" + messageName + "=" + messageValue
                    + " to not registered client");
        }
    }


    private void appendDataToMessage(Message message, int messageId)
    {
        Bundle bundle = new Bundle();
        switch (messageId)
        {
        case ServiceConnectionMessageTypes.Service.Response.CONNECTION_URL:
            bundle.putString(
                    ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_CONNECTION_URL,
                    formatConnectionUrl());
            message.setData(bundle);
            break;

        case ServiceConnectionMessageTypes.Service.Response.HTTP_PASSWORD:
            bundle.putString(
                    ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_PASSWORD,
                    mServer.getMaskedHttpPassword());
            message.setData(bundle);
            break;
        case ServiceConnectionMessageTypes.Service.Response.HTTP_USERNAME:
            bundle.putString(
                    ServiceConnectionMessageTypes.Bundle.Key.STRING_ARG_SERVER_USERNAME,
                    mServer.getHttpUsername());
            message.setData(bundle);
            break;
        }
    }


    protected void sendCurrentRunningState(ServiceRunningStates runningState)
    {
        sendMessageToClient(
                ServiceConnectionMessageTypes.Service.Response.CURRENT_RUNNING_STATE,
                ClientMessageBuilder.translateRunningStateToInt(runningState));
    }


    private String formatConnectionUrl()
    {
        StringBuffer connectionUrl = new StringBuffer();
        connectionUrl.append(mServer.getServerProtocol()).append("://")
                .append(mServer.getServerAddress()).append(":")
                .append(mServer.getServerPort());
        return connectionUrl.toString();
    }


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        MainActivity mainActivity = getActivity();
        assertTrue(setMessenger(mainActivity));
    }
}
