package com.github.anrimian.musicplayer.ui.widgets;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;

public class WidgetDataHolder {

    private static final String WIDGET_STATE = "widget_state";

    private static final String CURRENT_COMPOSITION = "current_composition";
    private static final String CURRENT_COMPOSITION_AUTHOR = "current_composition_author";
    private static final String CURRENT_QUEUE_SIZE = "current_queue_size";
    private static final String CURRENT_COMPOSITION_ID = "current_composition_id";
    private static final String CURRENT_COMPOSITION_UPDATE_TIME = "current_composition_update_time";

    static void setCompositionInfo(Context context,
                                   String compositionName,
                                   String author,
                                   long compositionId,
                                   long updateTime) {
        SharedPreferences preferences = getWidgetPreferences(context);
        preferences.edit()
                .putString(CURRENT_COMPOSITION, compositionName)
                .putString(CURRENT_COMPOSITION_AUTHOR, author)
                .putLong(CURRENT_COMPOSITION_ID, compositionId)
                .putLong(CURRENT_COMPOSITION_UPDATE_TIME, updateTime)
                .apply();
    }

    static void setCurrentQueueSize(Context context, int size) {
        SharedPreferencesHelper preferences = getPreferences(context);
        preferences.putInt(CURRENT_QUEUE_SIZE, size);
    }

    public static String getCompositionName(Context context) {
        SharedPreferencesHelper preferences = getPreferences(context);
        return preferences.getString(CURRENT_COMPOSITION);
    }

    public static String getCompositionAuthor(Context context) {
        SharedPreferencesHelper preferences = getPreferences(context);
        return preferences.getString(CURRENT_COMPOSITION_AUTHOR);
    }

    public static int getCurrentQueueSize(Context context) {
        SharedPreferencesHelper preferences = getPreferences(context);
        return preferences.getInt(CURRENT_QUEUE_SIZE);
    }

    public static long getCompositionId(Context context) {
        SharedPreferencesHelper preferences = getPreferences(context);
        return preferences.getLong(CURRENT_COMPOSITION_ID);
    }

    public static long getCompositionUpdateTime(Context context) {
        SharedPreferencesHelper preferences = getPreferences(context);
        return preferences.getLong(CURRENT_COMPOSITION_UPDATE_TIME);
    }

    private static SharedPreferencesHelper getPreferences(Context context) {
        SharedPreferences preferences = getWidgetPreferences(context);
        return new SharedPreferencesHelper(preferences);
    }

    private static SharedPreferences getWidgetPreferences(Context context) {
        return context.getSharedPreferences(WIDGET_STATE, Context.MODE_PRIVATE);
    }
}
