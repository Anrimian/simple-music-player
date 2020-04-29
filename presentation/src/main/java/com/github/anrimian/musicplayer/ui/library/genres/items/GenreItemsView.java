package com.github.anrimian.musicplayer.ui.library.genres.items;

import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface GenreItemsView extends BaseLibraryCompositionsView {

    String RENAME_STATE = "rename_state";

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showGenreInfo(Genre genre);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();

    @StateStrategyType(SkipStrategy.class)
    void showRenameGenreDialog(Genre genre);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = RENAME_STATE)
    void showRenameProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = RENAME_STATE)
    void hideRenameProgress();
}
