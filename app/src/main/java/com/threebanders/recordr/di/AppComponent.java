package com.threebanders.recordr.di;

import android.content.Context;

import com.threebanders.recordr.contactdetail.di.ContactDetailComponent;
import com.threebanders.recordr.contactslist.di.ContactsListComponent;
import com.threebanders.recordr.recorder.RecorderService;

import javax.inject.Singleton;
import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = { RepositoryModule.class, AppSubcomponents.class })
public interface AppComponent {
    @Component.Factory
    interface Factory {
        AppComponent create(@BindsInstance Context context);
    }

    void inject(RecorderService service);
    ContactsListComponent.Factory contactsListComponent();
    ContactDetailComponent.Factory contactDetailComponent();
}
