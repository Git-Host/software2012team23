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

import android.provider.ContactsContract;

public class ContactQueryBuilder
{

    private ContactFilter mFilter = null;
    private StringBuffer mWhere = new StringBuffer();
    private boolean mIsFirstWhereQuery = true;
    private List<String> mLikeArgs = new ArrayList<String>();
    private String mSort = "";


    public ContactQueryBuilder(ContactFilter filter)
    {
        mFilter = filter;
    }


    public ContentProviderQueryParameters getQueryArgs()
    {
        ContentProviderQueryParameters queryParameters = new ContentProviderQueryParameters();

        queryParameters.uri = ContactsContract.Contacts.CONTENT_URI;

        queryParameters.as = new String[] { ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.STARRED };

        if (mFilter == null)
            return queryParameters;

        collectIdClause();
        collectStarredQueryClause();
        collectWithPhoneQueryClause();
        collectSortClause();

        queryParameters.like = new String[mLikeArgs.size()];
        queryParameters.like = mLikeArgs.toArray(queryParameters.like);
        queryParameters.where = mWhere.toString();
        queryParameters.sortBy = mSort;
        return queryParameters;
    }


    /**
     * collect starred query clause
     */
    private void collectStarredQueryClause()
    {

        if (mFilter.getIsStarredActive())
        {
            appendWhereQueryString(ContactsContract.Contacts.STARRED + " = ? ");
            if (mFilter.isStarred())
            {
                mLikeArgs.add("1");
            } else
            {
                mLikeArgs.add("0");
            }
        }
    }


    /**
     * collect with phone query clause
     */
    private void collectWithPhoneQueryClause()
    {
        if (mFilter.getIsWithPhoneActive())
        {
            appendWhereQueryString(ContactsContract.Contacts.HAS_PHONE_NUMBER
                    + " = ? ");

            if (mFilter.getWithPhone())
            {
                mLikeArgs.add("1");
            } else
            {
                mLikeArgs.add("0");
            }
        }
    }


    /**
     * collect ID query clause
     */
    private void collectIdClause()
    {
        if (mFilter.getIsIdActive())
        {
            appendWhereQueryString(ContactsContract.Contacts._ID + " = ? ");
            mLikeArgs.add(Long.toString(mFilter.getId()));
        }
    }


    /**
     * append query clause in respect of adding " AND " in between all clases
     * 
     * @param clause
     */
    private void appendWhereQueryString(final String clause)
    {
        if (!mIsFirstWhereQuery)
        {
            mWhere.append(" AND ");
        } else
        {
            mIsFirstWhereQuery = false;
        }
        mWhere.append(clause);
    }


    /**
     * collect the sort clause from filter
     */
    private void collectSortClause()
    {
        if (mFilter.getOrderByDisplayName())
        {
            mSort = ContactsContract.Contacts.DISPLAY_NAME + " "
                    + mFilter.getSortOrder();
        }
    }
}
