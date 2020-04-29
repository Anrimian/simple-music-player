package com.github.anrimian.musicplayer.ui.playlist_screens.playlist;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.ListStateStrategy;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface PlayListView extends MvpView {

    String LIST_STATE = "list_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showLoading();

    @StateStrategyType(ListStateStrategy.class)
    void updateItemsList(List<PlayListItem> list);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showConfirmDeleteDialog(List<Composition> compositionsToDelete);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showPlayListInfo(PlayList playList);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeleteCompositionError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeletedCompositionMessage(List<Composition> compositionsToDelete);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectPlayListDialog();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAddingToPlayListError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeleteItemError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeleteItemCompleted(PlayList playList, List<PlayListItem> items);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showConfirmDeletePlayListDialog(PlayList playListToDelete);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showPlayListDeleteSuccess(PlayList playListToDelete);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeletePlayListError(ErrorCommand errorCommand);

    @StateStrategyType(SkipStrategy.class)
    void notifyItemMoved(int from, int to);

    @StateStrategyType(SkipStrategy.class)
    void showCompositionActionDialog(PlayListItem playListItem, int position);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void notifyItemRemoved(int position);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showEditPlayListNameDialog(PlayList playListInMenu);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onCompositionsAddedToPlayNext(List<Composition> compositions);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onCompositionsAddedToQueue(List<Composition> compositions);
}
