package com.github.anrimian.musicplayer.ui.editor.album;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface AlbumEditorView extends MvpView {

    String DISPLAY_ALBUM_STATE = "display_album_state";
    String CHANGE_STATE = "change_state";

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = DISPLAY_ALBUM_STATE)
    void showAlbumLoadingError(ErrorCommand errorCommand);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = DISPLAY_ALBUM_STATE)
    void showAlbum(Album album);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CHANGE_STATE)
    void showRenameProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CHANGE_STATE)
    void hideRenameProgress();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand errorCommand);

    @StateStrategyType(SkipStrategy.class)
    void showEnterAuthorDialog(Album album, String[] hints);

    @StateStrategyType(SkipStrategy.class)
    void showEnterNameDialog(Album album);

}
