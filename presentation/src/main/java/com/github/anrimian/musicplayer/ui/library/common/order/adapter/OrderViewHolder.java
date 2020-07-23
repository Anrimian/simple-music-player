package com.github.anrimian.musicplayer.ui.library.common.order.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;

import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getOrderTitle;

class OrderViewHolder extends RecyclerView.ViewHolder {

    private RadioButton rbOrder;

    private OrderType order;

    OrderViewHolder(LayoutInflater inflater,
                    ViewGroup parent,
                    OnItemClickListener<OrderType> onItemClickListener) {
        super(inflater.inflate(R.layout.item_order, parent, false));
        rbOrder = itemView.findViewById(R.id.rb_order);

        if (onItemClickListener != null) {
            rbOrder.setOnClickListener(v -> onItemClickListener.onItemClick(order));
        }
    }

    void bindView(OrderType order) {
        this.order = order;
        rbOrder.setText(getOrderTitle(order));
    }

    void setSelected(boolean selected) {
        rbOrder.setChecked(selected);
    }

    OrderType getOrder() {
        return order;
    }
}
