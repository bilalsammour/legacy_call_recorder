package com.threebanders.recordrs.ui

import androidx.fragment.app.Fragment
import com.threebanders.recordrs.ui.contact.ContactsListActivityMain

open class BaseFragment : Fragment() {
    val mainActivity: ContactsListActivityMain?
        get() = activity as ContactsListActivityMain?
    val baseActivity: BaseActivity?
        get() = activity as BaseActivity?
//    var mainViewModel: MainViewModel? = null
//        get() = mainActivity?.viewModel
}