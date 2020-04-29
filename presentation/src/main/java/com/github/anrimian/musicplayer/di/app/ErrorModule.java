package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import com.github.anrimian.musicplayer.data.controllers.music.error.PlayerErrorParserImpl;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerErrorParser;
import com.github.anrimian.musicplayer.ui.common.error.parser.DefaultErrorParser;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created on 29.10.2017.
 */

@Module
public class ErrorModule {

    @Provides
    @Nonnull
    @Singleton
    ErrorParser provideErrorParser(Context context, Analytics analytics) {
        return new DefaultErrorParser(context, analytics);
    }

    @Provides
    @Nonnull
    @Singleton
    PlayerErrorParser playerErrorParser(Analytics analytics) {
        return new PlayerErrorParserImpl(analytics);
    }
}
