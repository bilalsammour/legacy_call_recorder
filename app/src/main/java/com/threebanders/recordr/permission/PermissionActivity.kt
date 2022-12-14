package com.threebanders.recordr.permission

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.permission.fragments.*
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import com.threebanders.recordr.viewmodels.MainViewModel
import core.threebanders.recordr.MyService
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

        if (mainViewModel.checkIfPermissionsGranted(this)) {
            if (mainViewModel.isAppOptimized(pm, packageName)) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, AccessibilityFragment()).commit()
            }
            else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, OptimizationFragment()).commit()
            }
        }
        else {
            mainViewModel.clearPreferences(this)
            mainViewModel.addFragment(this,fragmentsList){
                mainViewModel.saveCurrentFragments(it)
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(3000)

                mainViewModel.addCurrentFragmentPosition(this@PermissionActivity, 0)
                supportFragmentManager.beginTransaction().replace(R.id.container, fragmentsList[0])
                    .commit()
            }
        }
    }

}