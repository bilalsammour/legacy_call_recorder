package com.threebanders.recordr.ui

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.threebanders.recordr.R
import com.threebanders.recordr.common.*
import com.threebanders.recordr.ui.setup.SetupActivity
import core.threebanders.recordr.Core
import core.threebanders.recordr.data.Contact
import core.threebanders.recordr.data.Recording
import core.threebanders.recordr.data.Repository
import java.io.File

class MainViewModel : ViewModel() {
    private val repository: Repository = Core.getRepository()
    var contact = MutableLiveData<Contact?>()
    private var contactList: List<Contact> = ArrayList()
    var contacts = MutableLiveData(contactList)
    private val recordList: MutableList<Recording> = ArrayList()
    var records = MutableLiveData(recordList)

    var deletedRecording = MutableLiveData<Recording?>()

    init {
        setupAllContacts()
    }

    private fun setupAllContacts() {
        contactList = repository.allContacts
        contacts.value = contactList
    }

    fun loadRecordings() {
        repository.getRecordings(contact.value) { recordings: List<Recording>? ->
            recordList.clear()
            recordList.addAll(recordings!!)
            records.postValue(recordList)
        }
    }

    fun deleteRecordings(recordings: List<Recording?>): DialogInfo? {
        for (recording in recordings) {
            try {
                recording!!.delete(repository)
                deletedRecording.postValue(recording)
            } catch (exc: Exception) {
                return DialogInfo(
                    R.string.error_title,
                    R.string.error_deleting_recordings,
                    R.drawable.error
                )
            }
        }
        return null
    }

    fun renameRecording(input: CharSequence, recording: Recording?): DialogInfo? {
        if (Recording.hasIllegalChar(input)) return DialogInfo(
            R.string.information_title,
            R.string.rename_illegal_chars,
            R.drawable.info
        )
        val parent = File(recording!!.path).parent
        val oldFileName = File(recording.path).name
        val ext = oldFileName.substring(oldFileName.length - 3)
        val newFileName = "$input.$ext"
        if (File(parent, newFileName).exists()) return DialogInfo(
            R.string.information_title,
            R.string.rename_already_used,
            R.drawable.info
        )
        try {
            if (File(recording.path).renameTo(File(parent, newFileName))) {
                recording.path = File(parent, newFileName).absolutePath
                recording.isNameSet = true
                recording.update(repository)
            } else throw Exception("File.renameTo() has returned false.")
        } catch (e: Exception) {
            return DialogInfo(R.string.error_title, R.string.rename_error, R.drawable.error)
        }
        return null
    }

    /* ------------------- CLEAN UP CODE --------------------*/

    // TODO : Shared Prefs
    fun getPrefs(context: Context): List<Recording?>? {
        return Extras.getDataFromSharedPreferences(context)
    }

    fun setPrefs(context: Context, list: List<Recording?>) {
        Extras.setDataFromSharedPreferences(context, list)
    }

    fun checkPermissions(context: Context): Boolean {
        return Extras.checkPermissions(context)
    }


    // TODO : Show Permissions Dialog
    fun showPermissionsDialog(parentActivity: SetupActivity?, onNextScreen: () -> Unit) {
        Extras.permissionsDialog(parentActivity, onNextScreen)
    }

    // TODO : Show Warning Dialog
    fun showWarningDialog(parentActivity: SetupActivity?, onFinish: () -> Unit) {
        Extras.warningDialog(parentActivity, onFinish)
    }

    fun changeBatteryOptimization(fragmentActivity: FragmentActivity) {
        Extras.changeBatteryOptimization(fragmentActivity)
    }

    fun isIgnoringBatteryOptimizations(activity: FragmentActivity): Boolean {
        return Extras.isIgnoringBatteryOptimizations(activity)
    }

    fun showOnBackPressedDialog(context: Context, onBackPressed: () -> Unit) {
        Extras.showExitDialog(context, onBackPressed)
    }

    fun checkIfServiceIsRunning(context: Context, serviceClass: Class<*>): Boolean {
        return Extras.isMyServiceRunning(context, serviceClass)
    }

    fun showAccessibilitySettingsInApp(
        activity: AppCompatActivity,
        block: (ActivityResult) -> Unit
    ) {
        Extras.showAccessibilitySettings(activity, block)
    }

    fun openSettingsActivityInApp(context: Activity) {
        Extras.openSettingsActivity(context)
    }

    fun openHelpActivityInApp(context: Activity) {
        Extras.openHelpActivity(context)
    }

    fun openGoogleMarketInApp(context: Activity) {
        Extras.openGoogleMarket(context)
    }
}