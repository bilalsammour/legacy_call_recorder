package com.threebanders.recordr.viewmodels

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.threebanders.recordr.common.DialogInfo
import com.threebanders.recordr.common.Extras
import com.threebanders.recordr.ui.contact.ContactDetailFragment
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import core.threebanders.recordr.data.Contact

class ContactDetailsViewModel : ViewModel() {

    /* -------------------------- Contact Details Fragment ---------------------------- */
    fun init(adapter : ContactDetailFragment.RecordingAdapter, recordingsRecycler : RecyclerView, mainActivity : ContactsListActivityMain){
        Extras.initRecycler(adapter,recordingsRecycler, mainActivity)
    }

    fun showDeleteDialog(mainActivity : ContactsListActivityMain, selectedItems : Int, onAction : () -> Unit){
        Extras.showDialog(mainActivity,selectedItems,onAction)
    }

    fun showSecondaryDialog(mainActivity : ContactsListActivityMain, result : DialogInfo){
        Extras.showSecondaryDialog(mainActivity,result)
    }
    fun fadeEffect(view: View, finalAlpha: Float, finalVisibility: Int, EFFECT_TIME: Int){
        Extras.fadeEffect(view, finalAlpha, finalVisibility, EFFECT_TIME)
    }
    fun shareRecording(path : String,context: Context?){
        Extras.shareRecorder(path, context)
    }

    fun modifyMargins(recording: View, context: Context, selectMode : Boolean){
        Extras.modifyMargins(recording,context, selectMode)
    }
    fun selectRecording(recording: View){
        Extras.selectRecording(recording)
    }
    fun deselectRecording(recording: View){
        Extras.deselectRecording(recording)
    }

    fun redrawRecordings(adapter: ContactDetailFragment.RecordingAdapter){
        Extras.redrawRecordings(adapter)
    }

    fun markNonexistent(holder: ContactDetailFragment.RecordingHolder, mainActivity: ContactsListActivityMain){
        Extras.markNonexistent(holder, mainActivity)
    }

    fun unMarkNonexistent(holder: ContactDetailFragment.RecordingHolder){
        Extras.unMarkNonexistent(holder)
    }

    fun removeIfPresentInSelectedItems(adapterPosition: Int,selectedItems : MutableList<Int>): Boolean {
        return Extras.removeIfPresentInSelectedItems(adapterPosition, selectedItems)
    }

    fun newInstance(contact: Contact?): ContactDetailFragment {
        return  Extras.newInstance(contact)
    }
}