package at.tugraz.ist.akm.test.content.config;

import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.test.WebSMSToolActivityTestcase;

public class ConfigTest extends WebSMSToolActivityTestcase {
	private Config mConfig = null;
	
	public ConfigTest() {
		super(ConfigTest.class.getSimpleName());
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mConfig = new Config(mContext);
	}

	
	public void testGetSettings() {
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
	
	public void testUpdateSettings() {
		mConfig.setUserName("user");
		assertTrue("username not equals 'user'", mConfig.getUserName().equals("user"));
		mConfig.setUserName("");
		assertTrue("username not equals ''", mConfig.getUserName().equals(""));
		
		mConfig.setPassword("password");
		assertTrue("password not equals 'password'", mConfig.getPassWord().equals("password"));
		mConfig.setPassword("");
		assertTrue("password not equals ''", mConfig.getPassWord().equals(""));
		
		mConfig.setPort("1234");
		assertTrue("port not equals '1234'", mConfig.getPort().equals("1234"));
		mConfig.setPort("8887");
		assertTrue("port not equals '8887'", mConfig.getPort().equals("8887"));
		
		mConfig.setProtocol("https");
		assertTrue("protocol not equals 'https'", mConfig.getProtocol().equals("https"));
		mConfig.setProtocol("http");
		assertTrue("protocol not equals 'http'", mConfig.getProtocol().equals("http"));
		
		mConfig.setKeyStorePassword("key");
		assertTrue("keyStorePassword not equals 'key'", mConfig.getKeyStorePassword().equals("key"));
		mConfig.setKeyStorePassword("foobar64");
		assertTrue("keyStorePassword not equals 'foobar64'", mConfig.getKeyStorePassword().equals("foobar64"));
	}
}

