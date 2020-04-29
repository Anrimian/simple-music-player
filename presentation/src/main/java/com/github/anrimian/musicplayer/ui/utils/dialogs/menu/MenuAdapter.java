package com.github.anrimian.musicplayer.ui.utils.dialogs.menu;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;

import java.util.List;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getMenuItems;

public class MenuAdapter extends RecyclerView.Adapter<MenuViewHolder> {

    private final List<? extends MenuItem> items;

    @LayoutRes
    private final int menuViewRes;

    private OnItemClickListener<MenuItem> onItemClickListener;

    public MenuAdapter(List<? extends MenuItem> items, @LayoutRes int menuViewRes) {
        this.items = items;
        this.menuViewRes = menuViewRes;
    }

    public MenuAdapter(Menu menu, @LayoutRes int menuViewRes) {
        this.items = getMenuItems(menu);
        this.menuViewRes = menuViewRes;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MenuViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                menuViewRes,
                onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(OnItemClickListener<MenuItem> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
