package com.threebanders.recordr.ui.contact

import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.threebanders.recordr.R
import com.threebanders.recordr.ui.BaseActivity.LayoutType
import com.threebanders.recordr.viewmodels.contact_details.ContactDetailsExtra
import core.threebanders.recordr.data.Recording
import core.threebanders.recordr.recorder.Recorder
import kotlinx.coroutines.launch

class UnassignedRecordingsFragment : ContactDetailFragment() {
    private lateinit var rootView: View
    private var record: Recorder? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.unassigned_recordings_fragment, container, false)

        recordingsRecycler = rootView.findViewById(R.id.unassigned_recordings)
        ContactDetailsExtra.showRecyclerView(recordingsRecycler, baseActivity, adapter!!)
        record = Recorder(context)

        lifecycleScope.launch {
            mainViewModel.loadRecordings()
            mainViewModel.records.observe(viewLifecycleOwner) { recordings: List<Recording?>? ->
                paintViews()
                if (recordings?.size != 0) {
                    mainViewModel.getPrefs(requireContext())
                    mainViewModel.setPrefs(requireContext(), recordings as List<Recording?>)
                }
            }
        }

        paintViews()
        mainViewModel.deletedRecording.observe(viewLifecycleOwner) { removeRecording() }
        removeRecording()

        return rootView
    }


    override fun toggleTitle() {
        val title = baseActivity?.findViewById<TextView>(R.id.actionbar_title)
        val params = title?.layoutParams as Toolbar.LayoutParams
        params.gravity = if (selectMode) Gravity.START else Gravity.CENTER
        title.layoutParams = params
        title.text =
            if (selectMode) selectedItems!!.size.toString() else getString(R.string.app_name)
    }

    public override fun toggleSelectModeActionBar(animate: Boolean) {
        val closeBtn = baseActivity?.findViewById<ImageButton>(R.id.close_select_mode)
        val selectAllBtn =
            baseActivity?.findViewById<ImageButton>(R.id.actionbar_select_all)
        val infoBtn = baseActivity?.findViewById<ImageButton>(R.id.actionbar_info)
        val menuRightSelectedBtn =
            baseActivity?.findViewById<ImageButton>(R.id.contact_detail_selected_menu)
        val hamburger = baseActivity?.findViewById<ImageButton>(R.id.hamburger)
        toggleTitle()
        if (baseActivity?.layoutType == LayoutType.DOUBLE_PANE && selectMode) {
            val menuRightBtn =
                baseActivity?.findViewById<ImageButton>(R.id.contact_detail_menu)
            hideView(menuRightBtn!!, animate)
        }
        if (selectMode) showView(closeBtn, animate) else hideView(closeBtn, animate)
        if (selectMode) showView(selectAllBtn, animate) else hideView(selectAllBtn, animate)
        if (selectMode) showView(infoBtn, animate) else hideView(infoBtn, animate)
        if (selectMode) showView(menuRightSelectedBtn, animate) else hideView(
            menuRightSelectedBtn,
            animate
        )
        if (selectMode) hideView(hamburger, animate) else showView(hamburger, animate)
    }

    override fun paintViews() {
        if (selectMode) putInSelectMode(false)
        val noContent = rootView.findViewById<TextView>(R.id.no_content_detail)
        adapter!!.replaceData(mainViewModel.records.value!!, callDetails)

        noContent.visibility =
            if (mainViewModel.records.value!!.size > 0) View.GONE else View.VISIBLE
    }

    override fun setDetailsButtonsListeners() {
        val closeBtn = baseActivity?.findViewById<ImageButton>(R.id.close_select_mode)
        closeBtn!!.setOnClickListener { clearSelectMode() }
        val menuButtonSelected =
            baseActivity?.findViewById<ImageButton>(R.id.contact_detail_selected_menu)
        menuButtonSelected!!.setOnClickListener { view: View ->
            val popupMenu = PopupMenu(
                baseActivity!!, view
            )
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.delete_recording -> {
                        onDeleteSelectedRecordings()
                        return@setOnMenuItemClickListener true
                    }
                    else -> return@setOnMenuItemClickListener false
                }
            }
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.recording_selected_popup, popupMenu.menu)
            (recordingsRecycler!!.adapter as RecordingAdapter?)!!.getItem(
                selectedItems!![0]
            )

            popupMenu.show()
        }

        val selectAllBtn =
            baseActivity?.findViewById<ImageButton>(R.id.actionbar_select_all)
        selectAllBtn!!.setOnClickListener { onSelectAll() }
        val infoBtn = baseActivity?.findViewById<ImageButton>(R.id.actionbar_info)
        infoBtn!!.setOnClickListener { onRecordingInfo() }
    }
}
