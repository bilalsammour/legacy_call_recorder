package com.threebanders.recordr.di;

import android.content.Context;

import com.threebanders.recordr.data.Repository;
import com.threebanders.recordr.data.RepositoryImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class RepositoryModule {
    private static final String DATABASE_NAME = "callrecorder.db";

    @Provides
    @Singleton
     Repository provideRepository(Context context) {
        return new RepositoryImpl(context, DATABASE_NAME);
    }
}
