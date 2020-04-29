package com.github.anrimian.musicplayer.data.controllers.music.error;

import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.source.UnrecognizedInputFormatException;
import com.google.android.exoplayer2.upstream.ContentDataSource;

import java.io.FileNotFoundException;

public class PlayerErrorParserImpl implements PlayerErrorParser {

    private final Analytics analytics;

    public PlayerErrorParserImpl(Analytics analytics) {
        this.analytics = analytics;
    }

    @Override
    public ErrorType getErrorType(Throwable throwable) {
        if (throwable instanceof ContentDataSource.ContentDataSourceException) {
            throwable = throwable.getCause();
            if (throwable instanceof FileNotFoundException) {
                return ErrorType.NOT_FOUND;
            }
        }
        if (throwable instanceof FileNotFoundException) {
            return ErrorType.NOT_FOUND;
        }
        if (throwable instanceof ExoPlaybackException) {
            Throwable cause = throwable.getCause();
            if (cause instanceof UnrecognizedInputFormatException) {
                return ErrorType.UNSUPPORTED;
            }
        }
        if (throwable instanceof SecurityException) {
            return ErrorType.IGNORED;
        }
        analytics.processNonFatalError(throwable);
        return ErrorType.UNKNOWN;
    }
}
