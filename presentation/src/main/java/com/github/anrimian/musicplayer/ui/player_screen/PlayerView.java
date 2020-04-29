package com.github.anrimian.musicplayer.ui.player_screen;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.ListStateStrategy;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import javax.annotation.Nullable;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

/**
 * Created on 02.11.2017.
 */

public interface PlayerView extends MvpView {

    String PLAYER_STATE = "player_state";
    String BOTTOM_PANEL_STATE = "bottom_panel_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void showStopState();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void showPlayState();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = BOTTOM_PANEL_STATE)
    void expandBottomPanel();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = BOTTOM_PANEL_STATE)
    void collapseBottomPanel();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void setMusicControlsEnabled(boolean show);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showCurrentQueueItem(@Nullable PlayQueueItem item, boolean showCover);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void scrollQueueToPosition(int position);

    @StateStrategyType(ListStateStrategy.class)
    void updatePlayQueue(List<PlayQueueItem> items);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showRepeatMode(int mode);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showRandomPlayingButton(boolean active);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showTrackState(long currentPosition, long duration);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectPlayListDialog();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showShareMusicDialog(Composition composition);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAddingToPlayListError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showConfirmDeleteDialog(List<Composition> compositionsToDelete);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeleteCompositionError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeleteCompositionMessage(List<Composition> compositionsToDelete);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showPlayQueueSubtitle(int size);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDrawerScreen(int selectedDrawerScreen, long selectedPlayListScreen);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showLibraryScreen(int selectedLibraryScreen);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void setSkipToNextButtonEnabled(boolean enabled);

    @StateStrategyType(SkipStrategy.class)
    void notifyItemMoved(int from, int to);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void setPlayQueueCoversEnabled(boolean isCoversEnabled);

    @StateStrategyType(SkipStrategy.class)
    void startEditCompositionScreen(long id);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeletedItemMessage();
}
