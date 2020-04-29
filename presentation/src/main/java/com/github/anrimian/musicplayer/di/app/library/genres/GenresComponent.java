package com.github.anrimian.musicplayer.di.app.library.genres;

import com.github.anrimian.musicplayer.di.app.library.genres.items.GenreItemsComponent;
import com.github.anrimian.musicplayer.di.app.library.genres.items.GenreItemsModule;
import com.github.anrimian.musicplayer.ui.library.genres.list.GenresListPresenter;

import dagger.Subcomponent;

@Subcomponent(modules = GenresModule.class)
public interface GenresComponent {

    GenreItemsComponent genreItemsComponent(GenreItemsModule module);

    GenresListPresenter genresListPresenter();
}
