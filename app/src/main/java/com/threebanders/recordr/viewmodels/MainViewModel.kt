package com.threebanders.recordr.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
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
    private var contacts = MutableLiveData(contactList)
    var fragments = MutableLiveData<MutableList<Fragment>>()
    private val recordList: MutableList<Recording> = ArrayList()
    var records = MutableLiveData(recordList)


    /* ------------------------------------------- */
    fun saveCurrentFragments(list: MutableList<Fragment>) {
        fragments.value = list
    }

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

    fun ready(activity: FragmentActivity): Boolean {
        return Extras.ready(activity)
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

    fun openSettingsActivityInApp(context: Activity) {
        Extras.openSettingsActivity(context)
    }

    fun openHelpActivityInApp(context: Activity) {
        Extras.openHelpActivity(context)
    }

    fun openGoogleMarketInApp(context: Activity) {
        Extras.openGoogleMarket(context)
    }

    /* -------------------- PERMISSIONS  *------------------ */
    fun addCurrentFragmentPosition(context: Context, position: Int) {
        Extras.addCurrentFragmentPosition(context, position)
    }

    fun getCurrentFragmentPosition(context: Context): Int {
        return Extras.getCurrentFragmentPosition(context)
    }

    fun clearPreferences(context: Context) {
        Extras.clearPreferences(context)
    }

    fun isAppOptimized(pm: PowerManager, packageName: String): Boolean {
        return Extras.isAppOptimized(pm, packageName)
    }

    fun openActivity(context: Activity) {
        return Extras.openActivity(context)
    }

    fun openOptimizationFragment(context: FragmentActivity) {
        Extras.openOptimizationFragment(context)
    }

    fun openNextFragment(context: FragmentActivity, mainViewModel: MainViewModel, position: Int) {
        Extras.openNextFragment(context, mainViewModel, position)
    }

    fun doNotOptimizeApp(context: Activity) {
        Extras.doNotOptimizeApp(context)
    }

    fun showRationale(
        context: Context,
        title: String,
        message: String,
        permission: String,
        activityResultLauncher: ActivityResultLauncher<String>
    ) {
        Extras.showRationale(context, title, message, permission, activityResultLauncher)
    }

    fun enablePermissionFromSettings(context: Activity) {
        Extras.enablePermissionFromSettings(context)
    }

    fun addFragment(
        context: Context,
        fragmentsList: MutableList<Fragment>,
        onFinish: (MutableList<Fragment>) -> Unit
    ) {
        Extras.addFragment(context, fragmentsList, onFinish)
    }

    fun checkIfPermissionsGranted(context: Context): Boolean {
        return Extras.checkIfPermissionsGranted(context)
    }

    fun moveToAccessibilityFragment(context: FragmentActivity) {
        Extras.moveToAccessibilityFragment(context)
    }

    fun openPermissionScreen(context: Activity) {
        Extras.openPermissionScreen(context)
    }

    fun openContactListScreen(context: Activity) {
        Extras.openContactListScreen(context)
    }

    fun openAccessibilitySettings(activityResultLauncher: ActivityResultLauncher<Intent>) {
        Extras.openAccessibilitySettings(activityResultLauncher)
    }

    fun launch(context: FragmentActivity,onGranted : (Boolean) -> Unit) : ActivityResultLauncher<String>{
        return Extras.launch(context,onGranted)
    }
}