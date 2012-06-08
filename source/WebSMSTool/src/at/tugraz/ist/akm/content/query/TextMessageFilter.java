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

package at.tugraz.ist.akm.content.query;

import android.net.Uri;

public class TextMessageFilter {

	/**
	 * @{ depending on content://sms/*box columns
	 */
	private String mId = "";
	private boolean mIsIdActive = false;

	private String mThreadId = "";
	private boolean mIsThreadIdActive = false;

	private String mPerson = "null";
	private boolean mIsPersonActive = false;

	private String mAddress = "";
	private boolean mIsAddressActive = false;

	private String mSeen = "1";
	private boolean mIsSeenActive = false;

	private String mRead = "1";
	private boolean mIsReadActive = false;

	/**
	 * @}
	 */

	/**
	 * @{ depending on @see SmsContent.ContentUri
	 */
	private Uri mBox = null;
	private boolean isBoxActive = false;

	/**
	 * @}
	 */

	public String getId() {
		return mId;
	}

	public void setId(long textMessageId) {
		this.mId = Long.toString(textMessageId);
		mIsIdActive = true;
	}

	public boolean getIsIdActive() {
		return mIsIdActive;
	}

	public void setIsIdActive(boolean mIsIdActive) {
		this.mIsIdActive = mIsIdActive;
	}

	public String getThreadId() {
		return mThreadId;
	}

	public void setThreadId(Long threadId) {
		this.mThreadId = Long.toString(threadId);
		mIsThreadIdActive = true;
	}

	public boolean getIsThreadIdActive() {
		return mIsThreadIdActive;
	}

	public void setIsThreadIdActive(boolean mIsThreadIdActive) {
		this.mIsThreadIdActive = mIsThreadIdActive;
	}

	public String getPerson() {
		return mPerson;
	}

	public void setPerson(String mPerson) {
		this.mPerson = mPerson;
		mIsPersonActive = true;
	}

	public boolean getIsPersonActive() {
		return mIsPersonActive;
	}

	public void setIsPersonActive(boolean mIsPersonActive) {
		this.mIsPersonActive = mIsPersonActive;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(String mAddress) {
		this.mAddress = mAddress;
		mIsAddressActive = true;
	}

	public boolean getIsAddressActive() {
		return mIsAddressActive;
	}

	public void setIsAddressActive(boolean mIsAddressActive) {
		this.mIsAddressActive = mIsAddressActive;
	}

	public String getSeen() {
		return mSeen;
	}

	public void setSeen(boolean isSeen) {
		if (isSeen) {
			mSeen = "1";
		} else {
			mSeen = "0";
		}
		mIsSeenActive = true;
	}

	public boolean getIsSeenActive() {
		return mIsSeenActive;
	}

	public void setIsSeenActive(boolean mIsSeenActive) {
		this.mIsSeenActive = mIsSeenActive;
	}

	public String getRead() {
		return mRead;
	}

	public void setRead(boolean isRead) {
		if (isRead) {
			mRead = "1";
		} else {
			mRead = "0";
		}
		mIsReadActive = true;
	}

	public boolean getIsReadActive() {
		return mIsReadActive;
	}

	public void setReadActive(boolean mReadActive) {
		this.mIsReadActive = mReadActive;
	}

	public void setBox(Uri smsBox) {
		mBox = smsBox;
		isBoxActive = true;
	}

	public Uri getBox() {
		return mBox;
	}

	public void setIsBoxActive(boolean isSmsBoxActive) {
		isBoxActive = isSmsBoxActive;
	}

	public boolean getIsBoxActive() {
		return isBoxActive;
	}
}
