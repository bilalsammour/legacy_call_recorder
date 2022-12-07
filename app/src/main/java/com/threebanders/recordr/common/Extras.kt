package com.threebanders.recordr.common

import android.Manifest
import android.animation.Animator
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
import android.graphics.Color
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.threebanders.recordr.R
import com.threebanders.recordr.ui.BaseActivity
import com.threebanders.recordr.ui.MainViewModel
import com.threebanders.recordr.ui.contact.ContactDetailFragment
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import com.threebanders.recordr.ui.help.HelpActivity
import com.threebanders.recordr.ui.settings.SettingsActivity
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


    /*-----------*/
    const val SELECT_MODE_KEY = "select_mode_key"
    const val SELECTED_ITEMS_KEY = "selected_items_key"
    const val EFFECT_TIME = 250
    const val RECORDING_EXTRA = "recording_extra"

    const val NOTIFICATION_ID = "1"
    const val NOTIFICATION_STRING = "notification"

    fun showRecyclerView(
        recordingsRecycler: RecyclerView?,
        activity: BaseActivity?,
        recordingAdapter: ContactDetailFragment.RecordingAdapter
    ) {
        recordingsRecycler!!.layoutManager = LinearLayoutManager(activity)
        recordingsRecycler.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
        recordingsRecycler.adapter = recordingAdapter
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
                            ?.setQ("mimeType='application/vnd.google-apps.folder' and trashed=false and name='" + Constants.APP_NAME + "'")
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

    /* ----------------------- CONTACTS EXTRAS --------------------*/

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

    private fun getPermissionsList(): Array<String> {
        return arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG
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


    /* ---------------------------- ContactDetailFragment ------------------------ */
     fun initRecycler(adapter : ContactDetailFragment.RecordingAdapter, recordingsRecycler : RecyclerView, mainActivity : ContactsListActivityMain){
        recordingsRecycler.layoutManager = LinearLayoutManager(mainActivity)
        recordingsRecycler.addItemDecoration(DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL))
        recordingsRecycler.adapter = adapter
    }
     fun showDialog(mainActivity : ContactsListActivityMain,selectedItems : Int, onAction : () -> Unit){
        MaterialDialog.Builder(mainActivity)
            .title(R.string.delete_recording_confirm_title)
            .content(String.format(mainActivity.resources.getString(
                R.string.delete_recording_confirm_message
            ), selectedItems))
            .positiveText(android.R.string.ok)
            .negativeText(android.R.string.cancel)
            .icon(ResourcesCompat.getDrawable(mainActivity.resources, R.drawable.warning, null)!!)
            .onPositive { _: MaterialDialog, _: DialogAction ->
                onAction.invoke()
            }
            .show()
    }
     fun showSecondaryDialog(mainActivity : ContactsListActivityMain,result : DialogInfo){
        MaterialDialog.Builder(mainActivity)
            .title(result.title)
            .content(result.message)
            .icon(ResourcesCompat.getDrawable(mainActivity.resources, result.icon, null)!!)
            .positiveText(android.R.string.ok)
            .show()
    }
     fun fadeEffect(view: View, finalAlpha: Float, finalVisibility: Int,EFFECT_TIME: Int) {
        view.animate()
            .alpha(finalAlpha)
            .setDuration(EFFECT_TIME.toLong())
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    view.visibility = finalVisibility
                }

                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
    }
     fun shareRecorder(path : String,context: Context?){
        try {
            val f = File(path)
            val uri = context?.let {
                FileProvider.getUriForFile(it, "com.threebanders.recordr.CrApp.provider", f)
            }
            val share = Intent(Intent.ACTION_SEND)
            share.putExtra(Intent.EXTRA_STREAM, uri)
            share.type = "audio/*"
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context!!.startActivity(Intent.createChooser(share, "Share audio File"))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
     fun modifyMargins(recording: View,context: Context,selectMode : Boolean) {
        val checkBox = recording.findViewById<CheckBox>(R.id.recording_checkbox)
        val res = context.resources
        checkBox.visibility = if (selectMode) View.VISIBLE else View.GONE
        val lpCheckBox = checkBox.layoutParams as RelativeLayout.LayoutParams
        lpCheckBox.marginStart =
            if (selectMode) res.getDimension(R.dimen.recording_checkbox_visible_start_margin)
                .toInt() else res.getDimension(R.dimen.recording_checkbox_gone_start_margin).toInt()
        checkBox.layoutParams = lpCheckBox
        val recordingAdorn = recording.findViewById<ImageView>(R.id.recording_adorn)
        val lpRecAdorn = recordingAdorn.layoutParams as RelativeLayout.LayoutParams
        lpRecAdorn.marginStart =
            if (selectMode) res.getDimension(R.dimen.recording_adorn_selected_margin_start)
                .toInt() else res.getDimension(R.dimen.recording_adorn_unselected_margin_start)
                .toInt()
        recordingAdorn.layoutParams = lpRecAdorn
        val title = recording.findViewById<TextView>(R.id.recording_title)
        val lpTitle = title.layoutParams as RelativeLayout.LayoutParams
        lpTitle.marginStart =
            if (selectMode) res.getDimension(R.dimen.recording_title_selected_margin_start)
                .toInt() else res.getDimension(R.dimen.recording_title_unselected_margin_start)
                .toInt()
        title.layoutParams = lpTitle
    }
     fun selectRecording(recording: View) {
        val checkBox = recording.findViewById<CheckBox>(R.id.recording_checkbox)
        checkBox.isChecked = true
    }
     fun deselectRecording(recording: View) {
        val checkBox = recording.findViewById<CheckBox>(R.id.recording_checkbox)
        checkBox.isChecked = false
    }
     fun redrawRecordings(adapter: ContactDetailFragment.RecordingAdapter) {
        for (i in 0 until adapter.itemCount) adapter.notifyItemChanged(i)
    }
     fun markNonexistent(holder: ContactDetailFragment.RecordingHolder,mainActivity: ContactsListActivityMain) {
        holder.exclamation.visibility = View.VISIBLE
        val filter =
            if (mainActivity.settledTheme == BaseActivity.LIGHT_THEME) Color.argb(
                255,
                0,
                0,
                0
            ) else Color.argb(255, 255, 255, 255)
        holder.recordingAdorn.setColorFilter(filter)
        holder.recordingType.setColorFilter(filter)
        holder.recordingAdorn.imageAlpha = 100
        holder.recordingType.imageAlpha = 100
        holder.title.alpha = 0.5f
    }
     fun unMarkNonexistent(holder: ContactDetailFragment.RecordingHolder) {
        holder.exclamation.visibility = View.GONE
        holder.recordingAdorn.colorFilter = null
        holder.recordingType.colorFilter = null
        holder.recordingType.imageAlpha = 255
        holder.recordingAdorn.imageAlpha = 255
        holder.title.alpha = 1f
    }
}