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

package at.tugraz.ist.akm.test.content.config;

import at.tugraz.ist.akm.content.Config;
import at.tugraz.ist.akm.test.base.WebSMSToolActivityTestcase;

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
		//insert values we want to check 
		mConfig.setUserName("testUserName");
		mConfig.setPassword("testPassword");
		mConfig.setPort("8800");
		mConfig.setProtocol("http");
		
		String username = mConfig.getUserName();
		String password = mConfig.getPassWord();
		String port = mConfig.getPort();
		String protocol = mConfig.getProtocol();
		String keyStorePassword = mConfig.getKeyStorePassword();
		assertTrue("username not equals standard username", username.equals("testUserName"));
		assertTrue("password not equals standard password", password.equals("testPassword"));
		assertTrue("port not equals standard port", port.equals("8800"));
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

