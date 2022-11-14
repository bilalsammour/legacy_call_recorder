/*
 * Copyright (C) 2019 Eugen Rădulescu <synapticwebb@gmail.com> - All rights reserved.
 *
 * You may use, distribute and modify this code only under the conditions
 * stated in the SW Call Recorder license. You should have received a copy of the
 * SW Call Recorder license along with this file. If not, please write to <synapticwebb@gmail.com>.
 */
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