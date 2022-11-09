package core.threebanders.recordr;

import static android.media.MediaRecorder.AudioSource.VOICE_RECOGNITION;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import core.threebanders.recordr.data.Recording;


public class Cache {

    private static final String APP_CACHE_FILE = "cache_1";
    private SharedPreferences prefs;
    private Context context;
    private static Cache cache;
    public static final String ENABLED = "enabled";
    public static final String SOURCE = "source";
    public static final String FORMAT = "format";
    public static final String MODE = "mode";
    public static final String STORAGE = "storage";
    public static final String STORAGE_PATH = "public_storage_path";
    public static final String RECODINGS_LIST = "RECODINGS_LIST";

    Cache(Context context) {
        this.context = context;
    }

    public static Cache getInstance(Context context) {

        if (cache == null) {
            cache = new Cache(context);
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

    public void enabled(Boolean enabled) {
        prefs.edit().putBoolean(ENABLED, enabled).apply();
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

    public void storage(String format) {
        prefs.edit().putString(STORAGE, format).apply();
    }

    public String storage() {
        return prefs.getString(STORAGE, "");
    }

    public void storagePath(String format) {
        prefs.edit().putString(STORAGE_PATH, format).apply();
    }

    public String storagePath() {
        return prefs.getString(STORAGE_PATH, null);
    }

    public void source(String format) {
        prefs.edit().putString(SOURCE, format).apply();
    }

    public String source() {
        return prefs.getString(SOURCE, String.valueOf(VOICE_RECOGNITION));
    }


    public <T> void setRecodingsList(String key, List<T> list, SharedPreferences sharedPref) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        sharedPref.edit().putString(key, json);
        sharedPref.edit().commit();
    }


    public List<Recording> getRecodingsListList(SharedPreferences sharedPref) {
        Gson gson = new Gson();
        List<Recording> productFromShared = new ArrayList<>();
        String jsonPreferences = sharedPref.getString(RECODINGS_LIST, "");

        Type type = new TypeToken<List<Recording>>() {
        }.getType();
        productFromShared = gson.fromJson(jsonPreferences, type);

        return productFromShared;
    }
}
