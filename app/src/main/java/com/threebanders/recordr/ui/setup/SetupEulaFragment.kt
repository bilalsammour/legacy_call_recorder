package com.threebanders.recordr.ui.setup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.threebanders.recordr.CrApp
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import org.acra.BuildConfig

class SetupEulaFragment : Fragment() {
    private lateinit var version: TextView
    private lateinit var showEula: Button
    private lateinit var cancelButton: Button
    private lateinit var nextButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.setup_eula_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val parentActivity = activity as SetupActivity?
        val checkResult = parentActivity!!.checkResult

        prepareUi(parentActivity)

        showEula.setOnClickListener {
            startActivity(Intent(activity, ShowEulaActivity::class.java))
        }

        nextButton.setOnClickListener(View.OnClickListener {
            val hasAccepted = parentActivity.findViewById<CheckBox>(R.id.has_accepted)
            if (!hasAccepted.isChecked || activity == null) {
                return@OnClickListener
            }

            val settings = (requireActivity().application as CrApp).core.prefs
            val editor = settings.edit()
            editor.putBoolean(Extras.HAS_ACCEPTED_EULA, true)
            editor.apply()
            if (checkResult and Extras.PERMS_NOT_GRANTED != 0) {
                val permissionsFragment = SetupPermissionsFragment()
                showFragment(permissionsFragment, parentActivity)
            } else {
                val powerFragment = SetupPowerFragment()
                showFragment(powerFragment, parentActivity)
            }
        })
    }

    private fun prepareUi(parentActivity: SetupActivity) {
        version = parentActivity.findViewById(R.id.app_version)
        version.text = String.format(
            parentActivity.resources.getString(R.string.version_eula_screen),
            BuildConfig.VERSION_NAME
        )
        showEula = parentActivity.findViewById(R.id.show_eula)

        cancelButton = parentActivity.findViewById(R.id.setup_confirm_cancel)
        cancelButton.setOnClickListener { parentActivity.cancelSetup() }
        nextButton = parentActivity.findViewById(R.id.setup_confirm_next)
    }

    private fun showFragment(fragment: Fragment, parentActivity: SetupActivity) {
        parentActivity.supportFragmentManager.beginTransaction()
            .replace(R.id.setup_fragment_container, fragment)
            .commitAllowingStateLoss()
    }
}