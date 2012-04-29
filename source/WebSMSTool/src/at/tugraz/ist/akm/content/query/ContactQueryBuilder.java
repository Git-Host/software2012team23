package at.tugraz.ist.akm.content.query;

import java.util.ArrayList;
import java.util.List;

import android.provider.ContactsContract;

public class ContactQueryBuilder {

	private ContactFilter mFilter = null;
	private StringBuffer mWhere = new StringBuffer();
	private boolean mIsFirstWhereQuery = true;
	private List<String> mLikeArgs = new ArrayList<String>();

	public ContactQueryBuilder(ContactFilter filter) {
		mFilter = filter;
	}

	public ContentProviderQueryParameters getQueryArgs() {
		ContentProviderQueryParameters q = new ContentProviderQueryParameters();

		q.uri = ContactsContract.Contacts.CONTENT_URI;

		q.as = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.STARRED };

		if (mFilter == null)
			return q;

		collectIdClause();
		collectStarredQueryClause();
		collectWithPhoneQueryClause();

		q.like = new String[mLikeArgs.size()];
		q.like = mLikeArgs.toArray(q.like);
		q.where = mWhere.toString();
		return q;
	}

	/**
	 * collect starred query clause
	 */
	private void collectStarredQueryClause() {

		if (mFilter.getIsStarredActive()) {
			appendWhereQueryString(ContactsContract.Contacts.STARRED + " = ? ");
			if (mFilter.getIsStarred()) {
				mLikeArgs.add("1");
			} else {
				mLikeArgs.add("0");
			}
		}
	}

	/**
	 * collect with phone query clause
	 */
	private void collectWithPhoneQueryClause() {
		if (mFilter.getIsWithPhoneActive()) {
			appendWhereQueryString(ContactsContract.Contacts.HAS_PHONE_NUMBER
					+ " = ? ");

			if (mFilter.getWithPhone()) {
				mLikeArgs.add("1");
			} else {
				mLikeArgs.add("0");
			}
		}
	}

	/**
	 * collect ID query clause
	 */
	private void collectIdClause() {
		if (mFilter.getIsIdActive()) {
			appendWhereQueryString(ContactsContract.Contacts._ID + " = ? ");
			mLikeArgs.add(Long.toString(mFilter.getId()));
		}
	}

	/**
	 * append query clause in respect of adding " AND " in between all clases
	 * 
	 * @param clause
	 */
	private void appendWhereQueryString(final String clause) {
		if (!mIsFirstWhereQuery) {
			mWhere.append(" AND ");
		} else {
			mIsFirstWhereQuery = false;
		}
		mWhere.append(clause);
	}
}
