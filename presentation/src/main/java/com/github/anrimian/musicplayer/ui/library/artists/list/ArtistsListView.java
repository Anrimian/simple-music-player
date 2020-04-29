package com.github.anrimian.musicplayer.ui.library.artists.list;

import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.ListStateStrategy;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface ArtistsListView extends MvpView {

    String LIST_STATE = "list_state";
    String RENAME_STATE = "rename_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptySearchResult();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showLoading();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showLoadingError(ErrorCommand errorCommand);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = RENAME_STATE)
    void showRenameProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = RENAME_STATE)
    void hideRenameProgress();

    @StateStrategyType(ListStateStrategy.class)
    void submitList(List<Artist> artists);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectOrderScreen(Order order);
}
