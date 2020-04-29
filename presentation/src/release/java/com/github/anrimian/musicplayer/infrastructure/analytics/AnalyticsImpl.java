package com.github.anrimian.musicplayer.infrastructure.analytics;

import android.util.Log;

import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.utils.filelog.FileLog;

public class AnalyticsImpl implements Analytics {

    private final FileLog fileLog;

    public AnalyticsImpl(FileLog fileLog) {
        this.fileLog = fileLog;
    }

    @Override
    public void processNonFatalError(Throwable throwable) {
        fileLog.writeException(throwable);
    }

    @Override
    public void logMessage(String message) {
        fileLog.writeMessage(message);
    }
}
