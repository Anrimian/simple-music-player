package com.github.anrimian.musicplayer.ui.settings.themes.view;

import android.view.ViewGroup;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ItemThemeBinding;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

class ThemesViewHolder extends BaseViewHolder {

    private ItemThemeBinding viewBinding;

    private AppTheme appTheme;

    ThemesViewHolder(ViewGroup parent,
                     Callback<AppTheme> onThemeClickListener) {
        super(parent, R.layout.item_theme);
        viewBinding = ItemThemeBinding.bind(itemView);

        viewBinding.flClickableArea.setOnClickListener(v -> onThemeClickListener.call(appTheme));
    }

    void bind(AppTheme appTheme, boolean isSelected) {
        this.appTheme = appTheme;

        String description = getContext().getString(appTheme.getDescriptionId());
        viewBinding.tvThemeName.setText(description);

        viewBinding.flClickableArea.setContentDescription(description);

        setSelected(isSelected);
    }

    void setSelected(boolean isSelected) {
        viewBinding.rbTheme.setChecked(isSelected);
        viewBinding.flClickableArea.setClickable(!isSelected);
    }

    AppTheme getAppTheme() {
        return appTheme;
    }
}
