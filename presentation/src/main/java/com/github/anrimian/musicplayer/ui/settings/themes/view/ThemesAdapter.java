package com.github.anrimian.musicplayer.ui.settings.themes.view;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;

import java.util.HashSet;
import java.util.Set;

public class ThemesAdapter extends RecyclerView.Adapter<ThemesViewHolder> {

    private final Set<ThemesViewHolder> viewHolders = new HashSet<>();

    private final AppTheme[] themes;
    private final Callback<AppTheme> themeClickListener;

    private AppTheme currentTheme;

    public ThemesAdapter(AppTheme[] themes,
                         AppTheme currentTheme,
                         Callback<AppTheme> themeClickListener) {
        this.themes = themes;
        this.currentTheme = currentTheme;
        this.themeClickListener = themeClickListener;
    }

    @NonNull
    @Override
    public ThemesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThemesViewHolder(parent, themeClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemesViewHolder holder, int position) {
        viewHolders.add(holder);

        AppTheme theme = themes[position];
        holder.bind(theme, theme.equals(currentTheme));
    }

    @Override
    public int getItemCount() {
        return themes.length;
    }

    @Override
    public void onViewRecycled(@NonNull ThemesViewHolder holder) {
        super.onViewRecycled(holder);
        viewHolders.remove(holder);
    }

    public void setCurrentTheme(AppTheme currentTheme) {
        this.currentTheme = currentTheme;
        for (ThemesViewHolder holder: viewHolders) {
            holder.setSelected(holder.getAppTheme() == currentTheme);
        }
    }
}
