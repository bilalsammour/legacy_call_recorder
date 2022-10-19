/*
 * Copyright (C) 2019 Eugen Rădulescu <synapticwebb@gmail.com> - All rights reserved.
 *
 * You may use, distribute and modify this code only under the conditions
 * stated in the SW Call Recorder license. You should have received a copy of the
 * SW Call Recorder license along with this file. If not, please write to <synapticwebb@gmail.com>.
 */

package com.threebanders.recordr.contactdetail;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.provider.CallLog;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.codekidlabs.storagechooser.Content;
import com.codekidlabs.storagechooser.StorageChooser;
import com.threebanders.recordr.CrApp;
import com.threebanders.recordr.CrLog;

import com.threebanders.recordr.BaseActivity;
import com.threebanders.recordr.BaseActivity.LayoutType;
import com.threebanders.recordr.R;
import com.threebanders.recordr.Util;
import com.threebanders.recordr.contactdetail.di.ViewModule;
import com.threebanders.recordr.contactslist.ContactsListActivityMain;
import com.threebanders.recordr.data.Contact;
import com.threebanders.recordr.data.Recording;
import com.threebanders.recordr.player.PlayerActivity;
import com.threebanders.recordr.recorder.Recorder;
import com.threebanders.recordr.contactdetail.ContactDetailContract.Presenter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.threebanders.recordr.contactslist.ContactsListActivityMain.PERMS_NOT_GRANTED;
import static com.threebanders.recordr.contactslist.ContactsListFragment.ARG_CONTACT;

import com.threebanders.recordr.Util.DialogInfo;


import javax.inject.Inject;

public class ContactDetailFragment extends Fragment implements ContactDetailContract.View {
    @Inject
    Presenter presenter;
    protected RecordingAdapter adapter;
    protected RecyclerView recordingsRecycler;
    private RelativeLayout detailView;
    protected Contact contact;
    protected boolean selectMode = false;
    protected List<Integer> selectedItems = new ArrayList<>();
    protected BaseActivity parentActivity;
    /**
     * Dacă există cel puțin un recording lipsă pe disc printre cele selectate, butonul de move se dezactivează.
     * Cind sunt 0 recorduri lispă se reactivează.
     */
    private int selectedItemsDeleted = 0;
    /**
     * Un flag care este setat cînd un recording este șters. Semnifică faptul că fragmentul detaliu curent
     * nu mai este valabil și trebuie înlocuit.TODO: de testat ce se întîmplă cînd se adaugă recording.
     */
    private boolean invalid = false;
    private static final String SELECT_MODE_KEY = "select_mode_key";
    private static final String SELECTED_ITEMS_KEY = "selected_items_key";
    protected static final int REQUEST_PICK_NUMBER = 2;
    private static final int EFFECT_TIME = 250;
    static final String EDIT_EXTRA_CONTACT = "edit_extra_contact";
    private static final int REQUEST_EDIT = 1;
    public static final String RECORDING_EXTRA = "recording_extra";

