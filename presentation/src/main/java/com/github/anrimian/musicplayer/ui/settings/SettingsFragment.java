package com.github.anrimian.musicplayer.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.layout.LayoutBuilder;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.settings.display.DisplaySettingsFragment;
import com.github.anrimian.musicplayer.ui.settings.headset.HeadsetSettingsFragment;
import com.github.anrimian.musicplayer.ui.settings.library.LibrarySettingsFragment;
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsFragment;
import com.github.anrimian.musicplayer.ui.settings.themes.ThemeSettingsFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

/**
 * Created on 19.10.2017.
 */

public class SettingsFragment extends Fragment implements FragmentLayerListener {

    private View flContainer;
    private TextView tvDisplay;
    private TextView tvPlayer;
    private TextView tvTheme;
    private TextView tvHeadset;
    private TextView tvLibrary;

    private FragmentNavigation navigation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(getColorFromAttr(context, android.R.attr.colorBackground));

        flContainer = linearLayout;
        frameLayout.addView(linearLayout);

        tvDisplay = LayoutBuilder.simpleTextView(context, R.string.display);
        linearLayout.addView(tvDisplay);

        linearLayout.addView(LayoutBuilder.divider(context));

        tvLibrary = LayoutBuilder.simpleTextView(context, R.string.library);
        linearLayout.addView(tvLibrary);

        linearLayout.addView(LayoutBuilder.divider(context));

        tvPlayer = LayoutBuilder.simpleTextView(context, R.string.playing);
        linearLayout.addView(tvPlayer);

        linearLayout.addView(LayoutBuilder.divider(context));

        tvHeadset = LayoutBuilder.simpleTextView(context, R.string.headset);
        linearLayout.addView(tvHeadset);

        linearLayout.addView(LayoutBuilder.divider(context));

        tvTheme = LayoutBuilder.simpleTextView(context, R.string.theme);
        linearLayout.addView(tvTheme);

        return frameLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);

        navigation = FragmentNavigation.from(requireFragmentManager());

        tvDisplay.setOnClickListener(v -> navigation.addNewFragment(new DisplaySettingsFragment()));
        tvLibrary.setOnClickListener(v -> navigation.addNewFragment(new LibrarySettingsFragment()));
        tvPlayer.setOnClickListener(v -> navigation.addNewFragment(new PlayerSettingsFragment()));
        tvTheme.setOnClickListener(v -> navigation.addNewFragment(new ThemeSettingsFragment()));
        tvHeadset.setOnClickListener(v -> navigation.addNewFragment(new HeadsetSettingsFragment()));

        SlidrPanel.simpleSwipeBack(flContainer, this, toolbar::onStackFragmentSlided);
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
