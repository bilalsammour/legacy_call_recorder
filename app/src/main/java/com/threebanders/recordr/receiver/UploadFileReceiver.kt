package com.threebanders.recordr.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import androidx.core.content.edit
import com.threebanders.recordr.common.Extras
import java.io.File

class UploadFileReceiver : BroadcastReceiver() {
    private var prevState = TelephonyManager.EXTRA_STATE_IDLE

    override fun onReceive(context: Context?, intent: Intent?) {

        if(!intent?.action.equals("android.intent.action.PHONE_STATE") && !intent?.action.equals("android.intent.action.NEW_OUTGOING_CALL")){
            return
        }

        val bundle = intent!!.extras ?: return
        val state = bundle.getString(TelephonyManager.EXTRA_STATE)
        val audioPath = Extras.getAudioPath(context)
        if (state == TelephonyManager.EXTRA_STATE_IDLE && prevState == TelephonyManager.EXTRA_STATE_IDLE) {
            if (audioPath.isNotEmpty()) {
                Extras.uploadFileToGDrive(File(audioPath), context)
                Extras.getSharedPrefsInstance(context).edit { clear() }
            }
        }
        prevState = state
    }

}