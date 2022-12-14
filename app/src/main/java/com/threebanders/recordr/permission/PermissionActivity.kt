package com.threebanders.recordr.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.permission.fragments.*
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import com.threebanders.recordr.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PermissionActivity : AppCompatActivity() {
    private lateinit var pm: PowerManager
    private lateinit var mainViewModel: MainViewModel
    private var fragmentsList = mutableListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)
    }

    override fun onStart() {
        super.onStart()

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        pm = getSystemService(Context.POWER_SERVICE) as PowerManager

        if (checkIfPermissionsGranted()) {
            if (mainViewModel.isAppOptimized(pm, packageName)) {
                Intent(this, ContactsListActivityMain::class.java).apply {
                    startActivity(this)
                    finish()
                }
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, OptimizationFragment()).commit()
            }
        } else {
            mainViewModel.clearPreferences(this)

            addFragment()

            CoroutineScope(Dispatchers.Main).launch {
                delay(3000)

                mainViewModel.addCurrentFragmentPosition(this@PermissionActivity, 0)
                supportFragmentManager.beginTransaction().replace(R.id.container, fragmentsList[0])
                    .commit()
            }
        }
    }

    private fun addFragment() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fragmentsList.add(PhoneStateFragment())
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fragmentsList.add(RecordAudioFragment())
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fragmentsList.add(ReadContactsFragment())
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fragmentsList.add(ReadCallLogFragment())
        }

        mainViewModel.saveCurrentFragments(fragmentsList)
    }

    private fun checkIfPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}