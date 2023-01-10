package core.threebanders.recordr.recorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.acra.ACRA;

import core.threebanders.recordr.Const;
import core.threebanders.recordr.Core;
import core.threebanders.recordr.CoreUtil;
import core.threebanders.recordr.R;
import core.threebanders.recordr.data.Contact;
import core.threebanders.recordr.data.Recording;

public class RecorderService extends Service {
    public static final int NOTIFICATION_ID = 1;
    public static final int RECORD_AUTOMATICALLY = 1;
    public static final int RECORD_ERROR = 4;
    public static final int RECORD_SUCCESS = 5;
    static final String ACTION_STOP_SPEAKER = "net.synapticweb.callrecorder.STOP_SPEAKER";
    static final String ACTION_START_SPEAKER = "net.synapticweb.callrecorder.START_SPEAKER";
    static final String ACRA_PHONE_NUMBER = "phone_number";
    static final String ACRA_INCOMING = "incoming";
    private static final String CHANNEL_ID = "call_recorder_channel";
    private static RecorderService self;
    private String receivedNumPhone = null;
    private boolean privateCall = false;
    private Boolean incoming = null;
    private Recorder recorder;
    private Thread speakerOnThread;
    private AudioManager audioManager;
    private NotificationManager nm;
    private boolean speakerOn = false;
    private Contact contact = null;
    private String callIdentifier;
    private SharedPreferences settings;

    public static RecorderService getService() {
        return self;
    }

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        recorder = new Recorder(getApplicationContext());
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        nm = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        settings = Core.getInstance().getCache().getPrefs();

