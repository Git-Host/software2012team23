package at.tugraz.ist.akm.test.content;

import junit.framework.TestCase;
import at.tugraz.ist.akm.content.SmsContentConstants;

public class SmsContentConstantsMockTest extends TestCase
{

    public void testMock_smsConstants_construct()
    {
        @SuppressWarnings("unused")
        SmsContentConstants smc = new SmsContentConstants();
        @SuppressWarnings("unused")
        SmsContentConstants.Column smcc = new SmsContentConstants.Column();
        @SuppressWarnings("unused")
        SmsContentConstants.Uri smcu = new SmsContentConstants.Uri();
    }

}
