package com.threebanders.recordr.test

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Contacts.Phones
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.test.fragments.PhoneStateFragment
import com.threebanders.recordr.test.fragments.ReadCallLogFragment
import com.threebanders.recordr.test.fragments.ReadContactsFragment
import com.threebanders.recordr.test.fragments.RecordAudioFragment
import com.threebanders.recordr.viewmodels.MainViewModel

class PermissionActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel
    private var fragmentsList  = mutableListOf<Fragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        Handler().postDelayed({
            Extras.addCurrentFragmentPosition(this,0)
            supportFragmentManager.beginTransaction().replace(R.id.container, fragmentsList[0]).commit()
        },3000)

    }

    override fun onStart() {
        super.onStart()
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        Extras.clearPreferences(this)
        addFragment()
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


}