package com.threebanders.recordr.di;

import com.threebanders.recordr.contactdetail.di.ContactDetailComponent;
import com.threebanders.recordr.contactslist.di.ContactsListComponent;

import dagger.Module;

@Module(subcomponents = {ContactsListComponent.class, ContactDetailComponent.class})
class AppSubcomponents {
}
