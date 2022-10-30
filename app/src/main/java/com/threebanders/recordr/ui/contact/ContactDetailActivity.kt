/*
 * Copyright (C) 2019 Eugen Rădulescu <synapticwebb@gmail.com> - All rights reserved.
 *
 * You may use, distribute and modify this code only under the conditions
 * stated in the SW Call Recorder license. You should have received a copy of the
 * SW Call Recorder license along with this file. If not, please write to <synapticwebb@gmail.com>.
 */
package com.threebanders.recordr.ui.contact

import com.threebanders.recordr.ui.BaseActivity
import android.os.Bundle
import com.threebanders.recordr.R
import android.widget.TextView
import core.threebanders.recordr.data.Contact
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

class ContactDetailActivity : BaseActivity() {
    var contact: Contact? = null
    override fun createFragment(): Fragment? {
        return ContactDetailFragment.Companion.newInstance(contact)
    }

    override fun onResume() {
        super.onResume()
        checkIfThemeChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.contact_detail_activity)
        val intent = intent
        contact = intent.getParcelableExtra(ContactsListFragment.Companion.ARG_CONTACT)
        insertFragment(R.id.contact_detail_fragment_container)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_detail)
        val title = findViewById<TextView>(R.id.actionbar_title)
        title.text = contact!!.contactName
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false)
            actionBar.setDisplayShowTitleEnabled(false)
        }
    }
}