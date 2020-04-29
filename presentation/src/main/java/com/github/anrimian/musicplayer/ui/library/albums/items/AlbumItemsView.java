package com.github.anrimian.musicplayer.ui.library.albums.items;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView;

import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface AlbumItemsView extends BaseLibraryCompositionsView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showAlbumInfo(Album album);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();

    @StateStrategyType(SkipStrategy.class)
    void showEditAlbumScreen(Album album);
}
