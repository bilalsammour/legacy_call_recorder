/*
 * Copyright (C) 2019 Eugen Rădulescu <synapticwebb@gmail.com> - All rights reserved.
 *
 * You may use, distribute and modify this code only under the conditions
 * stated in the SW Call Recorder license. You should have received a copy of the
 * SW Call Recorder license along with this file. If not, please write to <synapticwebb@gmail.com>.
 */
package com.threebanders.recordr

import android.app.Application
import android.content.Context
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import core.threebanders.recordr.Core
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraHttpSender
import org.acra.data.StringFormat
import org.acra.sender.HttpSender

@AcraCore(reportFormat = StringFormat.KEY_VALUE_LIST)
@AcraHttpSender(uri = "http://crashes.infopsihologia.ro", httpMethod = HttpSender.Method.POST)
class CrApp : Application() {
    lateinit var core: Core
        private set

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (!BuildConfig.DEBUG) ACRA.init(this)
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        //        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
//        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        core = Core.Builder.newInstance()
            .setContext(applicationContext)
            .setNotifyGoToActivity(ContactsListActivityMain::class.java)
            .setNotificationIcon(R.drawable.notification_icon)
            .setIconSpeakerOff(R.drawable.speaker_phone_off)
            .setIconSuccess(R.drawable.speaker_phone_on)
            .setIconFailure(R.drawable.notification_icon_error)
            .setIconSpeakerOn(R.drawable.speaker_phone_on)
            .setVersionCode(BuildConfig.VERSION_CODE)
            .setVersionName(BuildConfig.VERSION_NAME)
            .build()
    }

    companion object {
        private val TAG = CrApp::class.java.simpleName

        lateinit var instance: CrApp
            private set
    }
}