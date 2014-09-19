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

package at.tugraz.ist.akm.phonebook;

import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.phonebook.PhonebookCache.CachedAsyncPhonebookReader;
import at.tugraz.ist.akm.phonebook.contact.Contact;
import at.tugraz.ist.akm.phonebook.contact.ContactReader;
import at.tugraz.ist.akm.phonebook.contact.IContactModifiedCallback;
import at.tugraz.ist.akm.phonebook.contact.IContactReader;
import at.tugraz.ist.akm.trace.LogClient;

public class PhonebookBridge implements IContactModifiedCallback,
        IContactReader
{

    private Context mContext = null;
    private ContentResolver mContentResolver = null;
    private LogClient mLog = new LogClient(this);

    private ContactReader mContactReader = null;
    private CachedAsyncPhonebookReader mCachedContactReader = null;

    private Cursor mContactContentCursor = null;
    private ContactContentObserver mContactContentObserver = null;
    private IContactModifiedCallback mExternalContactModifiedCallback = null;

    private CacheModifiedHandler mCacheModifiedHandler = null;

    static private class ContactContentObserver extends ContentObserver
    {

        private IContactModifiedCallback mCallback = null;


        public ContactContentObserver(IContactModifiedCallback callback)
        {
            super(null);
            mCallback = callback;
        }


        @Override
        public boolean deliverSelfNotifications()
        {
            return true;
        }


        @Override
        public void onChange(boolean selfChange)
        {
            super.onChange(selfChange);
            if (!selfChange)
            {
                mCallback.contactModifiedCallback();
            }
        }
    }


    public PhonebookBridge(Context context)
    {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mContactReader = new ContactReader(mContentResolver);

        ContactFilter allFilter = new ContactFilter();
        allFilter.setWithPhone(true);
        allFilter.setOrderByDisplayName(true,
                ContactFilter.SORT_ORDER_ASCENDING);

        mCacheModifiedHandler = new CacheModifiedHandler(this);
        mCachedContactReader = new CachedAsyncPhonebookReader(allFilter,
                mContext, mContactReader);
        mCachedContactReader
                .registerCacheModifiedHandler(mCacheModifiedHandler);
        mContactContentCursor = getContactCursor();

        mCachedContactReader.start();
    }


    public void start()
    {
        registerContactChangedObserver();
    }


    public void stop()
    {
        mLog.debug("closing cache ...");
        mCacheModifiedHandler.onClose();
        mCachedContactReader.finish();
        mCachedContactReader.unregisterCacheModifiedHandler();
        mExternalContactModifiedCallback = null;
        unregisterContactChangedObserver();
    }


    public List<Contact> fetchContacts(ContactFilter filter)
    {
        return mCachedContactReader.fetchContacts();
    }


    synchronized public void setContactModifiedCallback(
            IContactModifiedCallback callback)
    {
        mLog.debug("registered new [ContactModifiedCallback] callback");
        mExternalContactModifiedCallback = callback;
    }


    protected synchronized void cacheModifiedCallback()
    {
        if (mExternalContactModifiedCallback != null)
        {
            mExternalContactModifiedCallback.contactModifiedCallback();
        }
    }


    @Override
    synchronized public void contactModifiedCallback()
    {
        mLog.debug("received contact modified");
        mCachedContactReader.contactModifiedCallback();
    }


    private Cursor getContactCursor()
    {
        Uri select = ContactsContract.Contacts.CONTENT_URI;
        String[] as = { ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts._ID };
        String where = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = ? ";
        String[] like = { "1" };

        return mContentResolver.query(select, as, where, like, null);
    }


    private void registerContactChangedObserver()
    {
        mContactContentObserver = new ContactContentObserver(this);
        mContactContentCursor.registerContentObserver(mContactContentObserver);
    }


    private void unregisterContactChangedObserver()
    {
        mContactContentCursor
                .unregisterContentObserver(mContactContentObserver);
        mContactContentObserver = null;
        mContactContentCursor.close();
    }
}
