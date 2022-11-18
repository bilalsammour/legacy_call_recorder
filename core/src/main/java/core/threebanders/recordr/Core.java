package core.threebanders.recordr;

import android.content.Context;
import android.content.SharedPreferences;

import core.threebanders.recordr.data.Repository;
import core.threebanders.recordr.data.RepositoryImpl;

public class Core {
    private static Core instance;
    private static Context context;
    private static int notificationIcon;
    private static int iconSpeakerOff;
    private static int iconSpeakerOn;
    private static int iconSuccess;
    private static int iconFailure;
    private static Class<?> notifyGoToActivity;
    private static int versionCode;
    private static String versionName;
    private static Repository repository;

    public Core(Builder builder) {
        this.context = builder.context;
        this.notificationIcon = builder.notificationIcon;
        int iconSmallIcon = builder.iconSmallIcon;
        this.iconSpeakerOff = builder.iconSpeakerOff;
        this.iconSpeakerOn = builder.iconSpeakerOn;
        this.iconSuccess = builder.iconSuccess;
        this.iconFailure = builder.iconFailure;
        this.notifyGoToActivity = builder.notifyGoToActivity;
        this.versionCode = builder.versionCode;
        this.versionName = builder.versionName;
        this.repository = builder.repository;
    }

    public static Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static int getNotificationIcon() {
        return notificationIcon;
    }

    public static int getIconSpeakerOff() {
        return iconSpeakerOff;
    }

    public static int getIconSpeakerOn() {
        return iconSpeakerOn;
    }

    public static int getIconSuccess() {
        return iconSuccess;
    }

    public static int getIconFailure() {
        return iconFailure;
    }

    public static Class<?> getNotifyGoToActivity() {
        return notifyGoToActivity;
    }

    public static int getVersionCode() {
        return versionCode;
    }

    public static String getVersionName() {
        return versionName;
    }

    public static Repository getRepository() {
        return repository;
    }

    public static Core getInstance() {
        return instance;
    }

    public Cache getCache() {
        return Cache.getInstance(context);
    }

    public SharedPreferences getPrefs() {
        return getCache().getPrefs();
    }

    public static class Builder {
        private Context context;
        private int notificationIcon;
        private int iconSmallIcon;
        private int iconSpeakerOff;
        private int iconSpeakerOn;
        private int iconSuccess;
        private int iconFailure;
        private Class<?> notifyGoToActivity;
        private int versionCode;
        private String versionName;
        private Repository repository;

        public Builder() {
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Context getContext() {
            return context;
        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setNotifyGoToActivity(Class<?> notifyGoToActivity) {
            this.notifyGoToActivity = notifyGoToActivity;
            return this;
        }

        public Core build() {
            repository = new RepositoryImpl(context, Const.DATABASE_NAME);
            instance = new Core(this);
            return instance;
        }
    }
}
