package core.threebanders.recordr.recorder;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import core.threebanders.recordr.Core;

public class CallReceiver extends BroadcastReceiver {
    public static final String ARG_NUM_PHONE = "arg_num_phone";
    public static final String ARG_INCOMING = "arg_incoming";
    private static boolean serviceStarted = false;
    private static ComponentName serviceName = null;

    public CallReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle;
        String state;
        String incomingNumber;
        String action = intent.getAction();

        if (action != null && action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            if ((bundle = intent.getExtras()) != null) {
                state = bundle.getString(TelephonyManager.EXTRA_STATE);

                if (state != null && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    incomingNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    boolean isEnabled = Core.getInstance().getCache().enabled();

                    if (!serviceStarted && isEnabled) {
                        Intent intentService = new Intent(context, RecorderService.class);
                        serviceName = intentService.getComponent();
                        intentService.putExtra(ARG_NUM_PHONE, incomingNumber);
                        intentService.putExtra(ARG_INCOMING, true);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intentService);
                        } else {
                            context.startService(intentService);
                        }

                        serviceStarted = true;
                    }
                } else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    boolean isEnabled = Core.getInstance().getCache().enabled();
                    if (!serviceStarted && isEnabled) {
                        Intent intentService = new Intent(context, RecorderService.class);
                        serviceName = intentService.getComponent();
                        intentService.putExtra(ARG_INCOMING, false);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intentService);
                        } else {
                            context.startService(intentService);
                        }

                        serviceStarted = true;
                    }
                } else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    if (serviceStarted) {
                        Intent stopIntent = new Intent(context, RecorderService.class);
                        stopIntent.setComponent(serviceName);
                        context.stopService(stopIntent);
                        serviceStarted = false;
                    }
                    serviceName = null;
                }
            }
        }
    }
}
