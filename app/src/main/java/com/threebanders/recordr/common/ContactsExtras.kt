package com.threebanders.recordr.common

import android.app.Activity
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.threebanders.recordr.R
import com.threebanders.recordr.ui.help.HelpActivity
import com.threebanders.recordr.ui.settings.SettingsActivity
import com.threebanders.recordr.ui.setup.SetupActivity

object ContactsExtras {
    const val SETUP_ACTIVITY = 3
    const val HAS_ACCEPTED_EULA = "has_accepted_eula"
    const val EULA_NOT_ACCEPTED = 1
    const val PERMS_NOT_GRANTED = 2
    const val POWER_OPTIMIZED = 4
    const val SETUP_ARGUMENT = "setup_arg"
    private const val ACCESSIBILITY_SETTINGS = 1991

    fun showExitDialog(context: Context, onBackPressed: () -> Unit) {
        MaterialDialog.Builder(context)
            .title(R.string.exit_app_title)
            .icon(ContextCompat.getDrawable(context, R.drawable.question_mark)!!)
            .content(R.string.exit_app_message)
            .positiveText(android.R.string.ok)
            .negativeText(android.R.string.cancel)
            .onPositive { _: MaterialDialog, _: DialogAction ->
                onBackPressed.invoke()
            }
            .show()
    }

    fun isMyServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager =
            context.getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }

        return false
    }

    fun showAccessibilitySettings(context: Activity) {
        val intent = Intent("android.settings.ACCESSIBILITY_SETTINGS")
        context.startActivityForResult(intent, ACCESSIBILITY_SETTINGS)
    }

    fun openSettingsActivity(context: Activity) {
        context.startActivity(
            Intent(context, SettingsActivity::class.java)
        )
    }

    fun openHelpActivity(context: Activity) {
        context.startActivity(
            Intent(context, HelpActivity::class.java)
        )
    }

    fun openSetupActivity(context: Activity, checkResult: Int) {
        val setupIntent = Intent(context, SetupActivity::class.java)
        setupIntent.putExtra(SETUP_ARGUMENT, checkResult)
        
        context.startActivityForResult(setupIntent, SETUP_ACTIVITY)
    }

    fun openGoogleMarket(context: Activity) {
        val uri = Uri.parse("market://details?id=${context.packageName}")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=${context.packageName}")
                )
            )
        }
    }
}