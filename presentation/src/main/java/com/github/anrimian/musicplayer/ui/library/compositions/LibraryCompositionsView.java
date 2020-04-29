package com.github.anrimian.musicplayer.ui.library.compositions;

import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsView;

import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface LibraryCompositionsView extends BaseLibraryCompositionsView {

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectOrderScreen(Order order);

}
