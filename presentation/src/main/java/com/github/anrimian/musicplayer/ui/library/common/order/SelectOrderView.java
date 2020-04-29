package com.github.anrimian.musicplayer.ui.library.common.order;

import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface SelectOrderView extends MvpView {

    @StateStrategyType(SkipStrategy.class)
    void close(Order order);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showSelectedOrder(OrderType order);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showReverse(boolean selected);
}
