package com.threebanders.recordr.viewmodels.contact_details

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.threebanders.recordr.common.DialogInfo
import com.threebanders.recordr.ui.contact.ContactDetailFragment
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import core.threebanders.recordr.data.Contact

class ContactDetailsViewModel : ViewModel() {
    fun init(
        adapter: ContactDetailFragment.RecordingAdapter,
        recordingsRecycler: RecyclerView,
        mainActivity: ContactsListActivityMain
    ) {
        ContactDetailsExtra.initRecycler(adapter, recordingsRecycler, mainActivity)
    }

    fun showDeleteDialog(
        mainActivity: ContactsListActivityMain,
        selectedItems: Int,
        onAction: () -> Unit
    ) {
        ContactDetailsExtra.showDialog(mainActivity, selectedItems, onAction)
    }

    fun showSecondaryDialog(mainActivity: ContactsListActivityMain, result: DialogInfo) {
        ContactDetailsExtra.showSecondaryDialog(mainActivity, result)
    }

    fun fadeEffect(view: View, finalAlpha: Float, finalVisibility: Int, EFFECT_TIME: Int) {
        ContactDetailsExtra.fadeEffect(view, finalAlpha, finalVisibility, EFFECT_TIME)
    }

    fun shareRecording(path: String, context: Context?) {
        ContactDetailsExtra.shareRecorder(path, context)
    }

    fun modifyMargins(recording: View, context: Context, selectMode: Boolean) {
        ContactDetailsExtra.modifyMargins(recording, context, selectMode)
    }

    fun selectRecording(recording: View) {
        ContactDetailsExtra.selectRecording(recording)
    }

    fun deselectRecording(recording: View) {
        ContactDetailsExtra.deselectRecording(recording)
    }

    fun redrawRecordings(adapter: ContactDetailFragment.RecordingAdapter) {
        ContactDetailsExtra.redrawRecordings(adapter)
    }

    fun markNonexistent(
        holder: ContactDetailFragment.RecordingHolder,
        mainActivity: ContactsListActivityMain
    ) {
        ContactDetailsExtra.markNonexistent(holder, mainActivity)
    }

    fun unMarkNonexistent(holder: ContactDetailFragment.RecordingHolder) {
        ContactDetailsExtra.unMarkNonexistent(holder)
    }

    fun removeIfPresentInSelectedItems(
        adapterPosition: Int,
        selectedItems: MutableList<Int>
    ): Boolean {
        return ContactDetailsExtra.removeIfPresentInSelectedItems(adapterPosition, selectedItems)
    }

    fun newInstance(contact: Contact?): ContactDetailFragment {
        return ContactDetailsExtra.newInstance(contact)
    }
}