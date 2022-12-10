package com.threebanders.recordr.viewmodels

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.threebanders.recordr.BuildConfig
import com.threebanders.recordr.R
import com.threebanders.recordr.common.DialogInfo
import com.threebanders.recordr.common.Extras
import core.threebanders.recordr.Core
import core.threebanders.recordr.data.Contact
import core.threebanders.recordr.data.Recording
import core.threebanders.recordr.data.Repository

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

    fun getVersion(): String = BuildConfig.VERSION_NAME

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

    fun getPrefs(context: Context): List<Recording?>? {
        return Extras.getDataFromSharedPreferences(context)
    }

    fun setPrefs(context: Context, list: List<Recording?>) {
        Extras.setDataFromSharedPreferences(context, list)
    }

    fun requestAllPermissions(activity: FragmentActivity) {
        Extras.requestAllPermissions(activity)
    }

    fun checkPermissions(context: Context): Boolean {
        return Extras.checkPermissions(context)
    }

    fun showWarningDialog(activity: FragmentActivity, onFinish: () -> Unit) {
        Extras.warningDialog(activity, onFinish)
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