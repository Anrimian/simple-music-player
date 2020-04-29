package com.github.anrimian.musicplayer.ui.settings.folders;

import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.AddToEndSingleTagStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface ExcludedFoldersView extends MvpView {

    String LIST_STATE = "list_state";

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LIST_STATE)
    void showListState();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LIST_STATE)
    void showEmptyListState();

    @StateStrategyType(value = AddToEndSingleTagStrategy.class, tag = LIST_STATE)
    void showErrorState(ErrorCommand errorCommand);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showExcludedFoldersList(List<IgnoredFolder> folders);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showRemovedFolderMessage(IgnoredFolder folder);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand errorCommand);
}
