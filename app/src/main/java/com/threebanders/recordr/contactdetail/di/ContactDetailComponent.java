package com.threebanders.recordr.contactdetail.di;

import com.threebanders.recordr.contactdetail.ContactDetailFragment;
import com.threebanders.recordr.di.FragmentScope;

import dagger.Subcomponent;

@Subcomponent(modules = {ViewModule.class, PresenterModule.class})
@FragmentScope
public interface ContactDetailComponent {
    @Subcomponent.Factory
    interface Factory {
        ContactDetailComponent create(ViewModule module);
    }

    void inject(ContactDetailFragment fragment);
}
