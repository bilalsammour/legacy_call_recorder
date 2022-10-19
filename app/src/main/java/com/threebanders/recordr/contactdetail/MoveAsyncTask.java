/*
 * Copyright (C) 2019 Eugen Rădulescu <synapticwebb@gmail.com> - All rights reserved.
 *
 * You may use, distribute and modify this code only under the conditions
 * stated in the SW Call Recorder license. You should have received a copy of the
 * SW Call Recorder license along with this file. If not, please write to <synapticwebb@gmail.com>.
 */

package com.threebanders.recordr.contactdetail;

import android.app.Activity;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.threebanders.recordr.CrLog;

import com.threebanders.recordr.R;
import com.threebanders.recordr.data.Recording;
import com.threebanders.recordr.data.Repository;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

public class MoveAsyncTask extends AsyncTask<Recording, Integer, Boolean> {
    public long alreadyCopied = 0;
    private String path;
    private long totalSize;
    private MaterialDialog dialog;
    private Repository repository;
    private WeakReference<Activity> activityRef; //http://sohailaziz05.blogspot.com/2014/10/asynctask-and-context-leaking.html

    MoveAsyncTask(Repository repository, String folderPath, long totalSize, Activity activity) {
        this.path = folderPath;
        this.totalSize = totalSize;
        this.repository = repository;
        activityRef = new WeakReference<>(activity);
    }

    public void callPublishProgress(int progress) {
        publishProgress(progress);
    }

    @Override
    protected void onPreExecute() {
        dialog = new MaterialDialog.Builder(activityRef.get())
                .title(R.string.progress_title)
                .content(R.string.progress_text)
                .progress(false, 100, true)
                .negativeText("Cancel")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        cancel(true);
                    }
                })
                .build();
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer...integers) {
        dialog.setProgress(integers[0]);
    }

    @Override
    protected void onCancelled() {
        new MaterialDialog.Builder(activityRef.get())
                .title(R.string.warning_title)
                .content(R.string.canceled_move)
                .positiveText("OK")
                .icon(activityRef.get().getResources().getDrawable(R.drawable.warning))
                .show();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dialog.dismiss();
        if(result) {
            new MaterialDialog.Builder(activityRef.get())
                    .title(R.string.success_move_title)
                    .content(R.string.success_move_text)
                    .positiveText("OK")
                    .icon(activityRef.get().getResources().getDrawable(R.drawable.success))
                    .show();
        }
        else {
            new MaterialDialog.Builder(activityRef.get())
                    .title(R.string.error_title)
                    .content(R.string.error_move)
                    .positiveText("OK")
                    .icon(activityRef.get().getResources().getDrawable(R.drawable.error))
                    .show();
        }
    }

    @Override
    protected Boolean doInBackground(Recording...recordings) {
        for(Recording recording : recordings) {
            try {
                recording.move(repository, path, this, totalSize);
                if(isCancelled())
                    break;
            }
            catch (Exception exc) {
                CrLog.log(CrLog.ERROR, "Error moving the recording(s): " + exc.getMessage());
                return false;
            }
        }
        return true;
    }
}