package com.github.anrimian.musicplayer.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentSettingsBinding;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.settings.display.DisplaySettingsFragment;
import com.github.anrimian.musicplayer.ui.settings.headset.HeadsetSettingsFragment;
import com.github.anrimian.musicplayer.ui.settings.library.LibrarySettingsFragment;
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsFragment;
import com.github.anrimian.musicplayer.ui.settings.themes.ThemeSettingsFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;

/**
 * Created on 19.10.2017.
 */

public class SettingsFragment extends Fragment implements FragmentLayerListener {

    private FragmentSettingsBinding viewBinding;

    private FragmentNavigation navigation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentSettingsBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);

        navigation = FragmentNavigation.from(requireFragmentManager());

        viewBinding.tvDisplay.setOnClickListener(v -> navigation.addNewFragment(new DisplaySettingsFragment()));
        viewBinding.tvLibrary.setOnClickListener(v -> navigation.addNewFragment(new LibrarySettingsFragment()));
        viewBinding.tvPlayer.setOnClickListener(v -> navigation.addNewFragment(new PlayerSettingsFragment()));
        viewBinding.tvTheme.setOnClickListener(v -> navigation.addNewFragment(new ThemeSettingsFragment()));
        viewBinding.tvHeadset.setOnClickListener(v -> navigation.addNewFragment(new HeadsetSettingsFragment()));

        SlidrPanel.simpleSwipeBack(viewBinding.flContainer, this, toolbar::onStackFragmentSlided);
    }

    @Override
    public void onFragmentMovedOnTop() {
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(null);
        toolbar.setTitleClickListener(null);
        toolbar.clearOptionsMenu();
    }
}
