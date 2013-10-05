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

public class ContactFilter {

    public final static String SORT_ORDER_ASCENDING = "ASC";
    public final static String SORT_ORDER_DESCENDING = "DESC";
	
	private long mId = 0;
	private boolean mIsIdActive = false;

	private boolean mStarred = false;
	private boolean mIsStarredActive = false;

	private boolean mWithPhone = false;
	private boolean mIsWithPhoneActive = false;
	
	private boolean mSetOrderByDisplayName = false;
	private String mSortOrder = SORT_ORDER_ASCENDING;

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
	
	
	public void setOrderByDisplayName(boolean orderByDisplayName, String sortOrder){
		mSetOrderByDisplayName = orderByDisplayName;
		if(sortOrder.equals(SORT_ORDER_ASCENDING)){
			mSortOrder = SORT_ORDER_ASCENDING;
		} else {
			mSortOrder = SORT_ORDER_DESCENDING;
		}
	}
	
	public boolean getOrderByDisplayName(){
		return mSetOrderByDisplayName;
	}
	
	public String getSortOrder(){
		return mSortOrder;
	}
}
