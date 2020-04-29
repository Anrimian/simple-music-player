package com.github.anrimian.musicplayer.ui.library.common.order;

import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;

import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public class SelectOrderPresenter extends MvpPresenter<SelectOrderView> {

    private OrderType orderType;
    private boolean reverse;

    void setOrder(Order order) {
        orderType = order.getOrderType();
        reverse = order.isReversed();
        getViewState().showReverse(reverse);
        getViewState().showSelectedOrder(orderType);
    }

    void onOrderTypeSelected(OrderType order) {
        this.orderType = order;
        getViewState().showSelectedOrder(order);
    }

    void onReverseTypeSelected(boolean selected) {
        this.reverse = selected;
        getViewState().showReverse(selected);
    }

    void onCompleteButtonClicked() {
        getViewState().close(new Order(orderType, reverse));
    }
}
