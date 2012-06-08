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

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import at.tugraz.ist.akm.content.SmsContent;

public class TextMessageQueryBuilder {

	private TextMessageFilter mFilter = null;
	private boolean mIsFirstWhereQuery = true;

	private Uri mBoxUri = null;
	private StringBuffer mWhere = new StringBuffer();
	private List<String> mLikeArgs = new ArrayList<String>();

	public TextMessageQueryBuilder(TextMessageFilter filter) {
		mFilter = filter;
	}

	public ContentProviderQueryParameters getQueryArgs() {
		ContentProviderQueryParameters queryParameters = new ContentProviderQueryParameters();

		if (mFilter == null)
			return queryParameters;

		collectUri();
		collectAddressQueryPart();
		collectThreadIdQueryPart();
		collectIdQueryPart();
		collectPersonQueryPart();
		collectSeenQueryPart();
		collectReadQueryPart();

		queryParameters.uri = mBoxUri;
		if (mWhere != null)
			queryParameters.where = mWhere.toString();
		if (mLikeArgs.size() > 0) {
			queryParameters.like = new String [] {""};
			queryParameters.like = mLikeArgs.toArray(queryParameters.like);
		}
			
		return queryParameters;
	}

	/**
	 * collect the uri from filter
	 */
	private void collectUri() {
		if (mFilter.getIsBoxActive()) {
			mBoxUri = mFilter.getBox();
		}
	}

	/**
	 * add query to filter only matched phone number
	 */
	private void collectAddressQueryPart() {
		if (mFilter.getIsAddressActive()) {
			appendWhereQueryString(SmsContent.Content.ADDRESS + " = ? ");
			mLikeArgs.add(mFilter.getAddress());
		}
	}

	/**
	 * collect thread id query if set
	 */
	private void collectThreadIdQueryPart() {
		if (mFilter.getIsThreadIdActive()) {
			appendWhereQueryString(SmsContent.Content.THREAD_ID + " = ? ");
			mLikeArgs.add(mFilter.getThreadId());
		}
	}

	private void collectIdQueryPart() {
		if (mFilter.getIsIdActive()) {
			appendWhereQueryString(SmsContent.Content.ID + " = ? ");
			mLikeArgs.add(mFilter.getId());
		}
	}

	private void collectPersonQueryPart() {
		if (mFilter.getIsPersonActive()) {
			appendWhereQueryString(SmsContent.Content.PERSON + " = ? ");
			mLikeArgs.add(mFilter.getPerson());
		}
	}

	private void collectSeenQueryPart() {
		if (mFilter.getIsSeenActive()) {
			appendWhereQueryString(SmsContent.Content.SEEN + " = ? ");
			mLikeArgs.add(mFilter.getSeen());
		}
	}

	private void collectReadQueryPart() {
		if (mFilter.getIsReadActive()) {
			appendWhereQueryString(SmsContent.Content.READ + " = ? ");
			mLikeArgs.add(mFilter.getRead());
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
