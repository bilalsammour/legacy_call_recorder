package com.threebanders.recordr.ui.contact

import android.content.Intent
import android.os.Bundle
import android.provider.CallLog
import android.view.*
import android.view.View.OnLongClickListener
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Extras.EFFECT_TIME
import com.threebanders.recordr.common.Extras.RECORDING_EXTRA
import com.threebanders.recordr.common.Extras.SELECTED_ITEMS_KEY
import com.threebanders.recordr.common.Extras.SELECT_MODE_KEY
import com.threebanders.recordr.ui.BaseActivity
import com.threebanders.recordr.ui.BaseActivity.LayoutType
import com.threebanders.recordr.ui.BaseFragment
import com.threebanders.recordr.ui.MainViewModel
import com.threebanders.recordr.ui.player.PlayerActivity
import core.threebanders.recordr.CoreUtil
import core.threebanders.recordr.data.Contact
import core.threebanders.recordr.data.Recording
import core.threebanders.recordr.recorder.Recorder
import java.text.SimpleDateFormat
import java.util.*

open class ContactDetailFragment : BaseFragment() {
    protected var adapter: RecordingAdapter? = null
    protected var recordingsRecycler: RecyclerView? = null
    private var detailView: RelativeLayout? = null
    protected var selectMode = false
    lateinit var mainViewModel: MainViewModel
    protected var selectedItems: MutableList<Int>? = ArrayList()
    private var selectedItemsDeleted = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = RecordingAdapter(ArrayList(0))
        mainViewModel = ViewModelProvider(mainActivity!!)[MainViewModel::class.java]

        if (savedInstanceState != null) {
            selectMode = savedInstanceState.getBoolean(SELECT_MODE_KEY)
            selectedItems = savedInstanceState.getIntegerArrayList(SELECTED_ITEMS_KEY)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        detailView =
            inflater.inflate(R.layout.contact_detail_fragment, container, false) as RelativeLayout
        recordingsRecycler = detailView!!.findViewById(R.id.recordings)
        mainViewModel.init(adapter!!, recordingsRecycler!!, mainActivity!!)
        return detailView
    }

    override fun onResume() {
        super.onResume()

        val mainViewModel = ViewModelProvider(mainActivity!!)[MainViewModel::class.java]
        mainViewModel.loadRecordings()
    }

