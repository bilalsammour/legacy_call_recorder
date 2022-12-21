package com.threebanders.recordr.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.permission.PermissionActivity
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import com.threebanders.recordr.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var powerManager: PowerManager
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        powerManager = getSystemService(POWER_SERVICE) as PowerManager

        CoroutineScope(Dispatchers.Main).launch {
            verifyConditions()
        }
    }

    private fun verifyConditions() {
        if (mainViewModel.ready(this)) {
            openContactListScreen()
        } else {
            openPermissionScreen()
        }
    }

    private fun openPermissionScreen() {
        Intent(this, PermissionActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }

    private fun openContactListScreen() {
        Intent(this, ContactsListActivityMain::class.java).apply {
            startActivity(this)
            finish()
        }
    }
}