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

        if (!BuildConfig.DEBUG) {
            ACRA.init(this)
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        core = Core.Builder.newInstance()
            .setContext(applicationContext)
            .setNotifyGoToActivity(ContactsListActivityMain::class.java)
            .build()
    }

    companion object {
        lateinit var instance: CrApp
            private set
    }
}