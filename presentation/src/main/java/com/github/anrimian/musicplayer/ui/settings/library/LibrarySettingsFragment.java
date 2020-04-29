package com.github.anrimian.musicplayer.ui.settings.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 19.10.2017.
 */

public class LibrarySettingsFragment extends Fragment implements FragmentLayerListener {

    @BindView(R.id.fl_container)
    View flContainer;

    @BindView(R.id.tv_excluded_folders)
    TextView tvExcludedFolders;

    private FragmentNavigation navigation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);

        navigation = FragmentNavigation.from(requireFragmentManager());

        tvExcludedFolders.setOnClickListener(v -> navigation.addNewFragment(new ExcludedFoldersFragment()));

        SlidrPanel.simpleSwipeBack(flContainer, this, toolbar::onStackFragmentSlided);
    }

    @Override
    public void onFragmentMovedOnTop() {
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(R.string.library);
        toolbar.setTitleClickListener(null);
    }
}
