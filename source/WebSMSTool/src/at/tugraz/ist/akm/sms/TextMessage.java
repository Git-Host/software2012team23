package at.tugraz.ist.akm.sms;

public class TextMessage {

	private String mId = null;
	private String mThreadId = null;
	private String mPerson = null;
	private String mDate = null;
	private String mAddress = null;
	private String mSeen = null;
	private String mRead = null;
	private String mBody = null;
	private String mProtocol = null;
	private String mStatus = null;
	private String mType = null;
	private String mServiceCenter = null;
	private String mLocked = null;

	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getThreadId() {
		return mThreadId;
	}

	public void setThreadId(String mThreadId) {
		this.mThreadId = mThreadId;
	}

	public String getPerson() {
		return mPerson;
	}

	public void setPerson(String mPerson) {
		this.mPerson = mPerson;
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String mDate) {
		this.mDate = mDate;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(String mAddress) {
		this.mAddress = mAddress;
	}

	public String getSeen() {
		return mSeen;
	}

	public void setSeen(String mSeen) {
		this.mSeen = mSeen;
	}

	public String getRead() {
		return mRead;
	}

	public void setRead(String mRead) {
		this.mRead = mRead;
	}

	public String getBody() {
		return mBody;
	}

	public void setBody(String mBody) {
		this.mBody = mBody;
	}

	public String getProtocol() {
		return mProtocol;
	}

	public void setProtocol(String mProtocol) {
		this.mProtocol = mProtocol;
	}

	public String getStatus() {
		return mStatus;
	}

	public void setStatus(String mStatus) {
		this.mStatus = mStatus;
	}

	public String getType() {
		return mType;
	}

	public void setType(String mType) {
		this.mType = mType;
	}

	public String getServiceCenter() {
		return mServiceCenter;
	}

	public void setServiceCenter(String mServiceCenter) {
		this.mServiceCenter = mServiceCenter;
	}

	public String getLocked() {
		return mLocked;
	}

	public void setLocked(String mLocked) {
		this.mLocked = mLocked;
	}

}
