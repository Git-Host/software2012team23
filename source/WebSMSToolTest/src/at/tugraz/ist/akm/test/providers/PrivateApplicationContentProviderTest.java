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
        mProvider.onCorruption(null);
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
