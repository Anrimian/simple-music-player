package com.github.anrimian.musicplayer.ui.library.common.compositions;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.mvp.ListMvpView;

import java.util.Collection;
import java.util.List;

import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface BaseLibraryCompositionsView extends ListMvpView<Composition> {

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectPlayListDialog();

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

    @StateStrategyType(SkipStrategy.class)
    void onCompositionSelected(Composition composition, int position);

    @StateStrategyType(SkipStrategy.class)
    void onCompositionUnselected(Composition composition, int position);

    @StateStrategyType(SkipStrategy.class)
    void setItemsSelected(boolean selected);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showSelectionMode(int count);

    @StateStrategyType(SkipStrategy.class)
    void shareCompositions(Collection<Composition> selectedCompositions);

    @StateStrategyType(SkipStrategy.class)
    void showCompositionActionDialog(Composition composition, int position);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand errorCommand);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void setDisplayCoversEnabled(boolean isCoversEnabled);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onCompositionsAddedToPlayNext(List<Composition> compositions);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onCompositionsAddedToQueue(List<Composition> compositions);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showCurrentComposition(CurrentComposition currentComposition);
}
