package com.threebanders.recordr.ui.contact

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.threebanders.recordr.R
import com.threebanders.recordr.common.ContactsExtras
import com.threebanders.recordr.ui.BaseActivity
import com.threebanders.recordr.ui.MainViewModel
import com.threebanders.recordr.ui.setup.SetupActivity
import core.threebanders.recordr.MyService

class ContactsListActivityMain : BaseActivity() {
    private var fm: FragmentManager? = null
    private var unassignedToInsert: Fragment? = null
    private var viewModel: MainViewModel? = null
    private lateinit var toolbar: Toolbar
    private lateinit var title: TextView
    private lateinit var hamburger: ImageButton
    private lateinit var drawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var permsNotGranted = 0
    private var powerOptimized = 0

    override fun createFragment(): Fragment {
        return UnassignedRecordingsFragment()
    }

    override fun onResume() {
        super.onResume()

        checkIfThemeChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contacts_list_activity)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        if (!ContactsExtras.isMyServiceRunning(this, MyService::class.java)) {
            ContactsExtras.showAccessibilitySettings(this)
        }

        prepareUi()
        checkValidations()
        if (savedInstanceState == null) insertFragment(R.id.contacts_list_fragment_container)
        setUpNavigationView()
    }


    private fun checkValidations() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        val settings = prefs
        val eulaNotAccepted = if (settings.getBoolean(
                ContactsExtras.HAS_ACCEPTED_EULA,
                false
            )
        ) 0 else ContactsExtras.EULA_NOT_ACCEPTED

        permsNotGranted =
            if (viewModel!!.checkPermissions(this)) 0 else ContactsExtras.PERMS_NOT_GRANTED
        powerOptimized =
            if (viewModel!!.isIgnoringBatteryOptimizations(this)) 0 else ContactsExtras.POWER_OPTIMIZED

        val checkResult = eulaNotAccepted or permsNotGranted or powerOptimized
        if (checkResult != 0) {
            ContactsExtras.openSetupActivity(this, checkResult)
        } else {
            setupRecorderFragment()
        }
    }

    private fun setUpNavigationView() {
        val navWidth: Int
        val pixelsDp =
            (resources.displayMetrics.widthPixels / resources.displayMetrics.density).toInt()
        navWidth = if (pixelsDp >= 480) (resources.displayMetrics.widthPixels * 0.4).toInt()
        else (resources.displayMetrics.widthPixels * 0.8).toInt()

        val params = navigationView.layoutParams as DrawerLayout.LayoutParams
        params.width = navWidth
        navigationView.layoutParams = params

        hamburger.setOnClickListener { drawer.openDrawer(GravityCompat.START) }
        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.settings -> ContactsExtras.openSettingsActivity(this)
                R.id.help -> ContactsExtras.openHelpActivity(this)
                R.id.rate_app -> ContactsExtras.openGoogleMarket(this)
            }
            drawer.closeDrawers()
            true
        }
    }

    private fun prepareUi() {
        toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        title = findViewById(R.id.actionbar_title)
        title.text = getString(R.string.app_name)

        hamburger = findViewById(R.id.hamburger)
        drawer = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
    }

    private fun setupRecorderFragment() {
        unassignedToInsert = UnassignedRecordingsFragment()
        fm = supportFragmentManager
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == ContactsExtras.SETUP_ACTIVITY) {
            setupRecorderFragment()
            if (data!!.getBooleanExtra(SetupActivity.EXIT_APP, true)) {
                finish()
            }
        }
    }

    override fun onBackPressed() {
        ContactsExtras.showExitDialog(this) {
            super@ContactsListActivityMain.onBackPressed()
        }
    }
}