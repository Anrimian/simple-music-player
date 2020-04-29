package com.github.anrimian.musicplayer.ui.library.artists.items.adapter;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.utils.wrappers.DefferedObject;

import java.util.List;

public class ArtistAlbumsPresenter {

    private DefferedObject<AlbumsViewHolder> view = new DefferedObject<>();

    public void submitAlbums(List<Album> albums) {
        view.call(view -> view.submitList(albums));
    }

    void attachView(AlbumsViewHolder view) {
        this.view.setObject(view);
    }

    public void setCompositionsTitleVisible(boolean visible) {
        view.call(view -> view.setCompositionsTitleVisible(visible));
    }
}
