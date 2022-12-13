package com.threebanders.recordr.test

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.provider.Contacts.Phones
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.test.fragments.*
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import com.threebanders.recordr.viewmodels.MainViewModel

class PermissionActivity : AppCompatActivity() {
    private lateinit var pm : PowerManager
    private lateinit var mainViewModel: MainViewModel
    private var fragmentsList  = mutableListOf<Fragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)


    }

    override fun onStart() {
        super.onStart()
        pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if(checkIfPermissionsGranted()){
            if(mainViewModel.isAppOptimized(pm , packageName)){
                Intent(this,ContactsListActivityMain::class.java).apply {
                    startActivity(this)
                    finish()
                }
            } else {
                supportFragmentManager.beginTransaction().replace(R.id.container, OptimizationFragment()).commit()
            }
        } else {
            mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
            mainViewModel.clearPreferences(this)
            addFragment()

            Handler().postDelayed({
                mainViewModel.addCurrentFragmentPosition(this,0)
                supportFragmentManager.beginTransaction().replace(R.id.container, fragmentsList[0]).commit()
            },3000)
        }
    }

    private fun addFragment() {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            fragmentsList.add(PhoneStateFragment())
        }
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            fragmentsList.add(RecordAudioFragment())
        }
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            fragmentsList.add(ReadContactsFragment())
        }
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
            fragmentsList.add(ReadCallLogFragment())
        }

        mainViewModel.saveCurrentFragments(fragmentsList)
    }
    private fun checkIfPermissionsGranted() : Boolean {
        return ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }


}