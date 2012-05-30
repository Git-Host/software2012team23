package at.tugraz.ist.akm.test.content.config;

import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase;

public class ConfigTest extends WebSMSToolActivityTestcase{
	private Config mConfig = null;
	
	public ConfigTest(String logTag) {
		super(ConfigTest.class.getSimpleName());
		mConfig = new Config(mContext);
	}
	
	public void testGetSettings(String name) {
		String username = mConfig.getUserName();
		String password = mConfig.getPassWord();
		String port = mConfig.getPort();
		String protocol = mConfig.getProtocol();
		String keyStorePassword = mConfig.getKeyStorePassword();
		assertTrue("username not equals standard username", username.equals(""));
		assertTrue("password not equals standard password", password.equals(""));
		assertTrue("port not equals standard port", port.equals("8887"));
		assertTrue("protocol not equals standard protocol", protocol.equals("http"));
		assertTrue("keystorepassword not equals standard keystorepassword", keyStorePassword.equals("foobar64"));
	}
}