        self = this;
    }

    public Recorder getRecorder() {
        return recorder;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        CharSequence name = getString(R.string.recorder);
        String description = getString(R.string.call_controllers);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(mChannel);
    }

    public Notification buildNotification(int typeOfNotification, String message) {
        try {
            Intent goToActivity = new Intent(getApplicationContext(), Core.getNotifyGoToActivity());
            PendingIntent tapNotificationPi = PendingIntent.getActivity(getApplicationContext(),
                    0, goToActivity, PendingIntent.FLAG_IMMUTABLE);

            Intent sendBroadcast = new Intent(getApplicationContext(), ControlRecordingReceiver.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                createChannel();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(Core.getNotificationIcon())
                    .setContentTitle(callIdentifier + (incoming ? getString(R.string.incoming) : getString(R.string.outgoing)))
                    .setContentIntent(tapNotificationPi);

            switch (typeOfNotification) {
                case RECORD_AUTOMATICALLY:
                    if (audioManager.isSpeakerphoneOn() || speakerOn) {
                        sendBroadcast.setAction(ACTION_STOP_SPEAKER);
                        PendingIntent stopSpeakerPi = PendingIntent.getBroadcast(Core.getContext(),
                                0, sendBroadcast, PendingIntent.FLAG_IMMUTABLE);
                        builder.addAction(new NotificationCompat.Action.Builder(Core.getIconSpeakerOff(),
                                        getString(R.string.stop_speaker), stopSpeakerPi).build())
                                .setContentText(getString(R.string.speaker_on));
                    } else {
                        sendBroadcast.setAction(ACTION_START_SPEAKER);
                        PendingIntent startSpeakerPi = PendingIntent.getBroadcast(getApplicationContext(),
                                0, sendBroadcast, PendingIntent.FLAG_IMMUTABLE);
                        builder.addAction(new NotificationCompat.Action.Builder(Core.getIconSpeakerOn(),
                                        getString(R.string.start_speaker), startSpeakerPi).build())
                                .setContentText(getString(R.string.speaker_off));
                    }
                    break;

                case RECORD_ERROR:
                    builder.setColor(Color.RED)
                            .setColorized(true)
                            .setSmallIcon(Core.getIconFailure())
                            .setContentTitle(getString(R.string.call_recorder_or_not))
                            .setContentText(message)
                            .setAutoCancel(true);
                    break;

                case RECORD_SUCCESS:
                    builder.setSmallIcon(Core.getIconSuccess())
                            .setContentText(getString(R.string.call_record_success))
                            .setAutoCancel(true);
            }

            return builder.build();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);


        if (intent.hasExtra(CallReceiver.ARG_NUM_PHONE)) {
            receivedNumPhone = intent.getStringExtra(CallReceiver.ARG_NUM_PHONE);
        }

        incoming = intent.getBooleanExtra(CallReceiver.ARG_INCOMING, false);

        try {
            ACRA.getErrorReporter().putCustomData(ACRA_PHONE_NUMBER, receivedNumPhone);
            ACRA.getErrorReporter().putCustomData(ACRA_INCOMING, incoming.toString());
        } catch (IllegalStateException ignored) {
        }

        if (receivedNumPhone == null && incoming && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            privateCall = true;
        }

        if (receivedNumPhone != null) {
            contact = Contact.queryNumberInAppContacts(Core.getRepository(), receivedNumPhone);

            if (contact == null) {
                {
                    contact = Contact.queryNumberInPhoneContacts(receivedNumPhone, getContentResolver());
                }

                if (contact == null) {
                    {
                        contact = new Contact(null, receivedNumPhone, getString(R.string.unknown_contact),
                                null, CoreUtil.UNKNOWN_TYPE_PHONE_CODE);
                    }
                }
                try {
                    contact.save(Core.getRepository());
                } catch (SQLException ignored) {
                }
            }
        }

        if (contact != null) {
            String name = contact.getContactName();
            callIdentifier = name.equals(getString(R.string.unknown_contact)) ?
                    receivedNumPhone : name;
        } else if (privateCall)
            callIdentifier = getString(R.string.hidden_number);
        else
            callIdentifier = getString(R.string.unknown_phone_number);

        try {

            recorder.startRecording(receivedNumPhone);

            if (settings.getBoolean(Const.SPEAKER_USE, false)) {
                putSpeakerOn();
            }

            Notification notification = buildNotification(RECORD_AUTOMATICALLY, "");

            if (notification != null) {
                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (RecordingException e) {
            Notification notification = buildNotification(RECORD_ERROR,
                    getString(R.string.cannot_start_record));

            if (notification != null) {
                startForeground(NOTIFICATION_ID, notification);
            }
        }

        return START_NOT_STICKY;
    }

    private void resetState() {
        self = null;
    }

    void putSpeakerOn() {
        speakerOnThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!Thread.interrupted()) {
                        audioManager.setMode(AudioManager.MODE_IN_CALL);

                        if (!audioManager.isSpeakerphoneOn()) {
                            audioManager.setSpeakerphoneOn(true);
                        }

                        sleep(500);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        speakerOnThread.start();
        speakerOn = true;
    }

    void putSpeakerOff() {
        if (speakerOnThread != null) {
            speakerOnThread.interrupt();
        }

        speakerOnThread = null;

        if (audioManager != null && audioManager.isSpeakerphoneOn()) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
        }

        speakerOn = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (audioManager.isBluetoothScoOn()) {
            audioManager.setBluetoothScoOn(false);
            audioManager.stopBluetoothSco();
        }
        putSpeakerOff();
        if (!recorder.isRunning() || recorder.hasError()) {
            onDestroyCleanUp();

            return;
        }

        recorder.stopRecording();
        Long contactId;

        if (privateCall) {
            contactId = Core.getRepository().getHiddenNumberContactId();

            if (contactId == null) {
                Contact contact = new Contact();
                contact.setIsPrivateNumber();
                contact.setContactName(getString(R.string.hidden_number));

                try {
                    contact.save(Core.getRepository());
                } catch (SQLException exc) {
                    onDestroyCleanUp();
                    return;
                }

                contactId = contact.getId();
            }
        } else if (contact != null) {
            contactId = contact.getId();
        } else {
            contactId = null;
        }

        Recording recording = new Recording(null, contactId, recorder.getAudioFilePath(), incoming,
                recorder.getStartingTime(), System.currentTimeMillis(), recorder.getFormat(),
                false, recorder.getMode(), recorder.getSource());

        try {
            recording.save(Core.getRepository());
        } catch (SQLException exc) {
            onDestroyCleanUp();

            return;
        }

        nm.notify(NOTIFICATION_ID, buildNotification(RECORD_SUCCESS, ""));

        onDestroyCleanUp();
    }

    private void onDestroyCleanUp() {
        resetState();

        try {
            ACRA.getErrorReporter().clearCustomData();
        } catch (IllegalStateException ignored) {
        }
    }
}
