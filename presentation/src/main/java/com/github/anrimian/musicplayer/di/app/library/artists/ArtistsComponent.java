package com.github.anrimian.musicplayer.di.app.library.artists;

import com.github.anrimian.musicplayer.di.app.library.artists.items.ArtistItemsComponent;
import com.github.anrimian.musicplayer.di.app.library.artists.items.ArtistItemsModule;
import com.github.anrimian.musicplayer.ui.library.artists.list.ArtistsListPresenter;

import dagger.Subcomponent;

@Subcomponent(modules = ArtistsModule.class)
public interface ArtistsComponent {

    ArtistItemsComponent artistItemsComponent(ArtistItemsModule module);

    ArtistsListPresenter artistsListPresenter();
}
