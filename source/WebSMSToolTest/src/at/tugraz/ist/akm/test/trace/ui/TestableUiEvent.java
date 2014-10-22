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

package at.tugraz.ist.akm.test.trace.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.trace.ui.UiEvent;

public class TestableUiEvent extends UiEvent
{
    private Date mTestDate = null;
    public int mTestDrawable = R.drawable.ic_action_hardware_computer;
    public String mTestDescription = null;
    public String mTestDetail = null;
    public String mTestTitle = null;


    public TestableUiEvent(Date date)
    {
        mTestDate = new Date(date.getTime());
    }


    @Override
    public String getDate()
    {
        DateFormat printFormat = SimpleDateFormat.getDateInstance();
        return printFormat.format(mTestDate);
    }


    @Override
    public String getDescription()
    {
        return mTestDescription;
    }


    @Override
    public String getDetail()
    {
        return mTestDetail;
    }


    @Override
    public int getDrawableIconId()
    {
        return mTestDrawable;
    }


    @Override
    public String getTime()
    {
        DateFormat printFormat = SimpleDateFormat.getTimeInstance();
        return printFormat.format(mTestDate);
    }


    @Override
    public String getTitle()
    {
        return mTestTitle;
    }

}
