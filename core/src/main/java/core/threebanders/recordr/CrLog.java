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
    private static final File logFile = new File(LOG_FOLDER, LOG_FILE_NAME + Core.getContext().getString(R.string.txt));

    private static void backupLogFiles() throws LoggerException {
        File backup = null;

        for (int i = 1; i <= MAX_FILE_COUNT; ++i) {
            backup = new File(LOG_FOLDER, LOG_FILE_NAME + i + Core.getContext().getString(R.string.txt));
            if (!backup.exists()) {
                backup = new File(LOG_FOLDER, LOG_FILE_NAME + i + Core.getContext().getString(R.string.txt));
                break;
            }
            if (i == MAX_FILE_COUNT) {
                File firstBackup = new File(LOG_FOLDER, LOG_FILE_NAME + Core.getContext().getString(R.string.one_txt));
                if (!firstBackup.delete())
                    throw new LoggerException(Core.getContext().getString(R.string.cannot_delete_last_backup));

                for (int j = 2; j <= MAX_FILE_COUNT; ++j) {
                    File currentBackup = new File(LOG_FOLDER, LOG_FILE_NAME + j + Core.getContext().getString(R.string.txt));
                    if (!currentBackup.renameTo(new File(LOG_FOLDER, LOG_FILE_NAME + (j - 1) + Core.getContext().getString(R.string.txt))))
                        throw new LoggerException(Core.getContext().getString(R.string.cannot_delete_last_backup) + currentBackup.getName());
                }
                backup = new File(LOG_FOLDER, LOG_FILE_NAME + MAX_FILE_COUNT + Core.getContext().getString(R.string.txt));
            }
        }

        if (!logFile.renameTo(backup))
            throw new LoggerException(Core.getContext().getString(R.string.could_not_rename_log_file));
    }

    private static void writeHeader() throws IOException {
        String header = "";
        header += Core.getContext().getString(R.string.app_version_code) + Core.getVersionCode() + "\n";
        header += Core.getContext().getString(R.string.app_version) + Core.getVersionName() + "\n";
        header += Core.getContext().getString(R.string.model) + Build.MODEL + "\n";
        header += Core.getContext().getString(R.string.manufacturer) + Build.MANUFACTURER + "\n";
        header += Core.getContext().getString(R.string.sdk) + Build.VERSION.SDK_INT + "\n";
        header += Core.getContext().getString(R.string.board) + Build.BOARD + "\n";
        header += Core.getContext().getString(R.string.brand) + Build.BRAND + "\n";
        header += Core.getContext().getString(R.string.device) + Build.DEVICE + "\n";
        header += Core.getContext().getString(R.string.display_name) + Build.DISPLAY + "\n";
        header += Core.getContext().getString(R.string.hardware) + Build.HARDWARE + "\n";
        header += Core.getContext().getString(R.string.product) + Build.PRODUCT + "\n";
        header += Core.getContext().getString(R.string.width_pixels) + Core.getContext().getResources().getDisplayMetrics().widthPixels + "\n";
        header += Core.getContext().getString(R.string.height_pixels) + Core.getContext().getResources().getDisplayMetrics().heightPixels + "\n";
        header += Core.getContext().getString(R.string.density_pixels) + Core.getContext().getResources().getDisplayMetrics().density + "\n\n";

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
                    throw new LoggerException(Core.getContext().getString(R.string.cannot_create_log_file));
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
                    throw new LoggerException(Core.getContext().getString(R.string.cannot_create_log_file));
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
