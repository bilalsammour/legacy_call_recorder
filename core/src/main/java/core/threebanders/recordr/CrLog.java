package core.threebanders.recordr;

import android.os.Build;
import android.util.Log;

import androidx.annotation.StringDef;

import com.afollestad.materialdialogs.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrLog {
    public static final String DEBUG = " DEBUG ";
    public static final String WARN = " WARN ";
    public static final String ERROR = " ERROR ";
    private static final String TAG = "CallRecorder";
    private final static int MAX_FILE_SIZE = 1000000;
    private final static int MAX_FILE_COUNT = 5;
    private final static String LOG_FILE_NAME = "log";
    private final static File LOG_FOLDER = Core.getContext().getFilesDir();
    private static final File logFile = new File(LOG_FOLDER, LOG_FILE_NAME + ".txt");

    private static void backupLogFiles() throws LoggerException {
        File backup = null;

        for (int i = 1; i <= MAX_FILE_COUNT; ++i) {
            backup = new File(LOG_FOLDER, LOG_FILE_NAME + i + ".txt");
            if (!backup.exists()) {
                backup = new File(LOG_FOLDER, LOG_FILE_NAME + i + ".txt");
                break;
            }
            if (i == MAX_FILE_COUNT) {
                File firstBackup = new File(LOG_FOLDER, LOG_FILE_NAME + "1.txt");
                if (!firstBackup.delete())
                    throw new LoggerException("Cannot delete last backup");

                for (int j = 2; j <= MAX_FILE_COUNT; ++j) {
                    File currentBackup = new File(LOG_FOLDER, LOG_FILE_NAME + j + ".txt");
                    if (!currentBackup.renameTo(new File(LOG_FOLDER, LOG_FILE_NAME + (j - 1) + ".txt")))
                        throw new LoggerException("Could not rename backup file " + currentBackup.getName());
                }
                backup = new File(LOG_FOLDER, LOG_FILE_NAME + MAX_FILE_COUNT + ".txt");
            }
        }

        if (!logFile.renameTo(backup))
            throw new LoggerException("Could not rename log file");
    }

    private static void writeHeader() throws IOException {
        String header = "";
        header += "APP VERSION CODE: " + Core.getVersionCode() + "\n";
        header += "APP VERSION: " + Core.getVersionName() + "\n";
        header += "MODEL: " + Build.MODEL + "\n";
        header += "MANUFACTURER: " + Build.MANUFACTURER + "\n";
        header += "SDK: " + Build.VERSION.SDK_INT + "\n";
        header += "BOARD: " + Build.BOARD + "\n";
        header += "BRAND: " + Build.BRAND + "\n";
        header += "DEVICE: " + Build.DEVICE + "\n";
        header += "DISPLAY NAME: " + Build.DISPLAY + "\n";
        header += "HARDWARE: " + Build.HARDWARE + "\n";
        header += "PRODUCT: " + Build.PRODUCT + "\n";
        header += "WIDTH PIXELS: " + Core.getContext().getResources().getDisplayMetrics().widthPixels + "\n";
        header += "HEIGHT PIXELS: " + Core.getContext().getResources().getDisplayMetrics().heightPixels + "\n";
        header += "DENSITY PIXELS: " + Core.getContext().getResources().getDisplayMetrics().density + "\n\n";

        BufferedWriter buffer = new BufferedWriter(new FileWriter(logFile, true));
        buffer.append(header);
        buffer.newLine();
        buffer.close();
    }

    public static void log(@levels String level, String message) {
        if (BuildConfig.DEBUG) {
            switch (level) {
                case DEBUG:
                    Log.d(TAG, message);
                    break;
                case WARN:
                    Log.w(TAG, message);
                    break;
                case ERROR:
                    Log.wtf(TAG, message);
            }
        }

        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile())
                    throw new LoggerException("Cannot create log file.");
                writeHeader();
            } catch (LoggerException | IOException e) {
                Log.wtf(TAG, e.getMessage());
                return;
            }
        }
        //check the file size
        if (logFile.length() > MAX_FILE_SIZE) {
            try {
                backupLogFiles();
                if (!logFile.createNewFile())
                    throw new LoggerException("Cannot create log file.");
                writeHeader();
            } catch (IOException | LoggerException e) {
                Log.wtf(TAG, e.getMessage());
                return;
            }
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.US).format(
                new Date(System.currentTimeMillis()));

        try {
            BufferedWriter buffer = new BufferedWriter(new FileWriter(logFile, true));
            buffer.append(timestamp)
                    .append(level)
                    .append(message);
            buffer.newLine();
            buffer.close();
        } catch (IOException e) {
            Log.wtf(TAG, e.getMessage());
        }
    }

    @StringDef({DEBUG, WARN, ERROR})
    @Retention(RetentionPolicy.SOURCE)
    @interface levels {
    }

    static class LoggerException extends Exception {
        LoggerException(String message) {
            super(message);
        }
    }
}
