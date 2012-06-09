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

package at.tugraz.ist.akm.sms;

import java.io.Serializable;

public class TextMessage implements Serializable {

	private static final long serialVersionUID = 6681600779409479945L;

	private String mId = "";
	private String mThreadId = "";
	private String mPerson = "";
	private String mDate = "";
	/**
	 * usually the phone number
	 */
	private String mAddress = "";
	private String mSeen = "1";
	private String mRead = "1";
	private String mBody = "";
	private String mProtocol = "null";
	private String mStatus = "-1";
	private String mType = "";
	private String mServiceCenter = "null";
	private String mLocked = "0";
	private String mReplyPathPresent = "";
	private String mSubject = "";
	private String mErrorCode = "";

	public String getReplyPathPresent() {
		return mReplyPathPresent;
	}

	public void setReplyPathPresent(String replyPathPresent) {
		this.mReplyPathPresent = replyPathPresent;
	}

	public String getSubject() {
		return mSubject;
	}

	public void setSubject(String subject) {
		this.mSubject = subject;
	}

	public String getErrorCode() {
		return mErrorCode;
	}

	public void setErrorCode(String errorCode) {
		this.mErrorCode = errorCode;
	}

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

	public void setDate(String date) {
		this.mDate = date;
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
