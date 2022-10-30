/*
 * Copyright (C) 2019 Eugen Rădulescu <synapticwebb@gmail.com> - All rights reserved.
 *
 * You may use, distribute and modify this code only under the conditions
 * stated in the SW Call Recorder license. You should have received a copy of the
 * SW Call Recorder license along with this file. If not, please write to <synapticwebb@gmail.com>.
 */
package com.threebanders.recordr.ui.setup

import com.threebanders.recordr.ui.BaseActivity
import android.os.Bundle
import com.threebanders.recordr.ui.help.HelpActivity
import core.threebanders.recordr.CoreUtil
import com.threebanders.recordr.R
import android.webkit.WebView
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

class ShowEulaActivity : BaseActivity() {
    override fun createFragment(): Fragment? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setup_show_eula_activity)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_show_eula)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        var html = CoreUtil.rawHtmlToString(R.raw.eula, this)
        html = html.replace(
            HelpActivity.Companion.APP_NAME_PLACEHOLDER,
            resources.getString(R.string.app_name)
        )
        val eulaHtml = findViewById<WebView>(R.id.eula_hmtl)
        eulaHtml.loadDataWithBaseURL(
            "file:///android_asset/",
            html, "text/html", null, null
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true
    }
}