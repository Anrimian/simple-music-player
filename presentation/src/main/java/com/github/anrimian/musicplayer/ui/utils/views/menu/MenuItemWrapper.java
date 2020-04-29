package com.github.anrimian.musicplayer.ui.utils.views.menu;

import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.IdRes;

import com.github.anrimian.musicplayer.domain.utils.functions.Callback;

import java.util.LinkedList;

import javax.annotation.Nullable;

public class MenuItemWrapper {

    @Nullable
    private MenuItem menuItem;

    private LinkedList<Callback<MenuItem>> deferredFunctions = new LinkedList<>();

    public void setMenuItem(Menu menu, @IdRes int resId) {
        if (menuItem == null) {
            MenuItem searchItem = menu.findItem(resId);
            searchItem.setVisible(false);
            setMenuItem(searchItem);
        }
    }

    public void setMenuItem(@Nullable MenuItem menuItem) {
        this.menuItem = menuItem;
        while (!deferredFunctions.isEmpty()) {
            deferredFunctions.pollFirst().call(menuItem);
        }
    }

    public void call(Callback<MenuItem> function) {
        if (menuItem != null) {
            function.call(menuItem);
        } else {
            deferredFunctions.add(function);
        }
    }
}
