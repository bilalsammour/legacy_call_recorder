package core.threebanders.recordr;

import static android.media.MediaRecorder.AudioSource.VOICE_RECOGNITION;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Cache {
    public static final String ENABLED = Core.getContext().getString(R.string.enabled);
    public static final String SOURCE = Core.getContext().getString(R.string.source);
    public static final String FORMAT = Core.getContext().getString(R.string.format);
    public static final String MODE = Core.getContext().getString(R.string.mode);

    private static Cache cache;
    private SharedPreferences prefs;

    public static Cache getInstance(Context context) {
        if (cache == null) {
            cache = new Cache();
            cache.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        }

        return cache;
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public boolean enabled() {
        return prefs.getBoolean(ENABLED, true);
    }

    public void format(String format) {
        prefs.edit().putString(FORMAT, format).apply();
    }

    public String format() {
        return prefs.getString(FORMAT, "");
    }

    public void mode(String format) {
        prefs.edit().putString(MODE, format).apply();
    }

    public String mode() {
        return prefs.getString(MODE, "");
    }

    public void source(String format) {
        prefs.edit().putString(SOURCE, format).apply();
    }

    public String source() {
        return prefs.getString(SOURCE, String.valueOf(VOICE_RECOGNITION));
    }
}