    @Nullable
    @Override
    public Context getContext() {
        return super.getContext();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new RecordingAdapter(new ArrayList<>(0));
        Bundle args = getArguments();
        if (args != null)
            contact = args.getParcelable(ARG_CONTACT);

        if (savedInstanceState != null) {
            selectMode = savedInstanceState.getBoolean(SELECT_MODE_KEY);
            selectedItems = savedInstanceState.getIntegerArrayList(SELECTED_ITEMS_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        detailView = (RelativeLayout) inflater.inflate(R.layout.contact_detail_fragment, container, false);
        recordingsRecycler = detailView.findViewById(R.id.recordings);
        recordingsRecycler.setLayoutManager(new LinearLayoutManager(parentActivity));
        recordingsRecycler.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        recordingsRecycler.setAdapter(adapter);

        return detailView;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadRecordings(contact);
    }

    protected void onDeleteSelectedRecordings() {
        new MaterialDialog.Builder(parentActivity)
                .title(R.string.delete_recording_confirm_title)
                .content(String.format(getResources().getString(
                                R.string.delete_recording_confirm_message),
                        selectedItems.size()))
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .icon(parentActivity.getResources().getDrawable(R.drawable.warning))
                .onPositive((@NonNull MaterialDialog dialog,
                             @NonNull DialogAction which) -> {

                    Util.DialogInfo result = presenter.deleteRecordings(getSelectedRecordings());
                    if (result != null)
                        new MaterialDialog.Builder(parentActivity)
                                .title(result.title)
                                .content(result.message)
                                .icon(getResources().getDrawable(result.icon))
                                .positiveText(android.R.string.ok)
                                .show();
                    else {
                        if (adapter.getItemCount() == 0) {
                            View noContent = parentActivity.findViewById(R.id.no_content_detail);
                            if (noContent != null)
                                noContent.setVisibility(View.VISIBLE);
                        }
                        clearSelectMode();
                    }
                })
                .show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        parentActivity = (BaseActivity) context;
        CrApp application = (CrApp) parentActivity.getApplication();
        ViewModule viewModule = new ViewModule(this);
        application.appComponent.contactDetailComponent().create(viewModule).inject(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parentActivity = null;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setDetailsButtonsListeners();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(SELECT_MODE_KEY, selectMode);
        outState.putIntegerArrayList(SELECTED_ITEMS_KEY, (ArrayList<Integer>) selectedItems);
    }

    @Override
    public void setContact(Contact contact) {
        this.contact = contact;
    }

    @Override
    public void paintViews(List<Recording> recordings) {
        adapter.replaceData(recordings, getCallDetails());
        if (selectMode)
            putInSelectMode(false);
        else
            toggleSelectModeActionBar(false);


        TextView noContent = detailView.findViewById(R.id.no_content_detail);
        if (recordings.size() > 0)
            noContent.setVisibility(View.GONE);
        else
            noContent.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isInvalid() {
        return invalid;
    }

    @Override
    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    @Override
    public void removeRecording(Recording recording) {
        adapter.removeItem(recording);
    }

    protected void putInSelectMode(boolean animate) {
        selectMode = true;
        toggleSelectModeActionBar(animate);
        redrawRecordings();
    }

    protected void toggleSelectModeActionBar(boolean animate) {
        ImageButton navigateBackBtn = parentActivity.findViewById(R.id.navigate_back);
        ImageButton closeBtn = parentActivity.findViewById(R.id.close_select_mode);
        ImageButton moveBtn = parentActivity.findViewById(R.id.actionbar_select_move);
        ImageButton selectAllBtn = parentActivity.findViewById(R.id.actionbar_select_all);
        ImageButton infoBtn = parentActivity.findViewById(R.id.actionbar_info);
        ImageButton menuRightBtn = parentActivity.findViewById(R.id.contact_detail_menu);
        ImageButton menuRightSelectedBtn = parentActivity.findViewById(R.id.contact_detail_selected_menu);

        toggleTitle();
        if (parentActivity.getLayoutType() == LayoutType.SINGLE_PANE)
            if (selectMode) hideView(navigateBackBtn, animate);
            else showView(navigateBackBtn, animate);

        if (selectMode) showView(closeBtn, animate);
        else hideView(closeBtn, animate);

        if (selectMode) showView(moveBtn, animate);
        else hideView(moveBtn, animate);
        if (selectMode) {
            if (checkIfSelectedRecordingsDeleted())
                disableMoveBtn();
            else
                enableMoveBtn();
        }
        if (selectMode) showView(selectAllBtn, animate);
        else hideView(selectAllBtn, animate);
        if (selectMode) showView(infoBtn, animate);
        else hideView(infoBtn, animate);
        if (selectMode) showView(menuRightSelectedBtn, animate);
        else hideView(menuRightSelectedBtn, animate);
        if (selectMode) hideView(menuRightBtn, animate);
        else showView(menuRightBtn, animate);

        if (parentActivity.getLayoutType() == LayoutType.DOUBLE_PANE) {
            ImageButton hamburger = parentActivity.findViewById(R.id.hamburger);
            if (selectMode) hideView(hamburger, animate);
            else showView(hamburger, animate);
        }
    }

    protected void toggleTitle() {
        TextView title = parentActivity.findViewById(R.id.actionbar_title);
        if (parentActivity.getLayoutType() == LayoutType.DOUBLE_PANE) {
            Toolbar.LayoutParams params = (Toolbar.LayoutParams) title.getLayoutParams();
            params.gravity = selectMode ? Gravity.START : Gravity.CENTER;
            title.setLayoutParams(params);
        }

        if (selectMode)
            title.setText(String.valueOf(selectedItems.size()));
        else {
            if (parentActivity.getLayoutType() == LayoutType.SINGLE_PANE)
                title.setText(contact.getContactName());
            else
                title.setText(R.string.app_name);
        }
    }


    private void fadeEffect(View view, float finalAlpha, int finalVisibility) {
        view.animate()
                .alpha(finalAlpha)
                .setDuration(EFFECT_TIME)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        view.setVisibility(finalVisibility);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
    }

    protected void hideView(View view, boolean animate) {
        if (animate)
            fadeEffect(view, 0.0f, View.GONE);
        else {
            view.setAlpha(0.0f); //poate lipsi?
            view.setVisibility(View.GONE);
        }
    }

    protected void showView(View view, boolean animate) {
        if (animate)
            fadeEffect(view, 1f, View.VISIBLE);
        else {
            view.setAlpha(1f); //poate lipsi?
            view.setVisibility(View.VISIBLE);
        }
    }

    protected void clearSelectMode() {
        selectMode = false;
        toggleSelectModeActionBar(true);
        redrawRecordings();
        selectedItems.clear();
    }


    private void modifyMargins(View recording) {
        CheckBox checkBox = recording.findViewById(R.id.recording_checkbox);
        Resources res = getContext().getResources();
        checkBox.setVisibility((selectMode ? View.VISIBLE : View.GONE));
        RelativeLayout.LayoutParams lpCheckBox = (RelativeLayout.LayoutParams) checkBox.getLayoutParams();
        lpCheckBox.setMarginStart(selectMode ?
                (int) res.getDimension(R.dimen.recording_checkbox_visible_start_margin) :
                (int) res.getDimension(R.dimen.recording_checkbox_gone_start_margin));
        checkBox.setLayoutParams(lpCheckBox);

        ImageView recordingAdorn = recording.findViewById(R.id.recording_adorn);
        RelativeLayout.LayoutParams lpRecAdorn = (RelativeLayout.LayoutParams) recordingAdorn.getLayoutParams();
        lpRecAdorn.setMarginStart(selectMode ?
                (int) res.getDimension(R.dimen.recording_adorn_selected_margin_start) :
                (int) res.getDimension(R.dimen.recording_adorn_unselected_margin_start));
        recordingAdorn.setLayoutParams(lpRecAdorn);

        TextView title = recording.findViewById(R.id.recording_title);
        RelativeLayout.LayoutParams lpTitle = (RelativeLayout.LayoutParams) title.getLayoutParams();
        lpTitle.setMarginStart(selectMode ?
                (int) res.getDimension(R.dimen.recording_title_selected_margin_start) :
                (int) res.getDimension(R.dimen.recording_title_unselected_margin_start));
        title.setLayoutParams(lpTitle);
    }

    private void selectRecording(@NonNull android.view.View recording) {
        CheckBox checkBox = recording.findViewById(R.id.recording_checkbox);
        checkBox.setChecked(true);
    }

    private void deselectRecording(View recording) {
        CheckBox checkBox = recording.findViewById(R.id.recording_checkbox);
        checkBox.setChecked(false);
    }

    protected void enableMoveBtn() {
        ImageButton moveBtn = parentActivity.findViewById(R.id.actionbar_select_move);
        moveBtn.setEnabled(true);
        moveBtn.setImageAlpha(255);
    }

    protected void disableMoveBtn() {
        ImageButton moveBtn = parentActivity.findViewById(R.id.actionbar_select_move);
        moveBtn.setEnabled(false);
        moveBtn.setImageAlpha(75);
    }


    private void redrawRecordings() {
        for (int i = 0; i < adapter.getItemCount(); ++i)
            adapter.notifyItemChanged(i);
    }


    private void manageSelectRecording(View recording, int adapterPosition, boolean exists) {
        if (!removeIfPresentInSelectedItems(adapterPosition)) {
            selectedItems.add(adapterPosition);
            selectRecording(recording);
            if (!exists) {
                selectedItemsDeleted++;
                disableMoveBtn();
            }
        } else {
            deselectRecording(recording);
            if (!exists)
                selectedItemsDeleted--;
            if (selectedItemsDeleted == 0)
                enableMoveBtn();
        }

        if (selectedItems.isEmpty())
            clearSelectMode();
        else
            toggleTitle();
    }


    private List<Recording> getSelectedRecordings() {
        List<Recording> list = new ArrayList<>();
        for (int adapterPosition : selectedItems)
            list.add(adapter.getItem(adapterPosition));
        return list;
    }


    private boolean removeIfPresentInSelectedItems(int adapterPosition) {
        if (selectedItems.contains(adapterPosition)) {
            selectedItems.remove((Integer) adapterPosition); //fără casting îl interpretează ca poziție
            //în selectedItems
            return true;
        } else
            return false;
    }

    protected boolean checkIfSelectedRecordingsDeleted() {
        for (Recording recording : getSelectedRecordings())
            if (!recording.exists())
                return true;
        return false;
    }


    public static ContactDetailFragment newInstance(Contact contact) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_CONTACT, contact);
        ContactDetailFragment fragment = new ContactDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private void onShowStorageInfo() {
        long sizePrivate = 0, sizePublic = 0;
        for (Recording recording : adapter.getRecordings()) {
            long size = new File(recording.getPath()).length();
            if (recording.isSavedInPrivateSpace(parentActivity))
                sizePrivate += size;
            else
                sizePublic += size;
        }

        MaterialDialog dialog = new MaterialDialog.Builder(parentActivity)
                .title(R.string.storage_info)
                .customView(R.layout.info_storage_dialog, false)
                .positiveText(android.R.string.ok).build();
        TextView privateStorage = dialog.getView().findViewById(R.id.info_storage_private_data);
        privateStorage.setText(Util.getFileSizeHuman(sizePrivate));

        TextView publicStorage = dialog.getView().findViewById(R.id.info_storage_public_data);
        publicStorage.setText(Util.getFileSizeHuman(sizePublic));

        dialog.show();
    }

    protected void onRenameRecording() {
        new MaterialDialog.Builder(parentActivity)
                .title(R.string.rename_recording_title)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(parentActivity.getResources().getString(R.string.rename_recording_input_text),
                        null, false, (@NonNull MaterialDialog dialog, CharSequence input) -> {
                            if (selectedItems.size() != 1) {
                                CrLog.log(CrLog.WARN, "Calling onRenameClick when multiple recordings are selected");
                                return;
                            }
                            DialogInfo result = presenter.renameRecording(input, getSelectedRecordings().get(0));
                            if (result != null)
                                new MaterialDialog.Builder(parentActivity)
                                        .title(result.title)
                                        .content(result.message)
                                        .icon(getResources().getDrawable(result.icon))
                                        .positiveText(android.R.string.ok)
                                        .show();
                            else
                                adapter.notifyItemChanged(selectedItems.get(0));
                        }
                ).show();
    }


    protected void onSelectAll() {
        List<Integer> notSelected = new ArrayList<>();
        for (int i = 0; i < adapter.getItemCount(); ++i)
            notSelected.add(i);
        notSelected.removeAll(selectedItems);

        for (int position : notSelected) {
            selectedItems.add(position);
            adapter.notifyItemChanged(position);
            //https://stackoverflow.com/questions/33784369/recyclerview-get-view-at-particular-position
            View selectedRecording = recordingsRecycler.getLayoutManager().findViewByPosition(position);
            if (selectedRecording != null) //dacă recordingul nu este încă afișat pe ecran
                // (sunt multe recordinguri și se scrolează) atunci selectedRecording va fi null. Dar mai înainte am
                //notificat adapterul că s-a schimbat, ca să îl reconstruiască.
                selectRecording(selectedRecording);
        }
        toggleTitle();
    }

    protected void onRecordingInfo() {
        if (selectedItems.size() > 1) {
            long totalSize = 0;
            for (int position : selectedItems) {
                Recording recording = adapter.getItem(position);
                totalSize += recording.getSize();
            }
            new MaterialDialog.Builder(parentActivity)
                    .title(R.string.recordings_info_title)
                    .content(String.format(parentActivity.getResources().getString(R.string.recordings_info_text), Util.getFileSizeHuman(totalSize)))
                    .positiveText(android.R.string.ok)
                    .show();
            return;
        }
        MaterialDialog dialog = new MaterialDialog.Builder(parentActivity)
                .title(R.string.recording_info_title)
                .customView(R.layout.info_dialog, false)
                .positiveText(android.R.string.ok).build();

        //There should be only one if we are here:
        if (selectedItems.size() != 1) {
            CrLog.log(CrLog.WARN, "Calling onInfoClick when multiple recordings are selected");
            return;
        }

        Recording recording = adapter.getItem(selectedItems.get(0));
        TextView date = dialog.getView().findViewById(R.id.info_date_data);
        date.setText(String.format("%s %s", recording.getDate(), recording.getTime()));
        TextView size = dialog.getView().findViewById(R.id.info_size_data);
        size.setText(Util.getFileSizeHuman(recording.getSize()));
        TextView source = dialog.getView().findViewById(R.id.info_source_data);
        source.setText(recording.getSource());

        TextView format = dialog.getView().findViewById(R.id.info_format_data);
        format.setText(recording.getHumanReadingFormat(parentActivity));
        TextView length = dialog.getView().findViewById(R.id.info_length_data);
        length.setText(Util.getDurationHuman(recording.getLength(), true));
        TextView path = dialog.getView().findViewById(R.id.info_path_data);
        path.setText(recording.isSavedInPrivateSpace(parentActivity) ? parentActivity.getResources().
                getString(R.string.private_storage) : recording.getPath());
        if (!recording.exists()) {
            path.setText(String.format("%s%s", path.getText(), parentActivity.getResources().
                    getString(R.string.nonexistent_file)));
            path.setTextColor(parentActivity.getResources().getColor(android.R.color.holo_red_light));
        }
        dialog.show();
    }

    private void onMoveSelectedRecordings(String path) {
        int totalSize = 0;
        List<Recording> recordings = getSelectedRecordings();
        Recording[] recordingsArray = new Recording[recordings.size()];

        for (Recording recording : recordings) {
            if (new File(recording.getPath()).getParent().equals(path)) {
                new MaterialDialog.Builder(parentActivity)
                        .title(R.string.information_title)
                        .content(R.string.move_destination_same)
                        .positiveText("OK")
                        .icon(getResources().getDrawable(R.drawable.info))
                        .show();
                return;
            }
            totalSize += new File(recording.getPath()).length();
        }

        presenter.moveSelectedRecordings(path, totalSize, parentActivity, recordings.toArray(recordingsArray));
    }

    protected void setDetailsButtonsListeners() {
        ImageButton navigateBack = parentActivity.findViewById(R.id.navigate_back);
        navigateBack.setOnClickListener((view) ->
                NavUtils.navigateUpFromSameTask(parentActivity)
        );
        final ImageButton menuButtonSelectOff = parentActivity.findViewById(R.id.contact_detail_menu);
        menuButtonSelectOff.setOnClickListener((view) -> {
            PopupMenu popupMenu = new PopupMenu(parentActivity, view);
            popupMenu.setOnMenuItemClickListener((item) -> {
                switch (item.getItemId()) {
                    case R.id.storage_info:
                        onShowStorageInfo();
                    default:
                        return false;
                }
            });
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.contact_popup, popupMenu.getMenu());
            popupMenu.show();
        });


        ImageButton closeBtn = parentActivity.findViewById(R.id.close_select_mode);
        closeBtn.setOnClickListener((View v) -> clearSelectMode());

        final ImageButton menuButtonSelectOn = parentActivity.findViewById(R.id.contact_detail_selected_menu);
        menuButtonSelectOn.setOnClickListener((View view) -> {
                    PopupMenu popupMenu = new PopupMenu(parentActivity, view);
                    popupMenu.setOnMenuItemClickListener((MenuItem item) -> {
                        switch (item.getItemId()) {
                            case R.id.rename_recording:
                                onRenameRecording();
                                return true;
                            default:
                                return false;
                        }
                    });

                    MenuInflater inflater = popupMenu.getMenuInflater();
                    inflater.inflate(R.menu.recording_selected_popup, popupMenu.getMenu());
                    MenuItem renameMenuItem = popupMenu.getMenu().findItem(R.id.rename_recording);
                    Recording recording = ((RecordingAdapter) recordingsRecycler.getAdapter()).
                            getItem(selectedItems.get(0));
                    if (selectedItems.size() > 1 || !recording.exists())
                        renameMenuItem.setEnabled(false);
                    popupMenu.show();
                }
        );

        ImageButton moveBtn = parentActivity.findViewById(R.id.actionbar_select_move);
        registerForContextMenu(moveBtn);
        //foarte necesar. Altfel meniul contextual va fi arătat numai la long click.
        moveBtn.setOnClickListener(View::showContextMenu);

        ImageButton selectAllBtn = parentActivity.findViewById(R.id.actionbar_select_all);
        selectAllBtn.setOnClickListener((View v) -> onSelectAll());
        ImageButton deleteBtn = parentActivity.findViewById(R.id.delete_recording);
        deleteBtn.setOnClickListener((View v) -> onDeleteSelectedRecordings());

        ImageButton infoBtn = parentActivity.findViewById(R.id.actionbar_info);
        infoBtn.setOnClickListener((View view) -> onRecordingInfo());
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = parentActivity.getMenuInflater();
        inflater.inflate(R.menu.storage_chooser_options, menu);

        boolean allowMovePrivate = true;
        for (Recording recording : getSelectedRecordings())
            if (recording.isSavedInPrivateSpace(parentActivity)) {
                allowMovePrivate = false;
                break;
            }
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            int end = spanString.length();
            spanString.setSpan(new RelativeSizeSpan(0.87f), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            item.setTitle(spanString);
        }

        MenuItem menuItem = menu.getItem(0);
        menuItem.setEnabled(allowMovePrivate);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.private_storage:
                onMoveSelectedRecordings(parentActivity.
                        getFilesDir().getAbsolutePath());
                return true;
            case R.id.public_storage:
                Content content = new Content();
                content.setOverviewHeading(parentActivity.getResources().getString(R.string.move_heading));
                StorageChooser.Theme theme = new StorageChooser.Theme(parentActivity);
                theme.setScheme(parentActivity.getSettedTheme().equals(BaseActivity.LIGHT_THEME) ?
                        parentActivity.getResources().getIntArray(R.array.storage_chooser_theme_light) :
                        parentActivity.getResources().getIntArray(R.array.storage_chooser_theme_dark));

                StorageChooser chooser = new StorageChooser.Builder()
                        .withActivity(parentActivity)
                        .withFragmentManager(parentActivity.getFragmentManager())
                        .allowCustomPath(true)
                        .setType(StorageChooser.DIRECTORY_CHOOSER)
                        .withMemoryBar(true)
                        .allowAddFolder(true)
                        .showHidden(true)
                        .withContent(content)
                        .setTheme(theme)
                        .build();

                chooser.show();

                chooser.setOnSelectListener(this::onMoveSelectedRecordings);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    class RecordingHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView title;
        ImageView recordingType, recordingAdorn, exclamation;
        CheckBox checkBox;

        RecordingHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.recording, parent, false));
            recordingType = itemView.findViewById(R.id.recording_type);
            title = itemView.findViewById(R.id.recording_title);
            checkBox = itemView.findViewById(R.id.recording_checkbox);
            recordingAdorn = itemView.findViewById(R.id.recording_adorn);
            exclamation = itemView.findViewById(R.id.recording_exclamation);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            if (!selectMode)
                putInSelectMode(true);
            Recording recording = adapter.getItem(getAdapterPosition());
            manageSelectRecording(v, this.getAdapterPosition(), recording.exists());
            return true;
        }

