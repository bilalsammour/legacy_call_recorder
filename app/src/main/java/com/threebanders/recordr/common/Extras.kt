package com.threebanders.recordr.common

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Notification
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.threebanders.recordr.CrApp
import com.threebanders.recordr.R
import com.threebanders.recordr.test.fragments.OptimizationFragment
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import com.threebanders.recordr.ui.help.HelpActivity
import com.threebanders.recordr.ui.settings.SettingsActivity
import com.threebanders.recordr.ui.settings.SettingsFragment.Companion.GOOGLE_DRIVE
import com.threebanders.recordr.viewmodels.MainViewModel
import core.threebanders.recordr.data.Recording
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Type
import kotlin.random.Random


object Extras {
    private const val PERMISSION_REQUEST_CODE = 1001
    const val HAS_ACCEPTED_EULA = "has_accepted_eula"
    const val EULA_NOT_ACCEPTED = 1
    const val PERMS_NOT_GRANTED = 2
    const val POWER_OPTIMIZED = 4
    const val SETUP_ARGUMENT = "setup_arg"
    const val BUTTON_WIDTH = 200
    const val MARGIN_BOTTOM = 30
    const val NOTIFICATION_ID = "1"
    const val NOTIFICATION_STRING = "notification"

    private fun getPermissionsList(): Array<String> {
        return arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG
        )
    }

    fun uploadFileToGDrive(file: File, context: Context?) {
        try {
            CoroutineScope(Dispatchers.Main).launch {
                val drive = getDriveService(context)
                var folderId = ""
                withContext(Dispatchers.IO) {
                    val gFolder = com.google.api.services.drive.model.File()
                    gFolder.name = Constants.APP_NAME
                    gFolder.mimeType = "application/vnd.google-apps.folder"

                    launch {
                        val fileList = drive?.Files()?.list()
                            ?.setQ(
                                "mimeType='application/vnd.google-apps.folder' and trashed=false and name='" +
                                        Constants.APP_NAME + "'"
                            )
                            ?.execute()

                        folderId = if (fileList?.files?.isEmpty() == true) {
                            val folder = drive.Files().create(gFolder)?.setFields("id")?.execute()
                            folder?.id ?: ""
                        } else {
                            fileList?.files?.get(0)?.id ?: ""
                        }
                    }
                }.join()
                withContext(Dispatchers.IO) {
                    launch {
                        val gFile = com.google.api.services.drive.model.File()
                        gFile.name = file.name
                        gFile.parents = mutableListOf(folderId)
                        val fileContent = FileContent("audio/wav", file)
                        drive?.Files()?.create(gFile, fileContent)?.setFields("id, parents")
                            ?.execute()
                        showNotification(context)
                    }
                }.key
            }
        } catch (userAuthEx: UserRecoverableAuthIOException) {
            context!!.startActivity(
                userAuthEx.intent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDriveService(context: Context?): Drive? {
        GoogleSignIn.getLastSignedInAccount(context!!)?.let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount.account!!
            return Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(context.getString(R.string.app_name))
                .build()
        }
        return null
    }

    private fun showNotification(context: Context?) {
        val pendingIntent: PendingIntent =
            Intent(context, ContactsListActivityMain::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

        val notification: Notification =
            NotificationCompat.Builder(context!!, NOTIFICATION_ID)
                .setContentTitle(context.getString(R.string.uploading))
                .setContentText(context.getString(R.string.uploading_file_to_google_drive))
                .setSmallIcon(R.drawable.ic_baseline_circle_notifications_24)
                .setContentIntent(pendingIntent)
                .build()

        NotificationManagerCompat.from(context).notify(Random.nextInt(), notification)
    }

    fun getSharedPrefsInstance(context: Context?): SharedPreferences {
        return context!!.getSharedPreferences("audioPrefs", Context.MODE_PRIVATE)
    }

    fun getAudioPath(context: Context?): String {
        return getSharedPrefsInstance(context).getString("audioPath", "")!!
    }

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

    @Suppress("DEPRECATION")
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

    fun showAccessibilitySettings(activity: AppCompatActivity, block: (ActivityResult) -> Unit) {
        val intent = Intent("android.settings.ACCESSIBILITY_SETTINGS")
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                block(it)
            }
        }.launch(intent)
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

    fun requestAllPermissions(activity: FragmentActivity) {
        ActivityCompat.requestPermissions(
            activity,
            getPermissionsList(),
            PERMISSION_REQUEST_CODE
        )
    }


    fun checkPermissions(context: Context): Boolean {
        val phoneState = createPermission(Manifest.permission.READ_PHONE_STATE, context)
        val recordAudio = createPermission(Manifest.permission.RECORD_AUDIO, context)
        val readContacts = createPermission(Manifest.permission.READ_CONTACTS, context)
        val readCallsLog = createPermission(Manifest.permission.READ_CALL_LOG, context)

        return phoneState && recordAudio && readContacts && readCallsLog
    }

    private fun createPermission(permission: String, context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("BatteryLife")
    fun changeBatteryOptimization(activity: FragmentActivity) {
        val intent = Intent()
        val packageName: String = activity.packageName

        if (!isIgnoringBatteryOptimizations(activity)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            activity.startActivity(intent)
        }
    }

    fun isIgnoringBatteryOptimizations(activity: FragmentActivity): Boolean {
        val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager

        return pm.isIgnoringBatteryOptimizations(activity.packageName)
    }

    fun warningDialog(activity: FragmentActivity, onFinish: () -> Unit) {
        MaterialDialog.Builder(activity)
            .title(R.string.warning_title)
            .content(R.string.optimization_still_active)
            .positiveText(android.R.string.ok)
            .icon(ContextCompat.getDrawable(activity, R.drawable.warning)!!)
            .onPositive { _, _ -> onFinish.invoke() }
            .show()
    }

    fun getDataFromSharedPreferences(context: Context): List<Recording?>? {
        val gson = Gson()
        val productFromShared: List<Recording?>?
        val sharedPref: SharedPreferences? =
            context.getSharedPreferences("PREFS_TAG", Context.MODE_PRIVATE)
        val jsonPreferences = sharedPref?.getString("PRODUCT_TAG", "")
        val type: Type = object : TypeToken<List<Recording?>?>() {}.type
        productFromShared = gson.fromJson<List<Recording?>>(jsonPreferences, type)
        return productFromShared
    }

    fun setDataFromSharedPreferences(context: Context, curProduct: List<Recording?>) {
        val gson = Gson()
        val jsonCurProduct = gson.toJson(curProduct)
        val sharedPref: SharedPreferences? =
            context.getSharedPreferences("PREFS_TAG", Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.putString("PRODUCT_TAG", jsonCurProduct)
        editor?.apply()
    }

    fun isGoogleDriveSynced(): Boolean {
        val corePrefs = CrApp.instance.core.prefs

        return corePrefs.getBoolean(GOOGLE_DRIVE, false)
    }


    /* ------------------------------ PERMISSIONS ACTIVITY ------------------------------ */
    fun addCurrentFragmentPosition(context: Context, position: Int) {
        val prefs = context.getSharedPreferences("permissionPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("position", position)
        editor.apply()
    } /**/

    fun getCurrentFragmentPosition(context: Context): Int {
        val prefs = context.getSharedPreferences("permissionPrefs", Context.MODE_PRIVATE)
        return prefs.getInt("position", 0)
    } /**/

    fun clearPreferences(context: Context) {
        val prefs = context.getSharedPreferences("permissionPrefs", Context.MODE_PRIVATE)
        prefs.edit {
            clear()
        }
    } /**/

    fun isAppOptimized(pm: PowerManager, packageName: String): Boolean {
        return pm.isIgnoringBatteryOptimizations(packageName)
    } /**/

    fun openActivity(context: Activity) {
        Intent(context, ContactsListActivityMain::class.java).apply {
            context.startActivity(this)
            context.finish()
        }
    } /**/

    fun openOptimizationFragment(context: FragmentActivity) {
        context
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, OptimizationFragment())
            .commit()
    } /**/

    fun openNextFragment(context: FragmentActivity, mainViewModel: MainViewModel, position: Int) {
        context
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, mainViewModel.fragments.value!![position])
            .commit()
    }/**/

    fun doNotOptimizeApp(context: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
    }/**/

    fun showRationale(
        context: Context,
        title: String,
        message: String,
        permission: String,
        activityResultLauncher: ActivityResultLauncher<String>
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok") { _, _ ->
                activityResultLauncher.launch(permission)
            }
            .show()
    } /**/

    fun enablePermissionFromSettings(context: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }
}