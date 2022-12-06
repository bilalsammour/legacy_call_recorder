package core.threebanders.recordr;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CoreUtil {
    public static final int UNKNOWN_TYPE_PHONE_CODE = -1;

    public static final List<Integer> colorList = new ArrayList<>(Arrays.asList(
            0xFF7b569b,
            0xFFb8ad38,
            0xFF586dd7,
            0xFF45aecf,
            0xFFd9a26a,
            0xFFe26855,
            0xFF8c6d2c,
            0xFFa4572e
    ));


    public static String getDurationHuman(long millis, boolean spokenStyle) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        if (spokenStyle) {
            String duration = "";
            if (hours > 0)
                duration += (hours + " hour" + (hours > 1 ? "s" : ""));
            if (minutes > 0)
                duration += ((hours > 0 ? ", " : "") + minutes + " minute" + (minutes > 1 ? "s" : ""));
            if (seconds > 0)
                duration += ((minutes > 0 || hours > 0 ? ", " : "") + seconds + " second" + (seconds > 1 ? "s" : ""));
            return duration;
        } else {
            if (hours > 0)
                return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
            else
                return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }

    public static String getFileSizeHuman(long size) {
        double numUnits = size >> 10;
        String unit = "KB";
        if (numUnits > 1000) {
            numUnits = (int) size >> 20;
            unit = "MB";
            double diff = (size - numUnits * 1048576) / 1048576;
            numUnits = numUnits + diff;
            if (numUnits > 1000) {
                numUnits = size / 1099511627776L;
                unit = "GB";
                diff = (size - numUnits * 1099511627776L) / 1099511627776L;
                numUnits = numUnits + diff;
            }
        }
        return new DecimalFormat("#.#").format(numUnits) + " " + unit;
    }

    public static String rawHtmlToString(int fileRes, Context context) {
        StringBuilder sb = new StringBuilder();
        try {
            InputStream is = context.getResources().openRawResource(fileRes);
            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();
            is.close();
        } catch (Exception e) {
            CrLog.log(CrLog.ERROR, "Error converting raw html to string: " + e.getMessage());
        }
        return sb.toString();
    }

    public static class PhoneTypeContainer {
        private final int typeCode;
        private final String typeName;

        PhoneTypeContainer(int code, String name) {
            typeCode = code;
            typeName = name;
        }

        @Override
        @NonNull
        public String toString() {
            return typeName;
        }

        public int getTypeCode() {
            return typeCode;
        }
    }
}