        @Override
        public void onClick(View v) {
            Recording recording = adapter.getItem(getAdapterPosition());
            if (selectMode)
                manageSelectRecording(v, this.getAdapterPosition(), recording.exists());
            else { //usual short click
                if (recording.exists()) {
                    Intent playIntent = new Intent(parentActivity, PlayerActivity.class);
                    playIntent.putExtra(RECORDING_EXTRA, recording);
                    startActivity(playIntent);
                } else
                    Toast.makeText(parentActivity, R.string.audio_file_missing, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //A devenit public pentru a funcționa adapter.replaceData() în UnassignedRecordingsFragment
    public class RecordingAdapter extends RecyclerView.Adapter<RecordingHolder> {
        private List<Recording> recordings;
        private List<Contact> contactList;

        List<Recording> getRecordings() {
            return recordings;
        }

        //A devenit public pentru a funcționa adapter.replaceData() în UnassignedRecordingsFragment
        public void replaceData(List<Recording> recordings, List<Contact> contactList) {
            this.recordings = recordings;
            this.contactList = contactList;
            notifyDataSetChanged();
        }

        void removeItem(Recording recording) {
            int position = recordings.indexOf(recording);
            recordings.remove(recording);
            notifyItemRemoved(position);
        }

        RecordingAdapter(List<Recording> recordings) {
            this.recordings = recordings;
        }

        @Override
        @NonNull
        public RecordingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parentActivity);
            return new RecordingHolder(layoutInflater, parent);
        }

        //A devenit public pentru a funcționa UnassignedRecordingsFragment
        public Recording getItem(int position) {
            return recordings.get(position);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordingHolder holder, final int position) {
            final Recording recording = recordings.get(position);
            int adornRes;
            switch (recording.getFormat()) {
                case Recorder.WAV_FORMAT:
                    adornRes = parentActivity.getSettedTheme().equals(BaseActivity.LIGHT_THEME) ?
                            R.drawable.sound_symbol_wav_light : R.drawable.sound_symbol_wav_dark;
                    break;
                case Recorder.AAC_HIGH_FORMAT:
                    adornRes = parentActivity.getSettedTheme().equals(BaseActivity.LIGHT_THEME) ?
                            R.drawable.sound_symbol_aac128_light : R.drawable.sound_symbol_aac128_dark;
                    break;
                case Recorder.AAC_BASIC_FORMAT:
                    adornRes = parentActivity.getSettedTheme().equals(BaseActivity.LIGHT_THEME) ?
                            R.drawable.sound_symbol_aac32_light : R.drawable.sound_symbol_aac32_dark;
                    break;
                default:
                    adornRes = parentActivity.getSettedTheme().equals(BaseActivity.LIGHT_THEME) ?
                            R.drawable.sound_symbol_aac64_light : R.drawable.sound_symbol_aac64_dark;
            }


            for (int i = 0; i < contactList.size(); i++) {

                if (contactList.get(i).getDaytime().equalsIgnoreCase(recording.getDateRecord())) {
                    if (contactList.get(i).isMissed())
                        holder.title.setText("missed by" + contactList.get(i).getPhoneNumber());
                    else
                        holder.title.setText(contactList.get(i).getPhoneNumber());

                }

            }

            if (contact == null || !contact.isPrivateNumber())
                holder.recordingType.setImageResource(recording.isIncoming() ? R.drawable.incoming :
                        parentActivity.getSettedTheme().equals(BaseActivity.LIGHT_THEME) ?
                                R.drawable.outgoing_light : R.drawable.outgoing_dark);
            holder.recordingAdorn.setImageResource(adornRes);
            holder.checkBox.setOnClickListener((View view) ->
                    manageSelectRecording(view, position, recording.exists()));

            if (!recording.exists())
                markNonexistent(holder);

            modifyMargins(holder.itemView);
            if (selectedItems.contains(position))
                selectRecording(holder.itemView);
            else
                deselectRecording(holder.itemView);
        }

        private void markNonexistent(RecordingHolder holder) {
            holder.exclamation.setVisibility(View.VISIBLE);
            int filter = parentActivity.getSettedTheme().equals(BaseActivity.LIGHT_THEME) ?
                    Color.argb(255, 0, 0, 0) : Color.argb(255, 255, 255, 255);
            holder.recordingAdorn.setColorFilter(filter);
            holder.recordingType.setColorFilter(filter);
            holder.recordingAdorn.setImageAlpha(100);
            holder.recordingType.setImageAlpha(100);
            holder.title.setAlpha(0.5f);
        }

        private void unMarkNonexistent(RecordingHolder holder) {
            holder.exclamation.setVisibility(View.GONE);
            holder.recordingAdorn.setColorFilter(null);
            holder.recordingType.setColorFilter(null);
            holder.recordingType.setImageAlpha(255);
            holder.recordingAdorn.setImageAlpha(255);
            holder.title.setAlpha(1f);
        }

        @Override
        public void onViewRecycled(@NonNull RecordingHolder holder) {
            super.onViewRecycled(holder);
            unMarkNonexistent(holder);
        }

        @Override
        public int getItemCount() {
            return recordings.size();
        }

    }

    protected List<Contact> getCallDetails() {

        try {

            List<Contact> contactList = new ArrayList<>();

            StringBuffer sb = new StringBuffer();
            Cursor managedCursor = getContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);
            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
            int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            sb.append("Call Details :");
            while (managedCursor.moveToNext()) {
                String phNumber = managedCursor.getString(number); // mobile number
                String phName = managedCursor.getString(name); // name
                String callType = managedCursor.getString(type); // call type
                String callDate = managedCursor.getString(date); // call date
                Date callDayTime = new Date(Long.valueOf(callDate));
                String callDuration = managedCursor.getString(duration);
                String dir = null;
                int dircode = Integer.parseInt(callType);
                switch (dircode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;

                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        break;
                }
                sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- " + dir + " \nCall Date:--- " + callDayTime + " \nCall duration in sec :--- " + callDuration);
                sb.append("\n----------------------------------");
                String value = "";
                if (phName == null)
                    value = phNumber;
                else
                    value = phName;
                Contact contact = new Contact();

                if (dir != null && dir.equalsIgnoreCase("MISSED"))
                    contact.setMissed(true);

                contact.setPhoneNumber(value);
                contact.setContactName(callDuration);
                contact.setDaytime(new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(callDayTime));
                contactList.add(contact);
            }
            managedCursor.close();
//        miss_cal.setText(sb);

            Log.e("Agil value --- ", sb.toString());
            return contactList;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

}