package com.threebanders.recordr.ui.setup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.ui.MainViewModel

class SetupPermissionsFragment : Fragment() {
    private var arePermissionsGranted = false
    private lateinit var mainViewModel: MainViewModel
    private lateinit var parentActivity: SetupActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.setup_permissions_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val res = resources
        parentActivity = activity as SetupActivity
        val permsIntro = parentActivity.findViewById<TextView>(R.id.perms_intro)
        permsIntro.text =
            String.format(res.getString(R.string.perms_intro), res.getString(R.string.app_name))
        val nextButton = parentActivity.findViewById<Button>(R.id.setup_perms_next)

        nextButton.setOnClickListener {
            if (arePermissionsGranted) {
                permissionsNext()
            } else {
                requestPermission()
            }
        }
    }

    private var requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.READ_PHONE_STATE] == true && permissions[Manifest.permission.RECORD_AUDIO] == true &&
                permissions[Manifest.permission.READ_CONTACTS] == true && permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true &&
                permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true && permissions[Manifest.permission.READ_CALL_LOG] == true
            ) {
                arePermissionsGranted = true
            }
        }

    private fun requestPermission() {
        if (!(checkPermission(Extras.getPermissionsList()[0]) && checkPermission(Extras.getPermissionsList()[1])
                    && checkPermission(Extras.getPermissionsList()[2]) && checkPermission(Extras.getPermissionsList()[3])
                    && checkPermission(Extras.getPermissionsList()[4]) && checkPermission(Extras.getPermissionsList()[5]))
        ) {
            requestMultiplePermissions.launch(Extras.getPermissionsList())
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun permissionsNext() {
        val checkResult = parentActivity.checkResult

        if (((checkResult != 0) and (Extras.EULA_NOT_ACCEPTED != 0)) || ((checkResult != 0) and (Extras.POWER_OPTIMIZED != 0))
        ) {
            val powerFragment = SetupPowerFragment()
            parentActivity.supportFragmentManager.beginTransaction()
                .replace(R.id.setup_fragment_container, powerFragment)
                .commitAllowingStateLoss()
        } else {
            parentActivity.finish()
        }
    }
}
