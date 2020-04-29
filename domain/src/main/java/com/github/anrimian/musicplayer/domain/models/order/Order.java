package com.github.anrimian.musicplayer.domain.models.order;

import java.io.Serializable;

import javax.annotation.Nonnull;

public class Order implements Serializable {

    @Nonnull
    private OrderType orderType;
    private boolean reversed;

    public Order(@Nonnull OrderType orderType, boolean reversed) {
        this.orderType = orderType;
        this.reversed = reversed;
    }

    @Nonnull
    public OrderType getOrderType() {
        return orderType;
    }

    public boolean isReversed() {
        return reversed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        if (reversed != order.reversed) return false;
        return orderType == order.orderType;
    }

    @Override
    public int hashCode() {
        int result = orderType.hashCode();
        result = 31 * result + (reversed ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderType=" + orderType +
                ", reversed=" + reversed +
                '}';
    }
}
