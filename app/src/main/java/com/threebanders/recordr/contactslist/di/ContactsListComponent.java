package com.threebanders.recordr.contactslist.di;

import com.threebanders.recordr.contactslist.ContactsListFragment;
import com.threebanders.recordr.di.FragmentScope;
import dagger.Subcomponent;

@Subcomponent(modules = {ViewModule.class, PresenterModule.class})
@FragmentScope
public interface ContactsListComponent {

    @Subcomponent.Factory
    interface Factory {
        ContactsListComponent create(ViewModule module);
    }

    void inject(ContactsListFragment fragment);
}
