package com.github.anrimian.musicplayer.ui.utils.views;

import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;

public class ViewClickUtil {

    private static final int CLICK_WINDOW_MILLIS = 250;

    private static final Handler handler = new Handler();
    private static boolean clickLocked = false;

    public static void onClick(@NonNull View view, @NonNull Runnable runnable) {
        view.setOnClickListener(v -> {
            if (!clickLocked) {
                clickLocked = true;
                runnable.run();
                handler.postDelayed(() -> clickLocked = false, CLICK_WINDOW_MILLIS);
            }
        });
    }

    public static boolean filterFastClick(@NonNull Runnable runnable) {
        return filterFastClick(runnable, CLICK_WINDOW_MILLIS);
    }

    public static boolean filterFastClick(@NonNull Runnable runnable, int clickWindowMillis) {
        if (!clickLocked) {
            clickLocked = true;
            runnable.run();
            handler.postDelayed(() -> clickLocked = false, clickWindowMillis);
            return true;
        }
        return false;
    }
}
