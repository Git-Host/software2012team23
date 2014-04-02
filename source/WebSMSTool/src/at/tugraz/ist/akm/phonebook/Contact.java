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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.net.Uri;

public class Contact
{

    public static class Number
    {
        private final String mNumber;
        private final int mType;


        public Number(String number, int type)
        {
            this.mNumber = Number.cleanNumber(number);
            /**
             * phone types are defined in
             * ContactsContract.CommonDataKinds.Phone.TYPE_***
             */
            this.mType = type;
        }


        public String getNumber()
        {
            return mNumber;
        }


        public int getType()
        {
            return mType;
        }


        public static String cleanNumber(String messyNumber)
        {
            return messyNumber.replaceAll("/", "")
                    .replaceAll(" ", "").replaceAll("-", "");
            
            //return messyNumber.replaceAll("^[+]", "00").replaceAll("/", "")
            //        .replaceAll(" ", "").replaceAll("-", "");
        }
    };

    private long mId = 0;
    private String mName = null;
    private String mFamilyName = null;
    private String mDisplayName = null;
    private Uri mPhotoUri = null;
    private byte[] mPhotoBytes = null;
    private List<Number> mPhoneNumbers = null;
    private boolean mStarred = false;


    public boolean isStarred()
    {
        return mStarred;
    }


    public void setStarred(boolean starred)
    {
        this.mStarred = starred;
    }


    public Contact()
    {
    }


    public long getId()
    {
        return mId;
    }


    public void setId(long id)
    {
        this.mId = id;
    }


    public String getName()
    {
        return mName;
    }


    public void setName(String name)
    {
        this.mName = name;
    }


    public String getFamilyName()
    {
        return mFamilyName;
    }


    public void setFamilyName(String familyName)
    {
        this.mFamilyName = familyName;
    }


    public String getDisplayName()
    {
        return mDisplayName;
    }


    public void setDisplayName(String displayName)
    {
        this.mDisplayName = displayName;
    }


    public Uri getPhotoUri()
    {
        return mPhotoUri;
    }


    public void setPhotoUri(Uri photoUri)
    {
        this.mPhotoUri = photoUri;
    }


    public byte[] getPhotoBytes()
    {
        if ( null == mPhotoBytes ) {
            return null;
        }
        return mPhotoBytes.clone();
    }


    public void setPhotoBytes(byte[] photo)
    {
        if (null != photo)
        {
            mPhotoBytes = new byte[photo.length];
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            bOut.reset();
            try
            {
                bOut.write(photo);
                mPhotoBytes = bOut.toByteArray();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    public List<Number> getPhoneNumbers()
    {
        return mPhoneNumbers;
    }


    public void setPhoneNumbers(List<Number> phoneNumbers)
    {
        this.mPhoneNumbers = phoneNumbers;
    }

}
