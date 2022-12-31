package com.threebanders.recordr.ui.welcome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.viewmodels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WelcomeActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
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