package com.github.anrimian.musicplayer.di.app.editor.album;

import com.github.anrimian.musicplayer.ui.editor.album.AlbumEditorPresenter;

import dagger.Subcomponent;

@Subcomponent(modules = AlbumEditorModule.class)
public interface AlbumEditorComponent {
    AlbumEditorPresenter albumEditorPresenter();
}
