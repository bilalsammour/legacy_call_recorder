package com.threebanders.recordr.contactdetail.di;

import com.threebanders.recordr.contactdetail.ContactDetailContract;
import com.threebanders.recordr.contactdetail.ContactDetailPresenter;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class PresenterModule {
    @Binds
    abstract ContactDetailContract.Presenter providePresenter(ContactDetailPresenter presenter);
}
