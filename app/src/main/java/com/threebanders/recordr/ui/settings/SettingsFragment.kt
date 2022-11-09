/*
 * Copyright (C) 2019 Eugen Rădulescu <synapticwebb@gmail.com> - All rights reserved.
 *
 * You may use, distribute and modify this code only under the conditions
 * stated in the SW Call Recorder license. You should have received a copy of the
 * SW Call Recorder license along with this file. If not, please write to <synapticwebb@gmail.com>.
 */
package com.threebanders.recordr.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.codekidlabs.storagechooser.Content
import com.codekidlabs.storagechooser.StorageChooser
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.threebanders.recordr.CrApp
import com.threebanders.recordr.R
import com.threebanders.recordr.sync.util.DriveServiceHelper
import com.threebanders.recordr.ui.BaseActivity
import java.util.*


class SettingsFragment : PreferenceFragmentCompat() {
    private var parentActivity: BaseActivity? = null
    private var preferences: SharedPreferences? = null
    private var mGoogleApiClient: GoogleSignInClient? = null

    override fun onResume() {
        super.onResume()
        val storagePath = findPreference<Preference>(STORAGE_PATH)
        val storage = findPreference<ListPreference>(STORAGE)
        storagePath?.let { storage?.let { it1 -> manageStoragePathSummary(it1, it) } }
    }

    private fun manageStoragePathSummary(storage: ListPreference, storagePath: Preference) {
        storagePath.isEnabled = true
        var path = preferences!!.getString(STORAGE_PATH, null)
        if (path == null) {
            val externalDir = requireActivity().getExternalFilesDir(null)
            if (externalDir != null) path = externalDir.absolutePath
            val editor = preferences!!.edit()
            editor.putString(STORAGE_PATH, path)
            editor.apply()
        }
        storagePath.summary = path
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        val recycler = listView
        recycler.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        parentActivity = activity as BaseActivity?
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (activity != null && requireActivity().application != null) {
            preferences = (requireActivity().application as CrApp).core.prefs
        }
        addPreferencesFromResource(R.xml.preferences)
        val themeOption = findPreference<Preference>(APP_THEME)
        val format = findPreference<Preference>(FORMAT)
        val mode = findPreference<Preference>(MODE)
        val storage = findPreference<Preference>(STORAGE)
        val storagePath = findPreference<Preference>(STORAGE_PATH)
        val source = findPreference<Preference>(SOURCE)
        val googleDrive = findPreference<Preference>(GOOGLE_DRIVE)
        googleDrive!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue == true) {
                    googleAuth()
                } else {
                    if (null != mGoogleApiClient)
                        mGoogleApiClient!!.signOut()
                }

                val preferences = (requireActivity().application as CrApp).core.prefs
                val editor = preferences.edit()
                editor.putBoolean(GOOGLE_DRIVE, newValue as Boolean)
                editor.apply()
                true
            }

        storagePath?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { preference ->
                val content = Content()
                content.overviewHeading =
                    requireContext().resources.getString(R.string.choose_recordings_storage)
                val theme = StorageChooser.Theme(activity)
                theme.scheme =
                    if (parentActivity?.settedTheme == BaseActivity.Companion.LIGHT_THEME) parentActivity!!.resources.getIntArray(
                        R.array.storage_chooser_theme_light
                    ) else parentActivity!!.resources.getIntArray(R.array.storage_chooser_theme_dark)
                val chooser = StorageChooser.Builder()
                    .withActivity(activity)
                    .withFragmentManager(parentActivity!!.fragmentManager)
                    .allowCustomPath(true)
                    .setType(StorageChooser.DIRECTORY_CHOOSER)
                    .withMemoryBar(true)
                    .allowAddFolder(true)
                    .showHidden(true)
                    .withContent(content)
                    .setTheme(theme)
                    .build()
                chooser.show()
                chooser.setOnSelectListener(StorageChooser.OnSelectListener { path ->
                    if (activity == null) {
                        return@OnSelectListener
                    }
                    val preferences =
                        (requireActivity().application as CrApp).core.prefs
                    val editor = preferences.edit()
                    editor.putString(STORAGE_PATH, path)
                    editor.apply()
                    preference.summary = path
                })
                true
            }


        storage?.summaryProvider =
            Preference.SummaryProvider { preference: Preference -> (preference as ListPreference).entry }
        themeOption?.summaryProvider =
            Preference.SummaryProvider<ListPreference> { preference -> preference.entry }
        themeOption?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                requireActivity().recreate()
                true
            }
        source?.summaryProvider =
            Preference.SummaryProvider<ListPreference> { preference -> preference.entry }
        format?.summaryProvider =
            Preference.SummaryProvider<ListPreference> { preference -> preference.entry }
        mode?.summaryProvider =
            Preference.SummaryProvider<ListPreference> { preference -> preference.entry }
    }


    private fun googleAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(
                requireContext().getString(R.string.web_client_id)
            )
            .requestEmail().build()
        mGoogleApiClient = GoogleSignIn.getClient(requireContext(), gso)
        startActivity(mGoogleApiClient!!.signInIntent)

    }


    companion object {
        //aceste valori vor fi dublate în res/xml/preferences.xml
        const val APP_THEME = "theme"
        const val STORAGE = "storage"
        const val STORAGE_PATH = "public_storage_path"
        const val FORMAT = "format"
        const val MODE = "mode"
        const val SOURCE = "source"
        const val GOOGLE_DRIVE = "put_on_drive"
    }
}
