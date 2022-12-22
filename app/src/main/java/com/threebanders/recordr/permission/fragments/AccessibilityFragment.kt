package com.threebanders.recordr.permission.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.viewmodels.MainViewModel

class AccessibilityFragment : Fragment() {
    private lateinit var rootView: View
    private lateinit var switchOnButton: Button
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.accessibility_fragment_layout, container, false)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        switchOnButton = rootView.findViewById(R.id.switchOnAccessibility)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Extras.isAccessibilityServiceEnabled(requireContext())) {
            mainViewModel.openActivity(requireActivity())
        }

        switchOnButton.setOnClickListener {
            mainViewModel.openAccessibilitySettings(activityResultLauncher)
        }

    }

    private var activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                mainViewModel.openActivity(requireActivity())
            }
        }
}


