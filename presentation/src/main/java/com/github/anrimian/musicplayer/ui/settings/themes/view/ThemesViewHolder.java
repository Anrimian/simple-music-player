package com.github.anrimian.musicplayer.ui.settings.themes.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

class ThemesViewHolder extends BaseViewHolder {

    @BindView(R.id.tv_theme_name)
    TextView tvTheme;

    @BindView(R.id.rb_theme)
    RadioButton rbTheme;

    @BindView(R.id.fl_clickable_area)
    View flClickableArea;

    private AppTheme appTheme;

    ThemesViewHolder(ViewGroup parent,
                     Callback<AppTheme> onThemeClickListener) {
        super(parent, R.layout.item_theme);
        ButterKnife.bind(this, itemView);

        flClickableArea.setOnClickListener(v -> onThemeClickListener.call(appTheme));
    }

    void bind(AppTheme appTheme, boolean isSelected) {
        this.appTheme = appTheme;

        String description = getContext().getString(appTheme.getDescriptionId());
        tvTheme.setText(description);

        flClickableArea.setContentDescription(description);

        setSelected(isSelected);
    }

    void setSelected(boolean isSelected) {
        rbTheme.setChecked(isSelected);
        flClickableArea.setClickable(!isSelected);
    }

    AppTheme getAppTheme() {
        return appTheme;
    }
}
