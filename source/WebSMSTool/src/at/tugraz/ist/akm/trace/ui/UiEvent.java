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

package at.tugraz.ist.akm.trace.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.tugraz.ist.akm.R;

public class UiEvent
{
    private Date mDate = null;
    private int mDrawableResourceId = 0;
    private String mTitle = null;
    private String mDescription = null;


    UiEvent()
    {
        mDate = new Date();
        mDrawableResourceId = R.drawable.ic_action_action_about;
    }


    protected void setTitle(String title)
    {
        mTitle = new String(title);
    }


    protected void setDescription(String shortDescription)
    {
        mDescription = new String(shortDescription);
    }


    public String getTitle()
    {
        return mTitle;
    }


    public String getDescription()
    {
        return mDescription;
    }


    public String getTime()
    {

        DateFormat printFormat = SimpleDateFormat.getTimeInstance();
        return printFormat.format(mDate);
    }


    public String getDate()
    {
        DateFormat printFormat = SimpleDateFormat.getDateInstance();
        return printFormat.format(mDate);
    }


    public int getDrawableIconId()
    {
        return mDrawableResourceId;
    }


    protected void setDrawableIconId(int drawableId)
    {
        mDrawableResourceId = drawableId;
    }


    public String getDetail()
    {
        return null;
    }


    public UiEvent load(ResourceStringLoader loader)
    {
        return this;
    }
}
