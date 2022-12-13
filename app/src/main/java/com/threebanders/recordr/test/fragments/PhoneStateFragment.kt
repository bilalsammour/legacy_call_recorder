package com.threebanders.recordr.test.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.provider.Contacts.Phones
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.test.PermissionActivity
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import com.threebanders.recordr.viewmodels.MainViewModel

class PhoneStateFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var rootView : View
    private lateinit var permissionText : TextView
    private lateinit var allowNextBtn : Button
    private lateinit var pm : PowerManager
    private lateinit var permissionTypeTxt : TextView
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

        pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        permissionTypeTxt.text = "Phone State Permission"
        allowNextBtn.setOnClickListener {
            if(allowNextBtn.text.toString() == "Allow"){
                activityResultLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            } else if (allowNextBtn.text.toString() == "Next"){
                if(mainViewModel.fragments.value!!.size == mainViewModel.getCurrentFragmentPosition(requireContext())){
                    if(mainViewModel.isAppOptimized(pm , requireContext().packageName)){
                        mainViewModel.openActivity(requireActivity())
                    } else {
                        mainViewModel.openOptimizationFragment(requireActivity())
                    }
                } else {
                    Extras.openNextFragment(requireActivity(),mainViewModel,mainViewModel.getCurrentFragmentPosition(requireContext()) + 1)
                    mainViewModel.addCurrentFragmentPosition(requireContext(),mainViewModel.getCurrentFragmentPosition(requireContext()) + 1)
                }
            }
        }
    }


    private var activityResultLauncher : ActivityResultLauncher<String>  = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted){
            allowNextBtn.text = "Next"
        } else {
            showDial()
        }
    }

    private fun showDial(){
        Extras.showRationale(
            requireContext(),
            "Phone State Permission",
            "xxxxxxx",
             Manifest.permission.READ_PHONE_STATE,
             activityResultLauncher)
    }
}