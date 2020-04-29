package com.github.anrimian.musicplayer.di.app.library.artists.items;

import com.github.anrimian.musicplayer.ui.library.artists.items.ArtistItemsPresenter;

import dagger.Subcomponent;

@Subcomponent(modules = ArtistItemsModule.class)
public interface ArtistItemsComponent {

    ArtistItemsPresenter artistItemsPresenter();
}
