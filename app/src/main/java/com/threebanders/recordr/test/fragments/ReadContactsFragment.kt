package com.threebanders.recordr.test.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.test.PermissionActivity
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import com.threebanders.recordr.viewmodels.MainViewModel

class ReadContactsFragment  : Fragment(){

    private lateinit var rootView : View
    private lateinit var permissionText : TextView
    private lateinit var allowNextBtn : Button
    private lateinit var permissionTypeTxt : TextView
    private lateinit var mainViewModel: MainViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.permission_fragment_layout,container,false)
        permissionText = rootView.findViewById(R.id.permissionText)
        allowNextBtn = rootView.findViewById(R.id.allowNextBtn)
        permissionTypeTxt = rootView.findViewById(R.id.permissionType)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionTypeTxt.text = "Read Contacts Permission"
        allowNextBtn.setOnClickListener {
            if(allowNextBtn.text.toString() == "Allow"){
                activityResultLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
            else if (allowNextBtn.text.toString() == "Next"){

                if(mainViewModel.fragments.value!!.size == Extras.getCurrentFragmentPosition(requireContext())){
                    Intent(requireContext(), ContactsListActivityMain::class.java).apply {
                        startActivity(this)
                        requireActivity().finish()
                    }
                } else {
                    requireActivity()
                        .supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container,mainViewModel.fragments.value!![Extras.getCurrentFragmentPosition(requireContext()) + 1])
                        .commit()

                    Extras.addCurrentFragmentPosition(requireContext(),Extras.getCurrentFragmentPosition(requireContext()) + 1)
                }
            }
        }
    }

    private var activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted){
            allowNextBtn.text = "Next"
        } else {
           showRationale()
        }
    }

    private fun showRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Read Contacts Permission")
            .setMessage("Permission is to be granted cause it is needed")
            .setPositiveButton("Ok") { _, _ ->
                activityResultLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
            .show()
    }
}