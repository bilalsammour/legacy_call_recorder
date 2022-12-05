package com.threebanders.recordr.ui.setup

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.common.Extras.EULA_NOT_ACCEPTED
import com.threebanders.recordr.ui.BaseActivity

class SetupActivity : BaseActivity() {
    var checkResult = 0
        private set

    override fun createFragment(): Fragment? {
        return if (checkResult and EULA_NOT_ACCEPTED != 0)
            SetupEulaFragment()
        else if (checkResult and Extras.PERMS_NOT_GRANTED != 0)
            SetupPermissionsFragment()
        else if (checkResult and Extras.POWER_OPTIMIZED != 0)
            SetupPowerFragment() else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setup_activity)

        checkResult = intent.getIntExtra(
            Extras.SETUP_ARGUMENT,
            EULA_NOT_ACCEPTED
                    and Extras.PERMS_NOT_GRANTED and
                    Extras.POWER_OPTIMIZED
        )
        insertFragment(R.id.setup_fragment_container)

        // onBackPressed
        onBackPressedDispatcher.addCallback(this , object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Back is pressed... Finishing the activity
                cancelSetup()
            }
        })
    }

    fun cancelSetup() {
        val intent = Intent()
        intent.putExtra(EXIT_APP, true)
        setResult(RESULT_OK, intent)
        finish()
    }




    companion object {
        const val EXIT_APP = "exit_app"
    }
}
