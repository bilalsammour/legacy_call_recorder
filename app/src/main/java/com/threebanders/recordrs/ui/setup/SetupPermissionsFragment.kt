/*
 * Copyright (C) 2019 Eugen Rădulescu <synapticwebb@gmail.com> - All rights reserved.
 *
 * You may use, distribute and modify this code only under the conditions
 * stated in the SW Call Recorder license. You should have received a copy of the
 * SW Call Recorder license along with this file. If not, please write to <synapticwebb@gmail.com>.
 */
package com.threebanders.recordrs.ui.setup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.threebanders.recordrs.R
import com.threebanders.recordrs.ui.contact.ContactsListActivityMain

class SetupPermissionsFragment : Fragment() {
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
        val res = resources
        parentActivity = activity as SetupActivity?
        val permsIntro = parentActivity!!.findViewById<TextView>(R.id.perms_intro)
        permsIntro.text =
            String.format(res.getString(R.string.perms_intro), res.getString(R.string.app_name))
        val nextButton = parentActivity!!.findViewById<Button>(R.id.setup_perms_next)
        //în Android 6 dacă o singură permisiune este revocată le cere din nou pe toate. Nu pare suficient de sever pentru reparație.
        nextButton.setOnClickListener {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_CALL_LOG
                ), PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        var notGranted = false
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.size == 0) notGranted = true else {
                for (result in grantResults) if (result != PackageManager.PERMISSION_GRANTED) {
                    notGranted = true
                    break
                }
            }
            if (notGranted) {
                MaterialDialog.Builder(parentActivity!!)
                    .title(R.string.warning_title)
                    .content(R.string.permissions_not_granted)
                    .positiveText(android.R.string.ok)
                    .icon(resources.getDrawable(R.drawable.warning))
                    .onPositive { dialog, which -> permissionsNext() }
                    .show()
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