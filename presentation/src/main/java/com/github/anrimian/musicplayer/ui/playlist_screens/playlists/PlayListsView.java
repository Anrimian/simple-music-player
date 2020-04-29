package com.github.anrimian.musicplayer.ui.playlist_screens.playlists;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.ListStateStrategy;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface PlayListsView extends MvpView {

    String LIST_STATE = "list_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showLoading();

    @StateStrategyType(ListStateStrategy.class)
    void updateList(List<PlayList> lists);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showPlayListMenu(PlayList playList);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showConfirmDeletePlayListDialog(PlayList playList);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showPlayListDeleteSuccess(PlayList playListToDelete);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeletePlayListError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showEditPlayListNameDialog(PlayList playListInMenu);
}
