package com.threebanders.recordr.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.ui.MainViewModel

class SetupPowerFragment : Fragment() {
    private var parentActivity: SetupActivity? = null
    private lateinit var dozeInfo: LinearLayout
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.setup_power_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        prepareUi()

        if (parentActivity!!.checkResult and Extras.POWER_OPTIMIZED != 0) {
            dozeInfo.visibility = View.VISIBLE
            val turnOffDoze = parentActivity!!.findViewById<Button>(R.id.turn_off_doze)
            turnOffDoze.setOnClickListener {
                mainViewModel.changeBatteryOptimization(requireActivity())
            }
        }

        val finish = parentActivity!!.findViewById<Button>(R.id.setup_power_finish)
        finish.setOnClickListener {
            if (!mainViewModel.isIgnoringBatteryOptimizations(requireActivity()))
                mainViewModel.showWarningDialog(parentActivity) {
                    parentActivity!!.finish()
                } else {
                parentActivity!!.finish()
            }
        }
    }

    private fun prepareUi() {
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        parentActivity = activity as SetupActivity?

        val res = resources
        val dozeInfoText = parentActivity!!.findViewById<TextView>(R.id.doze_info_text)
        dozeInfoText.text =
            String.format(res.getString(R.string.doze_info), res.getString(R.string.app_name))
        val otherOptimizations =
            parentActivity!!.findViewById<TextView>(R.id.other_power_optimizations)
        otherOptimizations.text = String.format(
            res.getString(R.string.other_power_optimizations),
            res.getString(R.string.app_name)
        )
        dozeInfo = parentActivity!!.findViewById(R.id.doze_info)
    }
}
