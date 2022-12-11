package com.threebanders.recordr.ui

import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    val mainActivity: BaseActivity?
        get() = activity as BaseActivity?
    val baseActivity: BaseActivity?
        get() = activity as BaseActivity?
}