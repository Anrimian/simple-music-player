package com.github.anrimian.musicplayer.ui.library.common.order;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;
import com.github.anrimian.musicplayer.ui.library.common.order.adapter.OrderAdapter;
import com.github.anrimian.musicplayer.ui.utils.OnCompleteListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.Constants.Arguments.ORDERS_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.ORDER_ARG;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getReversedOrderText;

public class SelectOrderDialogFragment extends MvpAppCompatDialogFragment implements SelectOrderView {

    @InjectPresenter
    SelectOrderPresenter presenter;

    @BindView(R.id.rv_order)
    RecyclerView rvOrder;

    @BindView(R.id.cb_desc)
    CheckBox cbDesc;

    private OrderAdapter orderAdapter;

    @ProvidePresenter
    SelectOrderPresenter providePresenter() {
        return Components.getLibraryComponent().selectOrderPresenter();
    }

    @Nullable
    private OnCompleteListener<Order> onCompleteListener;

    public static SelectOrderDialogFragment newInstance(Order selectedOrder,
                                                        OrderType... orders) {
        Bundle args = new Bundle();
        args.putSerializable(ORDER_ARG, selectedOrder);
        args.putSerializable(ORDERS_ARG, orders);
        SelectOrderDialogFragment fragment = new SelectOrderDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_order, null);

        ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvOrder.setLayoutManager(layoutManager);

        //noinspection ConstantConditions
        OrderType[] orders = (OrderType[]) getArguments().getSerializable(ORDERS_ARG);

        orderAdapter = new OrderAdapter(orders, presenter::onOrderTypeSelected);
        rvOrder.setAdapter(orderAdapter);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.order)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(R.string.cancel, (dialog1, which) -> {})
                .create();
        dialog.show();

        Button btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnOk.setOnClickListener(v -> presenter.onCompleteButtonClicked());

        cbDesc.setOnCheckedChangeListener((buttonView, isChecked) ->
                presenter.onReverseTypeSelected(isChecked)
        );

        if (savedInstanceState == null) {
            presenter.setOrder(getOrder());
        }

        return dialog;

    }

    @Override
    public void showSelectedOrder(OrderType orderType) {
        orderAdapter.setCheckedItem(orderType);
        cbDesc.setText(getReversedOrderText(orderType));
    }

    @Override
    public void showReverse(boolean selected) {
        cbDesc.setChecked(selected);
    }

    @Override
    public void close(Order order) {
        if (!getOrder().equals(order) && onCompleteListener != null) {
            onCompleteListener.onComplete(order);
        }
        dismissAllowingStateLoss();
    }

    public void setOnCompleteListener(@Nullable OnCompleteListener<Order> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    private Order getOrder() {
        //noinspection ConstantConditions
        return (Order) getArguments().getSerializable(ORDER_ARG);
    }
}
