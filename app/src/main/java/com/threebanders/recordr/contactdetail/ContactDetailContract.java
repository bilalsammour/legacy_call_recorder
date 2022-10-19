/*
 * Copyright (C) 2019 Eugen Rădulescu <synapticwebb@gmail.com> - All rights reserved.
 *
 * You may use, distribute and modify this code only under the conditions
 * stated in the SW Call Recorder license. You should have received a copy of the
 * SW Call Recorder license along with this file. If not, please write to <synapticwebb@gmail.com>.
 */

package com.threebanders.recordr.contactdetail;

import android.app.Activity;
import android.content.Context;

import com.threebanders.recordr.Util.DialogInfo;
import com.threebanders.recordr.data.Contact;
import com.threebanders.recordr.data.Recording;
import java.util.List;

public interface ContactDetailContract {
    interface View {
        void setContact(Contact contact);
        void paintViews(List<Recording> recordings);
        boolean isInvalid();
        void setInvalid(boolean invalid);
        void removeRecording(Recording recording);
        Context getContext();
    }

    interface Presenter {
        void loadRecordings(Contact contact);
        DialogInfo deleteRecordings(List<Recording> recordings);
        DialogInfo renameRecording(CharSequence input, Recording recording);
        void moveSelectedRecordings(String path, int totalsize, Activity parentActivity, Recording[] recordings);
    }
}
