package com.threebanders.recordr.ui.setup

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.common.PermissionsExtra
import com.threebanders.recordr.ui.MainViewModel
import com.threebanders.recordr.ui.contact.ContactsListActivityMain

class SetupPermissionsFragment : Fragment() {
    private lateinit var mainViewModel: MainViewModel
    private var parentActivity: SetupActivity? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.setup_permissions_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val res = resources
        parentActivity = activity as SetupActivity?
        val permsIntro = parentActivity!!.findViewById<TextView>(R.id.perms_intro)
        permsIntro.text =
            String.format(res.getString(R.string.perms_intro), res.getString(R.string.app_name))
        val nextButton = parentActivity!!.findViewById<Button>(R.id.setup_perms_next)

        nextButton.setOnClickListener {
            requestPermissions(PermissionsExtra.getPermissionsList(), PERMISSION_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        var notGranted = false
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.isEmpty()) notGranted = true else {
                for (result in grantResults) if (result != PackageManager.PERMISSION_GRANTED) {
                    notGranted = true
                    break
                }
            }
            if (notGranted) {
                mainViewModel.showPermissionsDialog(parentActivity) {
                    permissionsNext()
                }
            } else permissionsNext()
        }
    }

    private fun permissionsNext() {
        val checkResult = parentActivity?.checkResult
        //după permisiuni afișăm powersetup dacă suntem la prima rulare sau aplicația este optimizată.
        if (((checkResult!! != 0) and (ContactsListActivityMain.EULA_NOT_ACCEPTED != 0)) || ((checkResult != 0) and (ContactsListActivityMain.POWER_OPTIMIZED != 0))
        ) {
            val powerFragment = SetupPowerFragment()
            parentActivity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.setup_fragment_container, powerFragment)
                .commitAllowingStateLoss()
        } else {
            parentActivity!!.finish()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST = 0
    }
}