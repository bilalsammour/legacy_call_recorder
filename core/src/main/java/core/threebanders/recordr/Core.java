package core.threebanders.recordr;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import core.threebanders.recordr.data.Recording;
import core.threebanders.recordr.data.Repository;
import core.threebanders.recordr.data.RepositoryImpl;

public class Core {


    private static Core instance;
    private static Context context;
    private static int notificationIcon;
    private int iconSmallIcon;
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
        this.iconSmallIcon = builder.iconSmallIcon;
        this.iconSpeakerOff = builder.iconSpeakerOff;
        this.iconSpeakerOn = builder.iconSpeakerOn;
        this.iconSuccess = builder.iconSuccess;
        this.iconFailure = builder.iconFailure;
        this.notifyGoToActivity = builder.notifyGoToActivity;
        this.versionCode = builder.versionCode;
        this.versionName = builder.versionName;
        this.repository = builder.repository;
    }

    public Cache getCache() {
        return Cache.getInstance(context);
    }

//    public static void setCache(Cache cache) {
//        Core.cache = cache;
//    }

    public static Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static int getNotificationIcon() {
        return notificationIcon;
    }

    public void setNotificationIcon(int notificationIcon) {
        this.notificationIcon = notificationIcon;
    }

    public int getIconSmallIcon() {
        return iconSmallIcon;
    }

    public void setIconSmallIcon(int iconSmallIcon) {
        this.iconSmallIcon = iconSmallIcon;
    }

    public static int getIconSpeakerOff() {
        return iconSpeakerOff;
    }

    public void setIconSpeakerOff(int iconSpeakerOff) {
        this.iconSpeakerOff = iconSpeakerOff;
    }

    public static int getIconSpeakerOn() {
        return iconSpeakerOn;
    }

    public void setIconSpeakerOn(int iconSpeakerOn) {
        this.iconSpeakerOn = iconSpeakerOn;
    }

    public static int getIconSuccess() {
        return iconSuccess;
    }

    public void setIconSuccess(int iconSuccess) {
        this.iconSuccess = iconSuccess;
    }

    public static int getIconFailure() {
        return iconFailure;
    }

    public void setIconFailure(int iconFailure) {
        this.iconFailure = iconFailure;
    }

    public static Class<?> getNotifyGoToActivity() {
        return notifyGoToActivity;
    }

    public void setNotifyGoToActivity(Class<?> notifyGoToActivity) {
        this.notifyGoToActivity = notifyGoToActivity;
    }

    public static int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public static String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public static Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public SharedPreferences getPrefs() {
        return getCache().getPrefs();
    }

    public static Core getInstance() {
        return instance;
    }

    public static class Builder {

        public static Builder newInstance() {
            return new Builder();
        }

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
        private static Cache cache;
        private Repository repository;

        public Builder() {
        }

        public Context getContext() {
            return context;
        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Repository getRepository() {
            return repository;
        }

        private Builder setRepository(Repository repository) {
            this.repository = repository;
            return this;
        }

        public int getNotificationIcon() {
            return notificationIcon;
        }

        public Builder setNotificationIcon(int notificationIcon) {
            this.notificationIcon = notificationIcon;
            return this;
        }

        public int getIconSmallIcon() {
            return iconSmallIcon;
        }

        public Builder setIconSmallIcon(int iconSmallIcon) {
            this.iconSmallIcon = iconSmallIcon;
            return this;
        }

        public int getIconSpeakerOff() {
            return iconSpeakerOff;
        }

        public Builder setIconSpeakerOff(int iconSpeakerOff) {
            this.iconSpeakerOff = iconSpeakerOff;
            return this;
        }

        public int getIconSpeakerOn() {
            return iconSpeakerOn;
        }

        public Builder setIconSpeakerOn(int iconSpeakerOn) {
            this.iconSpeakerOn = iconSpeakerOn;
            return this;
        }

        public int getIconSuccess() {
            return iconSuccess;
        }

        public Builder setIconSuccess(int iconSuccess) {
            this.iconSuccess = iconSuccess;
            return this;
        }

        public int getIconFailure() {
            return iconFailure;
        }

        public Builder setIconFailure(int iconFailure) {
            this.iconFailure = iconFailure;
            return this;
        }

        public Class<?> getNotifyGoToActivity() {
            return notifyGoToActivity;
        }

        public Builder setNotifyGoToActivity(Class<?> notifyGoToActivity) {
            this.notifyGoToActivity = notifyGoToActivity;
            return this;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public Builder setVersionCode(int versionCode) {
            this.versionCode = versionCode;
            return this;
        }

        public String getVersionName() {
            return versionName;
        }

        public Builder setVersionName(String versionName) {
            this.versionName = versionName;
            return this;
        }

        public Core build() {
            repository = new RepositoryImpl(context, Const.DATABASE_NAME);
            instance = new Core(this);
            return instance;
        }
    }

    public static void move(String path, int totalSize, Activity parentActivity, Recording[] recordings) {
        new MoveAsyncTask(repository, path, totalSize, parentActivity).
                execute(recordings);
    }
}
