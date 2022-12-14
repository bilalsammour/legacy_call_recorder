package com.threebanders.recordr.permission

import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.permission.fragments.AccessibilityFragment
import com.threebanders.recordr.permission.fragments.OptimizationFragment
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
                delay(1500)
                mainViewModel.addCurrentFragmentPosition(this@PermissionActivity, 0)
                supportFragmentManager.beginTransaction().replace(R.id.container, fragmentsList[0])
                    .commit()
            }
        }
    }

}