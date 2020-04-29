package com.github.anrimian.musicplayer.di.app.library.albums;

import com.github.anrimian.musicplayer.di.app.library.albums.items.AlbumItemsComponent;
import com.github.anrimian.musicplayer.di.app.library.albums.items.AlbumItemsModule;
import com.github.anrimian.musicplayer.ui.library.albums.list.AlbumsListPresenter;

import dagger.Subcomponent;

@Subcomponent(modules = AlbumsModule.class)
public interface AlbumsComponent {

    AlbumItemsComponent albumItemsComponent(AlbumItemsModule module);

    AlbumsListPresenter albumsListPresenter();
}
