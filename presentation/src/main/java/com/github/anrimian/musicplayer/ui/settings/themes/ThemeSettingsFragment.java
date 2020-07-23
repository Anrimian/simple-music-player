package com.github.anrimian.musicplayer.ui.settings.themes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentSettingsThemesBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.settings.themes.view.ThemesAdapter;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.decorators.DividerItemDecoration;

import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked;

public class ThemeSettingsFragment extends Fragment {

    private FragmentSettingsThemesBinding viewBinding;

    private ThemeController themeController;

    private ThemesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentSettingsThemesBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        themeController = Components.getAppComponent().themeController();

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(R.string.theme);
        toolbar.setTitleClickListener(null);

        SlidrPanel.simpleSwipeBack(viewBinding.nsvContainer, this, toolbar::onStackFragmentSlided);

        viewBinding.rvThemes.setLayoutManager(new LinearLayoutManager(requireContext()));
        DividerItemDecoration itemDecorator = new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL,
                getResources().getDimensionPixelSize(R.dimen.toolbar_content_start),
                false);
        viewBinding.rvThemes.addItemDecoration(itemDecorator);

        adapter = new ThemesAdapter(AppTheme.values(),
                themeController.getCurrentTheme(),
                this::onThemeClicked);
        viewBinding.rvThemes.setAdapter(adapter);

        setChecked(viewBinding.cbAutoNightMode, themeController.isAutoDarkThemeEnabled());
        viewBinding.cbAutoNightMode.setOnCheckedChangeListener((v, isChecked) ->
                themeController.setAutoDarkModeEnabled(requireActivity(), isChecked)
        );
    }

    private void onThemeClicked(AppTheme appTheme) {
        themeController.setTheme(requireActivity(), appTheme);
        adapter.setCurrentTheme(appTheme);
    }
}
