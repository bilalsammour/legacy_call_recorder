package com.threebanders.recordr.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.threebanders.recordr.R
import com.threebanders.recordr.ui.setup.SetupActivity

object PermissionsExtra {

    // GRANT ALL PERMISSIONS
    @SuppressLint("NewApi")
    fun requestAllPermissions(context: Context, permissionRequest: Int) {
        ActivityCompat.requestPermissions(
            context as Activity, arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_CALL_LOG
            ), permissionRequest
        )
    }

    fun permissionsDialog(parentActivity: SetupActivity?, onNextScreen: () -> Unit) {
        MaterialDialog.Builder(parentActivity!!)
            .title(R.string.warning_title)
            .content(R.string.permissions_not_granted)
            .positiveText(android.R.string.ok)
            .icon(ContextCompat.getDrawable(parentActivity, R.drawable.warning)!!)
            .onPositive { dialog, which -> onNextScreen.invoke() }
            .show()
    }


    // SHOW ACCESSIBILITY PERMISSIONS
    @SuppressLint("InlinedApi", "QueryPermissionsNeeded")
    fun accessibilityPermission(activity: FragmentActivity) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        val pm = activity.packageManager
        if (intent.resolveActivity(pm) != null) {
            activity.startActivity(intent)
        }
    }

    // SHOW WARNING MESSAGE
    fun warningDialog(parentActivity: SetupActivity?, onFinish: () -> Unit) {
        MaterialDialog.Builder(parentActivity!!)
            .title(R.string.warning_title)
            .content(R.string.optimization_still_active)
            .positiveText(android.R.string.ok)
            .icon(ContextCompat.getDrawable(parentActivity, R.drawable.warning)!!)
            .onPositive { dialog, which -> onFinish.invoke() }
            .show()
    }

    // GET PERMISSIONS LIST
    fun getPermissionsList(): Array<String> {
        return arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CALL_LOG
        )
    }
}