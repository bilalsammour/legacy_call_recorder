package com.threebanders.recordr.ui.welcome

import android.os.Bundle
import android.os.PowerManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {
    private lateinit var powerManager: PowerManager
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        powerManager = getSystemService(POWER_SERVICE) as PowerManager

        CoroutineScope(Dispatchers.Main).launch {
            verifyConditions()
        }
    }

    private fun verifyConditions() {
        if (mainViewModel.ready(this)) {
            mainViewModel.openContactListScreen(this)
        } else {
            mainViewModel.openPermissionScreen(this)
        }
    }


}