package com.github.anrimian.musicplayer.domain.interactors.analytics;

public interface Analytics {

    void processNonFatalError(Throwable throwable);

    void logMessage(String message);
}