    protected fun onDeleteSelectedRecordings() {
        mainViewModel.showDeleteDialog(mainActivity!!, selectedItems!!.size) {
            val result = mainViewModel.deleteRecordings(selectedRecordings)
            if (result != null) mainViewModel.showSecondaryDialog(mainActivity!!, result) else {
                if (adapter!!.itemCount == 0) {
                    val noContent = mainActivity!!.findViewById<View>(R.id.no_content_detail)
                    if (noContent != null) noContent.visibility = View.VISIBLE
                }
                clearSelectMode()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDetailsButtonsListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SELECT_MODE_KEY, selectMode)
        outState.putIntegerArrayList(SELECTED_ITEMS_KEY, selectedItems as ArrayList<Int>?)
    }

    open fun paintViews() {
        adapter!!.replaceData(mainViewModel.records.value!!, callDetails)
        if (selectMode) {
            putInSelectMode(false)
        } else {
            toggleSelectModeActionBar(false)
        }
        val noContent = detailView!!.findViewById<TextView>(R.id.no_content_detail)
        if (mainViewModel.records.value!!.size > 0) {
            noContent.visibility =
                View.GONE
        } else {
            noContent.visibility = View.VISIBLE
        }
    }

    fun removeRecording() {
        adapter!!.removeItem(mainViewModel.deletedRecording.value)
    }

    protected fun putInSelectMode(animate: Boolean) {
        selectMode = true
        toggleSelectModeActionBar(animate)
        mainViewModel.redrawRecordings(adapter!!)
    }

    protected open fun toggleSelectModeActionBar(animate: Boolean) {
        val navigateBackBtn = mainActivity!!.findViewById<ImageButton>(R.id.navigate_back)
        val closeBtn = mainActivity!!.findViewById<ImageButton>(R.id.close_select_mode)
        val selectAllBtn = mainActivity!!.findViewById<ImageButton>(R.id.actionbar_select_all)
        val infoBtn = mainActivity!!.findViewById<ImageButton>(R.id.actionbar_info)
        val menuRightBtn = mainActivity!!.findViewById<ImageButton>(R.id.contact_detail_menu)
        val menuRightSelectedBtn =
            mainActivity!!.findViewById<ImageButton>(R.id.contact_detail_selected_menu)
        toggleTitle()
        if (mainActivity!!.layoutType == LayoutType.SINGLE_PANE) if (selectMode) hideView(
            navigateBackBtn,
            animate
        ) else {
            showView(navigateBackBtn, animate)
        }
        if (selectMode) {
            showView(closeBtn, animate)
        } else {
            hideView(closeBtn, animate)
        }
        if (selectMode) {
            showView(selectAllBtn, animate)
        } else {
            hideView(selectAllBtn, animate)
        }
        if (selectMode) {
            showView(infoBtn, animate)
        } else {
            hideView(infoBtn, animate)
        }
        if (selectMode) {
            showView(menuRightSelectedBtn, animate)
        } else {
            hideView(
                menuRightSelectedBtn,
                animate
            )
        }
        if (selectMode) {
            hideView(menuRightBtn, animate)
        } else {
            showView(menuRightBtn, animate)
        }
        if (mainActivity!!.layoutType == LayoutType.DOUBLE_PANE) {
            val hamburger = mainActivity!!.findViewById<ImageButton>(R.id.hamburger)
            if (selectMode) {
                hideView(hamburger, animate)
            } else {
                showView(hamburger, animate)
            }
        }
    }

    protected open fun toggleTitle() {
        val title = mainActivity!!.findViewById<TextView>(R.id.actionbar_title)
        if (mainActivity!!.layoutType == LayoutType.DOUBLE_PANE) {
            val params = title.layoutParams as Toolbar.LayoutParams
            params.gravity = if (selectMode) Gravity.START else Gravity.CENTER
            title.layoutParams = params
        }
        if (selectMode) title.text = selectedItems!!.size.toString() else {
            if (mainActivity!!.layoutType == LayoutType.SINGLE_PANE) title.text =
                mainViewModel.contact.value!!.contactName else title.setText(R.string.app_name)
        }
    }

    protected fun hideView(v: View?, animate: Boolean) {
        if (animate) mainViewModel.fadeEffect(v!!, 0.0f, View.GONE, EFFECT_TIME) else {
            v!!.alpha = 0.0f //poate lipsi?
            v.visibility = View.GONE
        }
    }

    protected fun showView(vw: View?, animate: Boolean) {
        if (animate) mainViewModel.fadeEffect(vw!!, 1f, View.VISIBLE, EFFECT_TIME) else {
            vw?.alpha = 1f //poate lipsi?
            vw?.visibility = View.VISIBLE
        }
    }

    protected fun clearSelectMode() {
        selectMode = false
        toggleSelectModeActionBar(true)
        mainViewModel.redrawRecordings(adapter!!)
        selectedItems!!.clear()
    }

    private fun manageSelectRecording(recording: View, adapterPosition: Int, exists: Boolean) {
        if (!mainViewModel.removeIfPresentInSelectedItems(adapterPosition, selectedItems!!)) {
            selectedItems!!.add(adapterPosition)
            mainViewModel.selectRecording(recording)
            if (!exists) {
                selectedItemsDeleted++
            }
        } else {
            mainViewModel.deselectRecording(recording)
            if (!exists) {
                selectedItemsDeleted--
            }
        }
        if (selectedItems!!.isEmpty()) {
            clearSelectMode()
        } else {
            toggleTitle()
        }
    }

    private val selectedRecordings: List<Recording?>
        get() {
            val list: MutableList<Recording?> = ArrayList()
            for (adapterPosition in selectedItems!!) list.add(adapter!!.getItem(adapterPosition))
            return list
        }


    protected fun onSelectAll() {
        val notSelected: MutableList<Int> = ArrayList()
        for (i in 0 until adapter!!.itemCount) notSelected.add(i)
        notSelected.removeAll(selectedItems!!)
        for (position in notSelected) {
            selectedItems!!.add(position)
            adapter!!.notifyItemChanged(position)

            val selectedRecording = recordingsRecycler!!.layoutManager!!
                .findViewByPosition(position)
            selectedRecording?.let { mainViewModel.selectRecording(it) }
        }
        toggleTitle()
    }

    protected fun onRecordingInfo() {
        if (selectedItems!!.size > 1) {
            var totalSize: Long = 0
            for (position in selectedItems!!) {
                val recording = adapter!!.getItem(position)
                totalSize += recording.size
            }
            MaterialDialog.Builder(mainActivity!!)
                .title(R.string.recordings_info_title)
                .content(
                    String.format(
                        requireContext().resources.getString(R.string.recordings_info_text),
                        CoreUtil.getFileSizeHuman(totalSize)
                    )
                )
                .positiveText(android.R.string.ok)
                .show()
            return
        }
        val dialog = MaterialDialog.Builder(mainActivity!!)
            .title(R.string.recording_info_title)
            .customView(R.layout.info_dialog, false)
            .positiveText(android.R.string.ok).build()

        if (selectedItems!!.size != 1) {
            return
        }
        val recording = adapter!!.getItem(
            selectedItems!![0]
        )
        val date = dialog.view.findViewById<TextView>(R.id.info_date_data)
        date.text = String.format("%s %s", recording.date, recording.time)
        val size = dialog.view.findViewById<TextView>(R.id.info_size_data)
        size.text = CoreUtil.getFileSizeHuman(recording.size)
        val source = dialog.view.findViewById<TextView>(R.id.info_source_data)
        source.text = recording.source
        val format = dialog.view.findViewById<TextView>(R.id.info_format_data)
        format.text = recording.humanReadingFormat
        val length = dialog.view.findViewById<TextView>(R.id.info_length_data)
        length.text = CoreUtil.getDurationHuman(recording.length, true)
        val path = dialog.view.findViewById<TextView>(R.id.info_path_data)

        if (!recording.exists()) {
            path.text = String.format(
                "%s%s",
                path.text,
                mainActivity!!.resources.getString(R.string.nonexistent_file)
            )
            path.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.holo_red_light
                )
            )
        }
        dialog.show()
    }

    protected open fun setDetailsButtonsListeners() {
        val navigateBack = mainActivity!!.findViewById<ImageButton>(R.id.navigate_back)
        navigateBack.setOnClickListener {
            NavUtils.navigateUpFromSameTask(
                mainActivity!!
            )
        }
        val menuButtonSelectOff = mainActivity!!.findViewById<ImageButton>(R.id.contact_detail_menu)
        menuButtonSelectOff.setOnClickListener { vw: View? ->
            val popupMenu = PopupMenu(
                mainActivity, vw!!
            )
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    else -> return@setOnMenuItemClickListener false
                }
            }
        }

        val closeBtn = mainActivity!!.findViewById<ImageButton>(R.id.close_select_mode)
        closeBtn.setOnClickListener { clearSelectMode() }
        val menuButtonSelectOn =
            mainActivity!!.findViewById<ImageButton>(R.id.contact_detail_selected_menu)
        menuButtonSelectOn.setOnClickListener { v: View ->
            val popupMenu = PopupMenu(
                mainActivity, v
            )
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
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

        val selectAllBtn = mainActivity!!.findViewById<ImageButton>(R.id.actionbar_select_all)
        selectAllBtn.setOnClickListener { onSelectAll() }
        val deleteBtn = mainActivity!!.findViewById<ImageButton>(R.id.delete_recording)
        deleteBtn.setOnClickListener { onDeleteSelectedRecordings() }
        val infoBtn = mainActivity!!.findViewById<ImageButton>(R.id.actionbar_info)
        infoBtn.setOnClickListener { onRecordingInfo() }
    }

    inner class RecordingHolder(inflater: LayoutInflater, parent: ViewGroup?) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.recording, parent, false)),
        View.OnClickListener, OnLongClickListener {
        var title: TextView = itemView.findViewById(R.id.recording_title)
        var recordingType: ImageView = itemView.findViewById(R.id.recording_type)
        var recordingAdorn: ImageView = itemView.findViewById(R.id.recording_adorn)
        val recordingShare: ImageView = itemView.findViewById(R.id.recording_share)
        var exclamation: ImageView = itemView.findViewById(R.id.recording_exclamation)
        var checkBox: CheckBox = itemView.findViewById(R.id.recording_checkbox)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onLongClick(v: View): Boolean {
            if (!selectMode) putInSelectMode(true)
            val recording = adapter!!.getItem(bindingAdapterPosition)
            manageSelectRecording(v, this.bindingAdapterPosition, recording.exists())
            return true
        }

        override fun onClick(v: View) {
            val recording = adapter!!.getItem(bindingAdapterPosition)
            if (selectMode) manageSelectRecording(
                v,
                this.bindingAdapterPosition,
                recording.exists()
            ) else {
                if (recording.exists()) {
                    val playIntent = Intent(mainActivity, PlayerActivity::class.java)
                    playIntent.putExtra(RECORDING_EXTRA, recording)
                    startActivity(playIntent)
                } else Toast.makeText(mainActivity, R.string.audio_file_missing, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    inner class RecordingAdapter internal constructor(private var recordings: MutableList<Recording>) :
        RecyclerView.Adapter<RecordingHolder>() {
        private var contactList: List<Contact?>? = null

        fun replaceData(recordings: MutableList<Recording>, contactList: List<Contact>) {
            this.recordings = recordings
            this.contactList = contactList
            notifyDataSetChanged()
        }

        fun removeItem(recording: Recording?) {
            val position = recordings.indexOf(recording)
            recordings.remove(recording)
            notifyItemRemoved(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingHolder {
            val layoutInflater = LayoutInflater.from(mainActivity)
            return RecordingHolder(layoutInflater, parent)
        }

        fun getItem(position: Int): Recording {
            return recordings[position]
        }

        override fun onBindViewHolder(holder: RecordingHolder, position: Int) {
            val recording = recordings[position]
            val adornRes: Int = when (recording.format) {
                Recorder.WAV_FORMAT -> if (mainActivity!!.settledTheme == BaseActivity.LIGHT_THEME) R.drawable.sound_symbol_wav_light else R.drawable.sound_symbol_wav_dark
                Recorder.AAC_HIGH_FORMAT -> if (mainActivity!!.settledTheme == BaseActivity.LIGHT_THEME) R.drawable.sound_symbol_aac128_light else R.drawable.sound_symbol_aac128_dark
                Recorder.AAC_BASIC_FORMAT -> if (mainActivity!!.settledTheme == BaseActivity.LIGHT_THEME) R.drawable.sound_symbol_aac32_light else R.drawable.sound_symbol_aac32_dark
                else -> if (mainActivity!!.settledTheme == BaseActivity.LIGHT_THEME) R.drawable.sound_symbol_aac64_light else R.drawable.sound_symbol_aac64_dark
            }

            for (i in contactList!!.indices) {
                if (contactList!![i]!!
                        .daytime.equals(recording.dateRecord, ignoreCase = true)
                ) {
                    if (contactList!![i]!!
                            .isMissed
                    ) holder.title.text =
                        getString(R.string.missed_by) + " " + contactList!![i]!!.phoneNumber else holder.title.text =
                        contactList!![i]!!.phoneNumber
                }
            }

            if (mainViewModel.contact.value == null || !mainViewModel.contact.value!!
                    .isPrivateNumber
            ) holder.recordingType.setImageResource(if (recording.isIncoming) R.drawable.incoming else if (mainActivity!!.settledTheme == BaseActivity.LIGHT_THEME) R.drawable.outgoing_light else R.drawable.outgoing_dark)
            holder.recordingAdorn.setImageResource(adornRes)
            holder.checkBox.setOnClickListener { view: View ->
                manageSelectRecording(
                    view,
                    position,
                    recording.exists()
                )
            }

            holder.recordingShare.setOnClickListener {
                mainViewModel.shareRecording(recording.path, context)
            }

            if (!recording.exists()) mainViewModel.markNonexistent(holder, mainActivity!!)
            mainViewModel.modifyMargins(holder.itemView, requireContext(), selectMode)
            if (selectedItems!!.contains(position)) mainViewModel.selectRecording(holder.itemView) else mainViewModel.deselectRecording(
                holder.itemView
            )
        }

        override fun onViewRecycled(holder: RecordingHolder) {
            super.onViewRecycled(holder)
            mainViewModel.unMarkNonexistent(holder)
        }

        override fun getItemCount(): Int {
            return recordings.size
        }
    }

    protected val callDetails: List<Contact>
        get() = try {
            val contactList: MutableList<Contact> = ArrayList()
            val managedCursor =
                requireContext().contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
                )
            val number = managedCursor!!.getColumnIndex(CallLog.Calls.NUMBER)
            val name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val type = managedCursor.getColumnIndex(CallLog.Calls.TYPE)
            val date = managedCursor.getColumnIndex(CallLog.Calls.DATE)
            val duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION)

            while (managedCursor.moveToNext()) {
                val phNumber = managedCursor.getString(number) // mobile number
                val phName = managedCursor.getString(name) // name
                val callType = managedCursor.getString(type) // call type
                val callDate = managedCursor.getString(date) // call date
                val callDayTime = Date(java.lang.Long.valueOf(callDate))
                val callDuration = managedCursor.getString(duration)
                var dir: String? = null

                when (callType.toInt()) {
                    CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                    CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                    CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
                }

                var value: String?
                value = phName
                if (phName == null || phName.isEmpty())
                    value = phNumber
                val contact = Contact()
                if (dir != null && dir.equals("MISSED", ignoreCase = true)) contact.isMissed = true
                contact.phoneNumber = value
                contact.contactName = callDuration
                contact.daytime =
                    SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(callDayTime)
                contactList.add(contact)
            }
            managedCursor.close()

            contactList
        } catch (ex: Exception) {
            ex.printStackTrace()
            ArrayList()
        }

}