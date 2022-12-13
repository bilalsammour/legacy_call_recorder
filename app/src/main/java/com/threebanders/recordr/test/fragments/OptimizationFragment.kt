package com.threebanders.recordr.test.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import com.threebanders.recordr.viewmodels.MainViewModel

class OptimizationFragment  : Fragment() {

    private lateinit var rootView : View
    private lateinit var turnOffBtn : Button
    private lateinit var pm : PowerManager
    private lateinit var mainViewModel : MainViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       rootView = inflater.inflate(R.layout.optimization_fragment_layout,container,false)
       turnOffBtn = rootView.findViewById(R.id.turnOffBtn)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
       return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        turnOffBtn.setOnClickListener {
            if(turnOffBtn.text.toString() == "Turn Off"){
                if(!mainViewModel.isAppOptimized(pm, requireContext().packageName)){
                    Extras.doNotOptimizeApp(requireActivity())
                    turnOffBtn.text = "Finish"
                }
                else {
                    mainViewModel.openActivity(requireActivity())
                }
            } else if(turnOffBtn.text == "Finish"){
                if(mainViewModel.isAppOptimized(pm, requireContext().packageName)){
                    mainViewModel.openActivity(requireActivity())
                }
                else {
                    Toast.makeText(requireContext(),"Please do no optimize this app ..",Toast.LENGTH_LONG).show()
                    turnOffBtn.text = "Turn Off"
                }
            }
        }
    }


}