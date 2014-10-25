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

package at.tugraz.ist.akm.phonebook.contact;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
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
            this.mNumber = cleanNumber(number);
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
            return messyNumber.replaceAll("/", "").replaceAll(" ", "")
                    .replaceAll("-", "");

            // return messyNumber.replaceAll("^[+]", "00").replaceAll("/", "")
            // .replaceAll(" ", "").replaceAll("-", "");
        }


        @Override
        public boolean equals(Object that)
        {
            if (that == null)
            {
                return false;
            }
            if (that instanceof Number)
            {
                Number otherNumber = (Number) that;
                if (this.mNumber.equals(otherNumber.mNumber)
                        && this.mType == otherNumber.mType)
                {
                    return true;
                }
            }
            return false;
        }


        @Override
        public int hashCode()
        {
            return new StringBuilder().append(mNumber).append(mType).hashCode();
        }


        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Nr: " + mNumber + " Type: " + mType);
            return sb.toString();
        }
    };

    private long mId = 0;
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
        if (null == mPhotoBytes)
        {
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
            }
            catch (IOException e)
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


    @Override
    public boolean equals(Object that)
    {
        if (that == null)
        {
            return false;
        }

        if (this == that)
        {
            return true;
        }

        if (false == (that instanceof Contact))
        {
            return false;
        }

        return areSignificantFieldsEqual(this, (Contact) that);

    }


    private final boolean areSignificantFieldsEqual(Contact a, Contact b)
    {
        boolean isEqual = false;

        if (a.mDisplayName.equals(b.mDisplayName) && a.mStarred == b.mStarred
                // && a.mFamilyName.equals(b.mFamilyName) && a.mId == b.mId
                && photoUriEquals(a, b) && photoBytesEquals(a, b)
                && phoneNumbersEquals(a, b))
        {
            isEqual = true;
        }

        return isEqual;
    }


    private boolean phoneNumbersEquals(Contact a, Contact b)
    {
        boolean isEqual = false;
        if (a.mPhoneNumbers == b.mPhoneNumbers)
        {
            isEqual = true;
        } else if (a.mPhoneNumbers != null && b.mPhoneNumbers != null)
        {
            boolean isAinBContained = true;
            for (Number aNumber : a.mPhoneNumbers)
            {
                if (!b.mPhoneNumbers.contains(aNumber))
                {
                    isAinBContained = false;
                    break;
                }
            }
            isEqual = isAinBContained;
        }
        return isEqual;

    }


    private boolean photoUriEquals(Contact a, Contact b)
    {
        boolean isEqual = false;
        if (a.mPhotoUri == b.mPhotoUri)
        {
            isEqual = true;
        } else if (a.mPhotoUri != null && b.mPhotoUri != null
                && a.mPhotoUri.equals(b.mPhotoUri))
        {
            isEqual = true;
        }
        return isEqual;
    }


    private boolean photoBytesEquals(Contact a, Contact b)
    {
        boolean isEqual = false;
        if (a.mPhotoBytes == b.mPhotoBytes)
        {
            isEqual = true;
        } else if (a.mPhotoBytes != null && b.mPhotoBytes != null
                && Arrays.equals(a.mPhotoBytes, b.mPhotoBytes))
        {
            isEqual = true;
        }
        return isEqual;
    }


    @Override
    public int hashCode()
    {
        StringBuilder hashCodeBuilder = new StringBuilder().append(17)
                .append(mDisplayName).append(mStarred).append(mId)
                .append(mPhotoUri);

        if (null != mPhotoBytes)
        {
            hashCodeBuilder.append(java.util.Arrays.hashCode(mPhotoBytes));
        }

        if (null != mPhoneNumbers)
        {
            for (Number number : mPhoneNumbers)
            {
                hashCodeBuilder.append(number.getNumber().hashCode());
            }
        }
        return hashCodeBuilder.toString().hashCode();
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("DisplayName: " + mDisplayName + " IsStarred: " + mStarred
                + " PhoneNumbers:");

        if (mPhoneNumbers != null)
            for (Number number : mPhoneNumbers)
            {
                sb.append(" " + number.toString());
            }

        return sb.toString();
    }
}
