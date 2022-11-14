/*
 * Copyright (C) 2019 Eugen Rădulescu <synapticwebb@gmail.com> - All rights reserved.
 *
 * You may use, distribute and modify this code only under the conditions
 * stated in the SW Call Recorder license. You should have received a copy of the
 * SW Call Recorder license along with this file. If not, please write to <synapticwebb@gmail.com>.
 */
package com.threebanders.recordrs.ui.setup

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.threebanders.recordrs.R
import com.threebanders.recordrs.ui.BaseActivity
import com.threebanders.recordrs.ui.contact.ContactsListActivityMain

class SetupActivity : BaseActivity() {
    var checkResult = 0
        private set

    override fun createFragment(): Fragment? {
        return if (checkResult and ContactsListActivityMain.EULA_NOT_ACCEPTED != 0) SetupEulaFragment() else if (checkResult and ContactsListActivityMain.PERMS_NOT_GRANTED != 0) SetupPermissionsFragment() else if (checkResult and ContactsListActivityMain.POWER_OPTIMIZED != 0) SetupPowerFragment() else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setup_activity)
        checkResult = intent.getIntExtra(
            ContactsListActivityMain.Companion.SETUP_ARGUMENT,
            ContactsListActivityMain.Companion.EULA_NOT_ACCEPTED and ContactsListActivityMain.Companion.PERMS_NOT_GRANTED and
                    ContactsListActivityMain.Companion.POWER_OPTIMIZED
        )
        insertFragment(R.id.setup_fragment_container)
    }

    fun cancelSetup() {
        val intent = Intent()
        intent.putExtra(EXIT_APP, true)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        cancelSetup()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        const val EXIT_APP = "exit_app"
    }
}
