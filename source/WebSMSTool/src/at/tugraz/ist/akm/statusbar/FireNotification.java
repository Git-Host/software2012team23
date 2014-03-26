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

package at.tugraz.ist.akm.statusbar;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import at.tugraz.ist.akm.R;
import at.tugraz.ist.akm.activities.MainActivity;

public class FireNotification
{

    public static class NotificationInfo
    {
        public String title = null;
        public String text = null;
        public String tickerText = null;
    }

    private static int NOTIFICATION_ID = 8151;
    private Context mContext = null;
    private NotificationManager mNotificationManager = null;


    public FireNotification(Context context)
    {
        mContext = context;
        mNotificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }


    public void fireStickyInfos(NotificationInfo wInfos)
    {
        int icon = R.drawable.ic_notification;
        Notification notification = new Notification.Builder(mContext)
                .setContentText(wInfos.text).setSmallIcon(icon)
                .setTicker(wInfos.tickerText)
                .setContentTitle(wInfos.title).getNotification();
        notification.flags = Notification.FLAG_NO_CLEAR;

        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }


    public void cancelAll()
    {
        mNotificationManager.cancelAll();
    }
}
