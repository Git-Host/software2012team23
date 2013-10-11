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

package at.tugraz.ist.akm.content;

public class SmsContentConstants
{
    public static class Uri
    {
        public final static android.net.Uri BASE_URI = android.net.Uri
                .parse("content://sms");
        public final static android.net.Uri INBOX_URI = android.net.Uri
                .withAppendedPath(BASE_URI, "inbox");
        public final static android.net.Uri OUTBOX_URI = android.net.Uri
                .withAppendedPath(BASE_URI, "outbox");
        public final static android.net.Uri SENT_URI = android.net.Uri
                .withAppendedPath(BASE_URI, "sent");
        public final static android.net.Uri DRAFT_URI = android.net.Uri
                .withAppendedPath(BASE_URI, "draft");
        public final static android.net.Uri UNDELIVERED_URI = android.net.Uri
                .withAppendedPath(BASE_URI, "undelivered");
        public final static android.net.Uri FAILED_URI = android.net.Uri
                .withAppendedPath(BASE_URI, "failed");
        public final static android.net.Uri QUEUED_URI = android.net.Uri
                .withAppendedPath(BASE_URI, "queued");
    }

    public static class Column
    {
        /**
         * column name
         */
        public final static String ID = "_id";
        /**
         * column name
         */
        public final static String THREAD_ID = "thread_id";
        /**
         * column name
         */
        public final static String ADDRESS = "address";
        /**
         * column name
         */
        public final static String PERSON = "person";
        /**
         * column name
         */
        public final static String DATE = "date";
        /**
         * column name
         */
        public final static String PROTOCOL = "protocol";
        /**
         * column name
         */
        public final static String READ = "read";
        /**
         * column name
         */
        public final static String STATUS = "status";
        /**
         * column name
         */
        public final static String MESSAGE_TYPE = "type";
        /**
         * column values
         */
        public static final String MESSAGE_TYPE_ALL = "0";
        public static final String MESSAGE_TYPE_INBOX = "1";
        public static final String MESSAGE_TYPE_SENT = "2";
        public static final String MESSAGE_TYPE_DRAFT = "3";
        public static final String MESSAGE_TYPE_OUTBOX = "4";
        public static final String MESSAGE_TYPE_FAILED = "5"; // for failed
                                                              // outgoing
                                                              // messages
        public static final String MESSAGE_TYPE_QUEUED = "6"; // for sms to send
                                                              // later

        /**
         * column name
         */
        public final static String REPLY_PATH_PRESENT = "reply_path_present";
        /**
         * column name
         */
        public final static String SUBJECT = "subject";
        /**
         * column name
         */
        public final static String BODY = "body";
        /**
         * column name
         */
        public final static String SERVICE_CENTER = "service_center";
        /**
         * column name
         */
        public final static String LOCKED = "locked";
        /**
         * column name
         */
        public final static String ERROR_CODE = "error_code";
        /**
         * column name
         */
        public final static String SEEN = "seen";
    }
}
