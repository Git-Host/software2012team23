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

package at.tugraz.ist.akm.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

public class FileReader {
    private final Context mContext;
    private final String mFilePath;

    public FileReader(final Context context, final String filePath) {
        this.mContext = context;
        this.mFilePath = filePath;
    }

    public String read() {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader reader = null;

        StringBuilder builder = new StringBuilder();
        try {
            is = mContext.getAssets().open(mFilePath);
            isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return builder.toString();
    }
}