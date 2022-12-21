package com.threebanders.recordr.permission.fragments

import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.threebanders.recordr.R
import com.threebanders.recordr.viewmodels.MainViewModel

class OptimizationFragment : Fragment() {
    private lateinit var rootView: View
    private lateinit var turnOffBtn : Button
    private lateinit var nextBtn : Button
    private lateinit var pm: PowerManager
    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.optimization_fragment_layout, container, false)
        turnOffBtn  =  rootView.findViewById(R.id.turnOffBtn)
        nextBtn = rootView.findViewById(R.id.nextBtn)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        turnOffBtn.setOnClickListener {
            mainViewModel.doNotOptimizeApp(requireActivity())
            turnOffBtn.visibility = View.GONE
            nextBtn.visibility = View.VISIBLE
        }
        nextBtn.setOnClickListener {
            if (mainViewModel.isAppOptimized(pm, requireContext().packageName)) {
                mainViewModel.moveToAccessibilityFragment(requireActivity())
            } else {
                turnOffBtn.visibility = View.VISIBLE
                nextBtn.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.do_not_optimize_text), Toast.LENGTH_LONG).show()
            }
        }
    }
}