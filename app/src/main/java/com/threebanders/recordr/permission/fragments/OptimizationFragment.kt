package com.threebanders.recordr.permission.fragments

import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.viewmodels.MainViewModel

class OptimizationFragment : Fragment() {
    private lateinit var rootView: View
    private lateinit var turnOffBtn: Button
    private lateinit var pm: PowerManager
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(
            R.layout.optimization_fragment_layout, container,
            false
        )

        turnOffBtn = rootView.findViewById(R.id.turnOffBtn)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager

        turnOffBtn.setOnClickListener {
            if (turnOffBtn.text.toString() == getString(R.string.turn_off_button)) {
                if (!mainViewModel.isAppOptimized(pm, requireContext().packageName)) {
                    mainViewModel.doNotOptimizeApp(requireActivity())
                    turnOffBtn.text = getString(R.string.finish_button)
                    turnOffBtn.background = ContextCompat.getDrawable(requireContext(),R.drawable.next_button_shape)
                } else {
                    mainViewModel.openActivity(requireActivity())
                }
            } else if (turnOffBtn.text == getString(R.string.finish_button)) {
                if (mainViewModel.isAppOptimized(pm, requireContext().packageName)) {
                    requireActivity().supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, AccessibilityFragment())
                        .commit()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.do_not_optimize_text),
                        Toast.LENGTH_LONG
                    ).show()
                    turnOffBtn.text = getString(R.string.turn_off_button)
                }
            }
        }
    }

}