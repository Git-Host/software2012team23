package at.tugraz.ist.akm.test.providers;

import android.net.Uri;
import at.tugraz.ist.akm.providers.AuthContentProvider;
import at.tugraz.ist.akm.test.WebSMSToolTestInstrumentation;

public class AuthContentProviderTest extends WebSMSToolTestInstrumentation {

	private AuthContentProvider mAuthContentProvider = null;
	public AuthContentProviderTest() {
		super(AuthContentProviderTest.class.getSimpleName());
		mAuthContentProvider = new AuthContentProvider();
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testDelete() {
		assertTrue("no users deleted", mAuthContentProvider.delete(Uri.parse("content://websmstool/settings/users"), null, null) != 0);
	}
	
	public void testInsert() {
		assertTrue("values not inserted", !(mAuthContentProvider.insert(null, null) == null));
	}
	
	public void testQuery() {
		assertTrue("no values found", !(mAuthContentProvider.query(null, null, null, null, null) == null));
	}

	public void testUpdate() {
		assertTrue("no values updated", mAuthContentProvider.update(null, null, null, null) != 0);
	}
}
