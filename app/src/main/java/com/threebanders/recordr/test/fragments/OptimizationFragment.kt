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
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.ui.contact.ContactsListActivityMain

class OptimizationFragment  : Fragment() {

    private lateinit var rootView : View
    private lateinit var turnOffBtn : Button
    private lateinit var pm : PowerManager
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       rootView = inflater.inflate(R.layout.optimization_fragment_layout,container,false)
       turnOffBtn = rootView.findViewById(R.id.turnOffBtn)
       return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        turnOffBtn.setOnClickListener {
            if(turnOffBtn.text.toString() == "Turn Off"){
                if(!Extras.isAppOptimized(pm, requireContext().packageName)){
                    Extras.doNotOptimizeApp(requireActivity())
                    turnOffBtn.text = "Finish"
                }
                else {
                    Extras.openActivity(requireActivity())
                }
            } else if(turnOffBtn.text == "Finish"){
                if(Extras.isAppOptimized(pm, requireContext().packageName)){
                    Extras.openActivity(requireActivity())
                }
                else {
                    Toast.makeText(requireContext(),"Please do no optimize this app ..",Toast.LENGTH_LONG).show()
                    turnOffBtn.text = "Turn Off"
                }
            }
        }
    }


}