package com.threebanders.recordrs.ui.settings

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceFragmentCompat
import com.threebanders.recordrs.R
import com.threebanders.recordrs.ui.BaseActivity

class SettingsActivity : BaseActivity() {
    override fun createFragment(): PreferenceFragmentCompat? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.settings_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_settings)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }
}