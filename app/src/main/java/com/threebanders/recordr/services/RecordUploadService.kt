package com.threebanders.recordr.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.threebanders.recordr.R
import com.threebanders.recordr.common.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RecordUploadService : Service() {

    private val FOLDER_NAME = Constants.APP_NAME
    override fun onBind(intent: Intent?): IBinder?  = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val recording = intent?.getStringExtra("recording")
        recording?.let { File(it) }?.let { uploadFileToGDrive(it) }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun uploadFileToGDrive(file: File) {
        try {
          CoroutineScope(Dispatchers.Main).launch{
              val drive = getDriveService()
              var folderId = ""
              withContext(Dispatchers.IO) {
                  val gFolder = com.google.api.services.drive.model.File()
                  gFolder.name = FOLDER_NAME
                  gFolder.mimeType = "application/vnd.google-apps.folder"

                  launch {
                      val fileList = drive?.Files()?.list()
                          ?.setQ("mimeType='application/vnd.google-apps.folder' and trashed=false and name='$FOLDER_NAME'")
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
          }
        }
        catch (userAuthEx: UserRecoverableAuthIOException) {
            startActivity(
                userAuthEx.intent
            )
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun getDriveService(): Drive? {
        GoogleSignIn.getLastSignedInAccount(this)?.let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
               this, listOf(DriveScopes.DRIVE_FILE)
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
}