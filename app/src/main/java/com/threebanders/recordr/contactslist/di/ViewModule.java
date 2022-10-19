package com.threebanders.recordr.contactslist.di;

import com.threebanders.recordr.contactslist.ContactsListContract;
import com.threebanders.recordr.contactslist.ContactsListFragment;

import dagger.Module;
import dagger.Provides;

@Module
public class ViewModule {
    private ContactsListFragment fragment;
    public ViewModule(ContactsListFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public ContactsListContract.View provideView() {
        return fragment;
    }
}
