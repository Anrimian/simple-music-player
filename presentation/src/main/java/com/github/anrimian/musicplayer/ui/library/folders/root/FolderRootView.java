package com.github.anrimian.musicplayer.ui.library.folders.root;

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface FolderRootView extends MvpView {

    String LOADING_STATE = "loading_state";

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showFolderScreens(List<Long> ids);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LOADING_STATE)
    void showProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LOADING_STATE)
    void showError(ErrorCommand errorCommand);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LOADING_STATE)
    void showIdle();

}
