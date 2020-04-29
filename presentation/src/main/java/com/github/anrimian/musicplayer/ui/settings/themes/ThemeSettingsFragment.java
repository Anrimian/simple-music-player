package com.github.anrimian.musicplayer.ui.settings.themes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.settings.themes.view.ThemesAdapter;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.decorators.DividerItemDecoration;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked;

public class ThemeSettingsFragment extends Fragment {

    @BindView(R.id.rv_themes)
    RecyclerView rvThemes;

    @BindView(R.id.cb_auto_night_mode)
    CheckBox cbAutoNightMode;

    @BindView(R.id.nsv_container)
    NestedScrollView container;

    private ThemeController themeController;

    private ThemesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_themes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        themeController = Components.getAppComponent().themeController();

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(R.string.theme);
        toolbar.setTitleClickListener(null);

        SlidrPanel.simpleSwipeBack(container, this, toolbar::onStackFragmentSlided);

        rvThemes.setLayoutManager(new LinearLayoutManager(requireContext()));
        DividerItemDecoration itemDecorator = new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL,
                getResources().getDimensionPixelSize(R.dimen.toolbar_content_start),
                false);
        rvThemes.addItemDecoration(itemDecorator);

        adapter = new ThemesAdapter(AppTheme.values(),
                themeController.getCurrentTheme(),
                this::onThemeClicked);
        rvThemes.setAdapter(adapter);

        setChecked(cbAutoNightMode, themeController.isAutoDarkThemeEnabled());
        cbAutoNightMode.setOnCheckedChangeListener((v, isChecked) ->
                themeController.setAutoDarkModeEnabled(requireActivity(), isChecked)
        );
    }

    private void onThemeClicked(AppTheme appTheme) {
        themeController.setTheme(requireActivity(), appTheme);
        adapter.setCurrentTheme(appTheme);
    }
}
