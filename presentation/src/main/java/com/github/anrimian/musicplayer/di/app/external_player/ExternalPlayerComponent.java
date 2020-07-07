package com.github.anrimian.musicplayer.di.app.external_player;

import com.github.anrimian.musicplayer.ui.main.external_player.ExternalPlayerPresenter;

import dagger.Subcomponent;

@Subcomponent(modules = ExternalPlayerModule.class)
public interface ExternalPlayerComponent {

    ExternalPlayerPresenter externalPlayerPresenter();
}
