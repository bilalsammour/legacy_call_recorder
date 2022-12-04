package com.threebanders.recordr.ui.setup

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.threebanders.recordr.R
import com.threebanders.recordr.common.ContactsExtras
import com.threebanders.recordr.common.ContactsExtras.EULA_NOT_ACCEPTED
import com.threebanders.recordr.ui.BaseActivity

class SetupActivity : BaseActivity() {
    var checkResult = 0
        private set

    override fun createFragment(): Fragment? {
        return if (checkResult and EULA_NOT_ACCEPTED != 0)
            SetupEulaFragment()
        else if (checkResult and ContactsExtras.PERMS_NOT_GRANTED != 0)
            SetupPermissionsFragment()
        else if (checkResult and ContactsExtras.POWER_OPTIMIZED != 0)
            SetupPowerFragment() else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setup_activity)

        checkResult = intent.getIntExtra(
            ContactsExtras.SETUP_ARGUMENT,
            EULA_NOT_ACCEPTED
                    and ContactsExtras.PERMS_NOT_GRANTED and
                    ContactsExtras.POWER_OPTIMIZED
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

    companion object {
        const val EXIT_APP = "exit_app"
    }
}
