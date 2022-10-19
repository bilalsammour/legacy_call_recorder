package com.threebanders.recordr.contactslist.di;

import dagger.Binds;
import dagger.Module;
import com.threebanders.recordr.contactslist.ContactsListContract.Presenter;
import com.threebanders.recordr.contactslist.ContactsListPresenter;

@Module
public abstract class PresenterModule {
    @Binds
    public abstract Presenter providePresenter(ContactsListPresenter presenter);
}
