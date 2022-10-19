package com.threebanders.recordr.contactdetail.di;

import com.threebanders.recordr.contactdetail.ContactDetailContract;
import com.threebanders.recordr.contactdetail.ContactDetailFragment;

import dagger.Module;
import dagger.Provides;

@Module
public class ViewModule {
    private ContactDetailFragment fragment;
    public ViewModule(ContactDetailFragment fragment) {
        this.fragment = fragment;
    }

    @Provides
    public ContactDetailContract.View provideView() {
        return fragment;
    }
}
