package at.tugraz.ist.akm.phonebook;

import java.util.List;

import android.net.Uri;

public class Contact {

	public static class Number {
		private final String mNumber;
		private final int mType;

		public Number(String number, int type) {
			this.mNumber = number;
			this.mType = type;
		}

		public String getNumber() {
			return mNumber;
		}

		public int getType() {
			return mType;
		}
	};

	private long mId = 0;
	private String mName = null;
	private String mFamilyName = null;
	private String mDisplayName = null;
	private Uri mPhotoUri = null;
	private List<Number> mPhoneNumbers = null;
	private boolean mStarred = false;

	public boolean isStarred() {
		return mStarred;
	}

	public void setStarred(boolean starred) {
		this.mStarred = starred;
	}

	public Contact() {
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getFamilyName() {
		return mFamilyName;
	}

	public void setFamilyName(String familyName) {
		this.mFamilyName = familyName;
	}

	public String getDisplayName() {
		return mDisplayName;
	}

	public void setDisplayName(String displayName) {
		this.mDisplayName = displayName;
	}

	public Uri getPhotoUri() {
		return mPhotoUri;
	}

	public void setPhotoUri(Uri photoUri) {
		this.mPhotoUri = photoUri;
	}

	public List<Number> getPhoneNumbers() {
		return mPhoneNumbers;
	}

	public void setPhoneNumbers(List<Number> phoneNumbers) {
		this.mPhoneNumbers = phoneNumbers;
	}

}
