package com.github.anrimian.musicplayer.domain;

public interface Constants {

    Object TRIGGER = new Object();
    int NO_POSITION = -1;
    long STORAGE_EVENTS_MIN_EMIT_WINDOW_MILLIS = 2000;

    interface TIMEOUTS {
        int STORAGE_LOADING_TIMEOUT_SECONDS = 30;
    }
}
