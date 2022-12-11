package com.threebanders.recordr.viewmodels.contact_details

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.threebanders.recordr.R
import com.threebanders.recordr.common.DialogInfo
import com.threebanders.recordr.ui.BaseActivity
import com.threebanders.recordr.ui.contact.ContactDetailFragment
import com.threebanders.recordr.ui.contact.ContactsListActivityMain
import core.threebanders.recordr.data.Contact
import java.io.File

object ContactDetailsExtra {
    const val SELECT_MODE_KEY = "select_mode_key"
    const val SELECTED_ITEMS_KEY = "selected_items_key"
    const val EFFECT_TIME = 250
    const val RECORDING_EXTRA = "recording_extra"

    const val ARG_CONTACT = "arg_contact"

    fun showRecyclerView(
        recordingsRecycler: RecyclerView?,
        activity: BaseActivity?,
        recordingAdapter: ContactDetailFragment.RecordingAdapter
    ) {
        recordingsRecycler!!.layoutManager = LinearLayoutManager(activity)
        recordingsRecycler.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
        recordingsRecycler.adapter = recordingAdapter
    }

    fun initRecycler(
        adapter: ContactDetailFragment.RecordingAdapter,
        recordingsRecycler: RecyclerView,
        mainActivity: ContactsListActivityMain
    ) {
        recordingsRecycler.layoutManager = LinearLayoutManager(mainActivity)
        recordingsRecycler.addItemDecoration(
            DividerItemDecoration(
                mainActivity,
                DividerItemDecoration.VERTICAL
            )
        )
        recordingsRecycler.adapter = adapter
    }

    fun showDialog(
        mainActivity: ContactsListActivityMain,
        selectedItems: Int,
        onAction: () -> Unit
    ) {
        MaterialDialog.Builder(mainActivity)
            .title(R.string.delete_recording_confirm_title)
            .content(
                String.format(
                    mainActivity.resources.getString(
                        R.string.delete_recording_confirm_message
                    ), selectedItems
                )
            )
            .positiveText(android.R.string.ok)
            .negativeText(android.R.string.cancel)
            .icon(ResourcesCompat.getDrawable(mainActivity.resources, R.drawable.warning, null)!!)
            .onPositive { _: MaterialDialog, _: DialogAction ->
                onAction.invoke()
            }
            .show()
    }

    fun showSecondaryDialog(mainActivity: ContactsListActivityMain, result: DialogInfo) {
        MaterialDialog.Builder(mainActivity)
            .title(result.title)
            .content(result.message)
            .icon(ResourcesCompat.getDrawable(mainActivity.resources, result.icon, null)!!)
            .positiveText(android.R.string.ok)
            .show()
    }

    fun fadeEffect(view: View, finalAlpha: Float, finalVisibility: Int, EFFECT_TIME: Int) {
        view.animate()
            .alpha(finalAlpha)
            .setDuration(EFFECT_TIME.toLong())
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    view.visibility = finalVisibility
                }

                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
    }

    fun shareRecorder(path: String, context: Context?) {
        try {
            val f = File(path)
            val uri = context?.let {
                FileProvider.getUriForFile(it, "com.threebanders.recordr.CrApp.provider", f)
            }
            val share = Intent(Intent.ACTION_SEND)
            share.putExtra(Intent.EXTRA_STREAM, uri)
            share.type = "audio/*"
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context!!.startActivity(Intent.createChooser(share, "Share audio File"))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun modifyMargins(recording: View, context: Context, selectMode: Boolean) {
        val checkBox = recording.findViewById<CheckBox>(R.id.recording_checkbox)
        val res = context.resources
        checkBox.visibility = if (selectMode) View.VISIBLE else View.GONE
        val lpCheckBox = checkBox.layoutParams as RelativeLayout.LayoutParams
        lpCheckBox.marginStart =
            if (selectMode) res.getDimension(R.dimen.recording_checkbox_visible_start_margin)
                .toInt() else res.getDimension(R.dimen.recording_checkbox_gone_start_margin).toInt()
        checkBox.layoutParams = lpCheckBox
        val recordingAdorn = recording.findViewById<ImageView>(R.id.recording_adorn)
        val lpRecAdorn = recordingAdorn.layoutParams as RelativeLayout.LayoutParams
        lpRecAdorn.marginStart =
            if (selectMode) res.getDimension(R.dimen.recording_adorn_selected_margin_start)
                .toInt() else res.getDimension(R.dimen.recording_adorn_unselected_margin_start)
                .toInt()
        recordingAdorn.layoutParams = lpRecAdorn
        val title = recording.findViewById<TextView>(R.id.recording_title)
        val lpTitle = title.layoutParams as RelativeLayout.LayoutParams
        lpTitle.marginStart =
            if (selectMode) res.getDimension(R.dimen.recording_title_selected_margin_start)
                .toInt() else res.getDimension(R.dimen.recording_title_unselected_margin_start)
                .toInt()
        title.layoutParams = lpTitle
    }

    fun selectRecording(recording: View) {
        val checkBox = recording.findViewById<CheckBox>(R.id.recording_checkbox)
        checkBox.isChecked = true
    }

    fun deselectRecording(recording: View) {
        val checkBox = recording.findViewById<CheckBox>(R.id.recording_checkbox)
        checkBox.isChecked = false
    }

    fun redrawRecordings(adapter: ContactDetailFragment.RecordingAdapter) {
        for (i in 0 until adapter.itemCount) adapter.notifyItemChanged(i)
    }

    fun markNonexistent(
        holder: ContactDetailFragment.RecordingHolder,
        mainActivity: ContactsListActivityMain
    ) {
        holder.exclamation.visibility = View.VISIBLE
        val filter =
            if (mainActivity.settledTheme == BaseActivity.LIGHT_THEME) Color.argb(
                255,
                0,
                0,
                0
            ) else Color.argb(255, 255, 255, 255)
        holder.recordingAdorn.setColorFilter(filter)
        holder.recordingType.setColorFilter(filter)
        holder.recordingAdorn.imageAlpha = 100
        holder.recordingType.imageAlpha = 100
        holder.title.alpha = 0.5f
    }

    fun unMarkNonexistent(holder: ContactDetailFragment.RecordingHolder) {
        holder.exclamation.visibility = View.GONE
        holder.recordingAdorn.colorFilter = null
        holder.recordingType.colorFilter = null
        holder.recordingType.imageAlpha = 255
        holder.recordingAdorn.imageAlpha = 255
        holder.title.alpha = 1f
    }

    fun removeIfPresentInSelectedItems(
        adapterPosition: Int,
        selectedItems: MutableList<Int>
    ): Boolean {
        return if (selectedItems.contains(adapterPosition)) {
            selectedItems.remove(adapterPosition)
            true
        } else false
    }

    fun newInstance(contact: Contact?): ContactDetailFragment {
        val args = Bundle()
        args.putParcelable(ARG_CONTACT, contact)
        val fragment = ContactDetailFragment()
        fragment.arguments = args

        return fragment
    }
}