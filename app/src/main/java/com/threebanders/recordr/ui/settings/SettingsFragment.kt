package com.threebanders.recordr.ui.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.threebanders.recordr.CrApp
import com.threebanders.recordr.R
import com.threebanders.recordr.ui.BaseActivity

class SettingsFragment : PreferenceFragmentCompat() {
    private var parentActivity: BaseActivity? = null
    private var preferences: SharedPreferences? = null
    private var mGoogleApiClient: GoogleSignInClient? = null

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                handleSignData(it.data)
            }
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
        val source = findPreference<Preference>(SOURCE)
        val googleDrive = findPreference<Preference>(GOOGLE_DRIVE)

        googleDrive!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue == true) {
                    googleAuth()
                } else {
                    signOut()
                }

                true
            }

        themeOption?.summaryProvider =
            Preference.SummaryProvider<ListPreference> { preference -> preference.entry }
        themeOption?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, _ ->
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
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(
                requireContext().getString(R.string.google_id)
            )
            .requestEmail()
            .requestProfile()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        mGoogleApiClient = GoogleSignIn.getClient(requireContext(), gso)

        launcher.launch(mGoogleApiClient!!.signInIntent)
    }

    private fun updateGoogleDriveToggle(newValue: Boolean) {
        val preferences = (requireActivity().application as CrApp).core.prefs
        val editor = preferences.edit()
        editor.putBoolean(GOOGLE_DRIVE, newValue)
        editor.apply()
    }

    private fun handleSignData(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnCompleteListener {
                updateGoogleDriveToggle(it.isSuccessful)

                if (it.isSuccessful) {
                    updateGoogleDriveToggle(true)
                }
            }
    }

    private fun signOut() {
        mGoogleApiClient?.signOut()?.addOnCompleteListener(
            requireActivity()
        ) { Toast.makeText(requireContext(), R.string.signed_out, Toast.LENGTH_SHORT).show() }
    }

    companion object {
        const val APP_THEME = "theme"
        const val FORMAT = "format"
        const val MODE = "mode"
        const val SOURCE = "source"
        const val GOOGLE_DRIVE = "put_on_drive"
    }
}
