package com.github.anrimian.simplemusicplayer.di.app;

import android.content.Context;

import com.github.anrimian.simplemusicplayer.data.database.AppDatabase;
import com.github.anrimian.simplemusicplayer.data.database.DatabaseManager;
import com.github.anrimian.simplemusicplayer.data.database.dao.CompositionsDao;
import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDao;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created on 20.11.2017.
 */

@Module
public class DbModule {

    @Provides
    @Nonnull
    @Singleton
    DatabaseManager provideDatabaseManager(Context context) {
        return new DatabaseManager(context);
    }

    @Provides
    @Nonnull
    @Singleton
    AppDatabase provideAppDatabase(DatabaseManager databaseManager) {
        return databaseManager.getAppDatabase();
    }

    @Provides
    @Nonnull
    @Singleton
    CompositionsDao compositionsDao(AppDatabase appDatabase) {
        return appDatabase.compositionsDao();
    }

    @Provides
    @Nonnull
    @Singleton
    PlayQueueDao playQueueDao(AppDatabase appDatabase) {
        return appDatabase.playQueueDao();
    }
}
