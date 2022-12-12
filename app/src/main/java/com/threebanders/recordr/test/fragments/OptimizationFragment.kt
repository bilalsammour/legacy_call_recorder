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
                    val intent = Intent()
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:${requireContext().packageName}")
                    startActivity(intent)
                    turnOffBtn.text = "Finish"
                }
                else {
                    Intent(requireContext(), ContactsListActivityMain::class.java).apply {
                        startActivity(this)
                        requireActivity().finish()
                    }
                }
            } else if(turnOffBtn.text == "Finish"){
                if(Extras.isAppOptimized(pm, requireContext().packageName)){
                    Intent(requireContext(), ContactsListActivityMain::class.java).apply {
                        startActivity(this)
                        requireActivity().finish()
                    }
                }
            }
        }
    }


}