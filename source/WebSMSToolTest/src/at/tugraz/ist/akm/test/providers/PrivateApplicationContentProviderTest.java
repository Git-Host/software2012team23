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

package at.tugraz.ist.akm.test.providers;

import at.tugraz.ist.akm.providers.PrivateApplicationContentProvider;
import at.tugraz.ist.akm.test.base.WebSMSToolInstrumentationTestcase;

public class PrivateApplicationContentProviderTest extends
        WebSMSToolInstrumentationTestcase
{

    PrivateApplicationContentProvider mProvider = null;


    public PrivateApplicationContentProviderTest()
    {
        super(PrivateApplicationContentProviderTest.class.getName());
    }


    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        PrivateApplicationContentProvider.construct(mContext);
        mProvider = PrivateApplicationContentProvider.instance();
        mProvider.openDatabase();
    }


    @Override
    protected void tearDown() throws Exception
    {
        mProvider.closeDatabase();
        super.tearDown();
    }


    public void test_updateKeystorePassword()
    {
        assertEquals(1, mProvider.storeKeystorePassword("secret"));
        assertEquals(0, mProvider.storeKeystorePassword("secret"));
        assertEquals(1, mProvider.storeKeystorePassword("secret1"));
    }


    public void test_readKeystorePassword()
    {
        String password = "sepp-huaba-franz";
        mProvider.storeKeystorePassword("");
        assertEquals(1, mProvider.storeKeystorePassword(password));
        assertEquals(password, mProvider.restoreKeystorePassword(), password);
    }

}
