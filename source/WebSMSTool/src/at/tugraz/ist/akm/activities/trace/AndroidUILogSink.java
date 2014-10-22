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

package at.tugraz.ist.akm.activities.trace;

import java.io.IOException;

import android.app.Activity;
import android.widget.TextView;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.trace.ILogSink;

public class AndroidUILogSink implements ILogSink
{

    private Activity mActivity = null;
    private TextView mLogView = null;


    public AndroidUILogSink(Activity mainActivity)
    {
        mActivity = mainActivity;
        mLogView = (TextView) mActivity.findViewById(R.id.info_log_field);
    }


    @Override
    public void error(final String tag, final String message)
    {
        android.util.Log.e(tag, message);
    }


    @Override
    public void warning(final String tag, final String message)
    {
        android.util.Log.w(tag, message);
    }


    @Override
    public void info(final String tag, final String message)
    {
        android.util.Log.i(tag, message);
    }


    @Override
    public void debug(final String tag, final String message)
    {
        android.util.Log.d(tag, message);
    }


    @Override
    public void verbose(final String tag, final String message)
    {
        if (mLogView != null)
        {
            if (message != null)
            {
                mLogView.append("\n" + message);
            }
        }
        android.util.Log.v(tag, message);
    }


    @Override
    public void close() throws IOException
    {
        mActivity = null;
        mLogView = null;
    }
}
