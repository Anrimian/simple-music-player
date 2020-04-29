package com.github.anrimian.musicplayer.ui.common.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.MenuRes;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getMenuItems;

public class PopupMenuWindow {

    public static void showPopup(View anchorView,
                                 @MenuRes int menuResId,
                                 Callback<MenuItem> listener) {
        showPopup(anchorView, menuResId, Gravity.START, listener);
    }

    public static void showPopup(View anchorView,
                                 @MenuRes int menuResId,
                                 int gravity,
                                 Callback<MenuItem> listener) {
        Menu menu = AndroidUtils.createMenu(anchorView.getContext(), menuResId);
        int screenMargin = anchorView.getResources().getDimensionPixelSize(R.dimen.popup_screen_margin);
        showPopup(anchorView, getMenuItems(menu), listener, gravity, screenMargin);
    }

    public static void showActionBarPopup(View anchorView,
                                          ArrayList<MenuItemImpl> items,
                                          Callback<MenuItem> listener) {
        int screenMargin = anchorView.getResources().getDimensionPixelSize(R.dimen.action_bar_popup_screen_margin);
        showPopup(anchorView, items, listener, Gravity.CENTER, screenMargin);
    }

    private static void showPopup(View anchorView,
                                  List<? extends MenuItem> menuItems,
                                  Callback<MenuItem> listener,
                                  int gravity,
                                  int screenMargin) {
        Context context = anchorView.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams")
        View popupView = inflater.inflate(R.layout.menu_popup, null);

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        AtomicReference<PopupWindow> popupWindow = new AtomicReference<>();

        MenuAdapter menuAdapter = new MenuAdapter(menuItems, R.layout.item_popup_menu);
        menuAdapter.setOnItemClickListener(item -> {
            listener.call(item);
            popupWindow.get().dismiss();
        });
        recyclerView.setAdapter(menuAdapter);

        popupWindow.set(AppPopupWindow.showPopupWindow(anchorView, popupView, gravity, screenMargin));
    }
}
