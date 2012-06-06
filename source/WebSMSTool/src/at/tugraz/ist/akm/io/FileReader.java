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