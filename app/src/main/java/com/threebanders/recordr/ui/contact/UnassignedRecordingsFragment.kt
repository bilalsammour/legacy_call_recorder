package com.threebanders.recordr.ui.contact

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Constants
import com.threebanders.recordr.ui.BaseActivity.LayoutType
import com.threebanders.recordr.ui.settings.SettingsFragment.Companion.GOOGLE_DRIVE
import core.threebanders.recordr.Cache
import core.threebanders.recordr.data.Recording
import core.threebanders.recordr.recorder.Recorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Type

class UnassignedRecordingsFragment : ContactDetailFragment() {
    private lateinit var rootView: View
    private var record: Recorder? = null
    private var sharedPref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var file: File? = null
    private var fileName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(
            R.layout.unassigned_recordings_fragment,
            container, false
        )

        recordingsRecycler = rootView.findViewById(R.id.unassigned_recordings)
        recordingsRecycler!!.layoutManager = LinearLayoutManager(
            baseActivity
        )
        recordingsRecycler?.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        recordingsRecycler?.adapter = adapter
        record = Recorder(context)
        sharedPref = context?.getSharedPreferences(Cache.RECORDINGS_LIST, Context.MODE_PRIVATE)
        editor = sharedPref?.edit()


        lifecycleScope.launch {

            mainViewModel.loadRecordings()

            mainViewModel.records.observe(viewLifecycleOwner) { recordings: List<Recording?>? ->
                paintViews()

                if (recordings?.size != 0) {

                    var list = getDataFromSharedPreferences()
                    if (list == null)
                        list = arrayListOf()

                    getDataFromSharedPreferences()

                    val settings = baseActivity?.prefs
                    val isGoogleDriveSynced = settings?.getBoolean(GOOGLE_DRIVE, false)

                    if (isGoogleDriveSynced!! && list.size != recordings?.size) {
                        file = File(recordings?.get(0)?.path.toString())
                        fileName = recordings?.get(0)?.dateRecord.toString()

                        uploadFileToGDrive(File(recordings?.get(0)?.path.toString()))
                    }

                    setDataFromSharedPreferences(recordings as List<Recording?>)
                }
            }
        }
        paintViews()
        mainViewModel.deletedRecording.observe(viewLifecycleOwner) { removeRecording() }
        removeRecording()

        return rootView
    }

    private fun uploadFileToGDrive(file: File) {
        lifecycleScope.launch {
            try {
                val drive = getDriveService()

                var folderId = ""
                withContext(Dispatchers.IO) {
                    val gFolder = com.google.api.services.drive.model.File()
                    gFolder.name = Constants.APP_NAME
                    gFolder.mimeType = "application/vnd.google-apps.folder"

                    launch {
                        val fileList = drive?.Files()?.list()
                            ?.setQ("mimeType='application/vnd.google-apps.folder' and trashed=false and name='$Constants.APP_NAME'")
                            ?.execute()

                        folderId = if (fileList?.files?.isEmpty() == true) {
                            val folder = drive.Files().create(gFolder)?.setFields("id")?.execute()
                            folder?.id ?: ""
                        } else {
                            fileList?.files?.get(0)?.id ?: ""
                        }
                    }
                }.join()

                withContext(Dispatchers.IO) {
                    launch {
                        val gFile = com.google.api.services.drive.model.File()
                        gFile.name = file.name
                        gFile.parents = mutableListOf(folderId)
                        val fileContent = FileContent("audio/wav", file)
                        drive?.Files()?.create(gFile, fileContent)?.setFields("id, parents")
                            ?.execute()
                    }
                }.key

            } catch (userAuthEx: UserRecoverableAuthIOException) {
                startActivity(
                    userAuthEx.intent
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    private fun getDriveService(): Drive? {
        GoogleSignIn.getLastSignedInAccount(requireContext())?.let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
                requireContext(), listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount.account!!
            return Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(getString(R.string.app_name))
                .build()
        }
        return null
    }

    private fun getDataFromSharedPreferences(): List<Recording?>? {
        val gson = Gson()
        val productFromShared: List<Recording?>?
        val sharedPref: SharedPreferences? =
            context?.getSharedPreferences("PREFS_TAG", Context.MODE_PRIVATE)
        val jsonPreferences = sharedPref?.getString("PRODUCT_TAG", "")
        val type: Type = object : TypeToken<List<Recording?>?>() {}.type
        productFromShared = gson.fromJson<List<Recording?>>(jsonPreferences, type)
        return productFromShared
    }

    private fun setDataFromSharedPreferences(curProduct: List<Recording?>) {
        val gson = Gson()
        val jsonCurProduct = gson.toJson(curProduct)
        val sharedPref: SharedPreferences? =
            context?.getSharedPreferences("PREFS_TAG", Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.putString("PRODUCT_TAG", jsonCurProduct)
        editor?.apply()
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
        val moveBtn = baseActivity?.findViewById<ImageButton>(R.id.actionbar_select_move)
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
        if (selectMode) showView(moveBtn, animate) else hideView(moveBtn, animate)
        if (selectMode) {
            if (checkIfSelectedRecordingsDeleted()) disableMoveBtn() else enableMoveBtn()
        }
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
                    R.id.rename_recording -> {
                        onRenameRecording()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.delete_recording -> {
                        onDeleteSelectedRecordings()
                        return@setOnMenuItemClickListener true
                    }
                    else -> return@setOnMenuItemClickListener false
                }
            }
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.recording_selected_popup, popupMenu.menu)
            val renameMenuItem = popupMenu.menu.findItem(R.id.rename_recording)
            val recording = (recordingsRecycler!!.adapter as RecordingAdapter?)!!.getItem(
                selectedItems!![0]
            )
            if (selectedItems!!.size > 1 || !recording!!.exists()) renameMenuItem.isEnabled =
                false
            popupMenu.show()
        }
        val moveBtn = baseActivity!!.findViewById<ImageButton>(R.id.actionbar_select_move)
        registerForContextMenu(moveBtn)

        moveBtn.setOnClickListener { obj: View -> obj.showContextMenu() }
        val selectAllBtn =
            baseActivity?.findViewById<ImageButton>(R.id.actionbar_select_all)
        selectAllBtn!!.setOnClickListener { onSelectAll() }
        val infoBtn = baseActivity?.findViewById<ImageButton>(R.id.actionbar_info)
        infoBtn!!.setOnClickListener { onRecordingInfo() }
    }
}