package com.github.anrimian.musicplayer.ui.library.common.order.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.order.OrderType;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;

import java.util.HashSet;
import java.util.Set;

public class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {

    private final Set<OrderViewHolder> viewHolders = new HashSet<>();

    private final OrderType[] orderList;
    private final OnItemClickListener<OrderType> onItemClickListener;

    private OrderType selectedOrder;

    public OrderAdapter(OrderType[] orderList,
                        OnItemClickListener<OrderType> onItemClickListener) {
        this.orderList = orderList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(parent.getContext());
      return new OrderViewHolder(inflater, parent, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        viewHolders.add(holder);

        OrderType order = orderList[position];
        holder.bindView(order);
        holder.setSelected(order == selectedOrder);
    }

    @Override
    public int getItemCount() {
        return orderList.length;
    }

    @Override
    public void onViewRecycled(@NonNull OrderViewHolder holder) {
        super.onViewRecycled(holder);
        viewHolders.remove(holder);
    }

    public void setCheckedItem(OrderType selectedOrder) {
        this.selectedOrder = selectedOrder;
        for (OrderViewHolder holder: viewHolders) {
            holder.setSelected(holder.getOrder() == selectedOrder);
        }
    }
}
