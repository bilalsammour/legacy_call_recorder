package com.threebanders.recordr.ui.contact

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.ui.BaseActivity
import com.threebanders.recordr.ui.setup.SetupActivity
import com.threebanders.recordr.viewmodels.MainViewModel
import core.threebanders.recordr.MyService

class ContactsListActivityMain : BaseActivity() {
    private var fm: FragmentManager? = null
    private var unassignedToInsert: Fragment? = null
    private lateinit var viewModel: MainViewModel
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

        prepareUi()

        setupRecorderFragment()

        if (savedInstanceState == null) {
            insertFragment(R.id.contacts_list_fragment_container)
        }

        setUpNavigationView()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.showOnBackPressedDialog(this@ContactsListActivityMain) {
                    finish()
                }
            }
        })
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
                R.id.settings -> viewModel.openSettingsActivityInApp(this)
                R.id.help -> viewModel.openHelpActivityInApp(this)
                R.id.rate_app -> viewModel.openGoogleMarketInApp(this)
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
}