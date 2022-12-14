package com.threebanders.recordr.permission.fragments

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.viewmodels.MainViewModel

class PhoneStateFragment : Fragment() {
    private var counter = 0
    private lateinit var mainViewModel: MainViewModel
    private lateinit var rootView: View
    private lateinit var permissionText: TextView
    private lateinit var allowNextBtn: Button
    private lateinit var pm: PowerManager
    private lateinit var permissionTypeTxt: TextView

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
        allowNextBtn = rootView.findViewById(R.id.allowNextBtn)
        permissionTypeTxt = rootView.findViewById(R.id.permissionType)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        permissionTypeTxt.text = getString(R.string.phone_state_permission)
        allowNextBtn.setOnClickListener {
            if (allowNextBtn.text.toString() == getString(R.string.allow_button)) {
                activityResultLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            } else if (allowNextBtn.text.toString() == getString(R.string.next_button)) {
                if (mainViewModel.fragments.value!!.size == mainViewModel.getCurrentFragmentPosition(
                        requireContext()
                    )
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
    }


    private var activityResultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                allowNextBtn.text = getString(R.string.next_button)
                allowNextBtn.background = ContextCompat.getDrawable(requireContext(),R.drawable.next_button_shape)
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
            getString(R.string.phone_state_permission),
            getString(R.string.phone_state_rationale),
            Manifest.permission.READ_PHONE_STATE,
            activityResultLauncher
        )
    }



}