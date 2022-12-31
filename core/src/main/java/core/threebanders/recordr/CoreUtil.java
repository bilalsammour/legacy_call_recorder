package core.threebanders.recordr;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CoreUtil {
    public static final int UNKNOWN_TYPE_PHONE_CODE = -1;


    public static String getDurationHuman(long millis, boolean spokenStyle) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        if (spokenStyle) {
            String duration = "";
            if (hours > 0)
                duration += (hours + Core.getContext().getString(R.string.hour) + (hours > 1 ? Core.getContext().getString(R.string.s) : ""));
            if (minutes > 0)
                duration += ((hours > 0 ? ", " : "") + minutes + Core.getContext().getString(R.string.minute) + (minutes > 1 ? Core.getContext().getString(R.string.s) : ""));
            if (seconds > 0)
                duration += ((minutes > 0 || hours > 0 ? ", " : "") + seconds + Core.getContext().getString(R.string.second) + (seconds > 1 ? Core.getContext().getString(R.string.s) : ""));
            return duration;
        } else {
            if (hours > 0)
                return String.format(Locale.US, Core.getContext().getString(R.string.three_d_format), hours, minutes, seconds);
            else
                return String.format(Locale.US, Core.getContext().getString(R.string.two_d_format), minutes, seconds);
        }
    }

    public static String getFileSizeHuman(long size) {
        double numUnits = size >> 10;
        String unit = Core.getContext().getString(R.string.kb);
        if (numUnits > 1000) {
            numUnits = (int) size >> 20;
            unit = Core.getContext().getString(R.string.mb);
            double diff = (size - numUnits * 1048576) / 1048576;
            numUnits = numUnits + diff;
            if (numUnits > 1000) {
                numUnits = size / 1099511627776L;
                unit = Core.getContext().getString(R.string.gb);
                diff = (size - numUnits * 1099511627776L) / 1099511627776L;
                numUnits = numUnits + diff;
            }
        }
        return new DecimalFormat(Core.getContext().getString(R.string.diez)).format(numUnits) + " " + unit;
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
            CrLog.log(CrLog.ERROR, Core.getContext().getString(R.string.error_converting_raw_html_to_string) + e.getMessage());
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
