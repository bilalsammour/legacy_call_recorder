package com.threebanders.recordr.ui.contact

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.threebanders.recordr.R
import com.threebanders.recordr.ui.BaseActivity
import com.threebanders.recordr.viewmodels.contact_details.ContactDetailsExtra
import core.threebanders.recordr.data.Contact

class ContactDetailActivity : BaseActivity() {
    var contact: Contact? = null
    override fun createFragment(): Fragment {
        return ContactDetailsExtra.newInstance(contact)
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
        contact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(ContactsListFragment.ARG_CONTACT, Contact::class.java)
        } else {
            intent.getParcelableExtra(ContactsListFragment.ARG_CONTACT)
        }
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