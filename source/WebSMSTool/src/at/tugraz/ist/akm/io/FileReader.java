package at.tugraz.ist.akm.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

public class FileReader {
    private final Context context;
    private final String filePath;

    public FileReader(final Context context, final String filePath) {
        this.context = context;
        this.filePath = filePath;
    }

    public String read() {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader reader = null;

        StringBuilder builder = new StringBuilder();
        try {
            is = context.getAssets().open(filePath);
            isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
}