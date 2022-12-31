package com.threebanders.recordr.permission.fragments

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.viewmodels.MainViewModel

class ReadCallLogFragment : Fragment() {
    private var counter = 0
    private lateinit var rootView: View
    private lateinit var permissionText: TextView
    private lateinit var allowBtn: Button
    private lateinit var nextBtn: Button
    private lateinit var permissionTypeTxt: TextView
    private lateinit var mainViewModel: MainViewModel
    private lateinit var pm: PowerManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(
            R.layout.permission_fragment_layout, container,
            false
        )

        permissionText = rootView.findViewById(R.id.permissionText)
        allowBtn = rootView.findViewById(R.id.allowBtn)
        nextBtn = rootView.findViewById(R.id.nextBtn)
        permissionTypeTxt = rootView.findViewById(R.id.permissionType)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionTypeTxt.text = getString(R.string.read_call_log_permission)
        pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        allowBtn.setOnClickListener {
            activityResultLauncher.launch(Manifest.permission.READ_CALL_LOG)
        }
        nextBtn.setOnClickListener {
            if (mainViewModel.fragments.value!!.size == mainViewModel.getCurrentFragmentPosition(
                    requireContext()
                ) + 1
            ) {
                if (mainViewModel.isAppOptimized(pm, requireContext().packageName)) {
                    mainViewModel.openActivity(requireActivity())
                } else {
                    mainViewModel.openOptimizationFragment(requireActivity())
                }
            } else {
                mainViewModel.openNextFragment(
                    requireActivity(),
                    mainViewModel,
                    mainViewModel.getCurrentFragmentPosition(requireContext()) + 1
                )
                mainViewModel.addCurrentFragmentPosition(
                    requireContext(),
                    mainViewModel.getCurrentFragmentPosition(requireContext()) + 1
                )
            }
        }
    }




    private val activityResultLauncher: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            allowBtn.visibility = View.GONE
            nextBtn.visibility = View.VISIBLE
        } else {
            counter++
            if (counter >= 2) {
                mainViewModel.enablePermissionFromSettings(requireActivity())
            } else {
                showDial()
            }
        }
    }

    private fun showDial() {
        mainViewModel.showRationale(
            requireContext(),
            getString(R.string.read_call_log_permission),
            getString(R.string.call_log_rationale),
            Manifest.permission.READ_CALL_LOG,
            activityResultLauncher
        )
    }
}