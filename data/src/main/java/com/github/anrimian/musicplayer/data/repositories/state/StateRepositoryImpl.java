package com.github.anrimian.musicplayer.data.repositories.state;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.domain.repositories.StateRepository;

import static com.github.anrimian.musicplayer.data.repositories.state.StateRepositoryImpl.Constants.*;

public class StateRepositoryImpl implements StateRepository {

    interface Constants {
        String PREFERENCES_NAME = "state_preferences";

        String ROOT_FOLDER_PATH = "root_folder_path";
    }

    private final SharedPreferencesHelper preferences;

    public StateRepositoryImpl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    @Override
    public String getRootFolderPath() {
        return preferences.getString(ROOT_FOLDER_PATH);
    }

    @Override
    public void setRootFolderPath(String path) {
        preferences.putString(ROOT_FOLDER_PATH, path);
    }
}
