package at.tugraz.ist.akm.content.query;

public class ContactFilter {

	private long mId = 0;
	private boolean mIsIdActive = false;

	private boolean mStarred = false;
	private boolean mIsStarredActive = false;

	private boolean mWithPhone = false;
	private boolean mIsWithPhoneActive = false;

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
		mIsIdActive = true;
	}

	public boolean getIsIdActive() {
		return mIsIdActive;
	}

	public void setIsIdActive(boolean isActive) {
		mIsIdActive = isActive;
	}

	public boolean getIsStarred() {
		return mStarred;
	}

	public void setIsStarred(boolean mStarred) {
		this.mStarred = mStarred;
		this.mIsStarredActive = true;
	}

	public boolean getIsStarredActive() {
		return mIsStarredActive;
	}

	public void setIsStarredActive(boolean isStarred) {
		mIsStarredActive = isStarred;
	}

	public void setWithPhone(boolean mWithPhone) {
		this.mWithPhone = mWithPhone;
		this.mIsWithPhoneActive = true;
	}

	public boolean getWithPhone() {
		return this.mWithPhone;
	}

	public boolean getIsWithPhoneActive() {
		return mIsWithPhoneActive;
	}
	
	public void setIsWithPhoneActive(boolean isWithPhoneActive) {
		mIsWithPhoneActive = isWithPhoneActive;
	}
}
