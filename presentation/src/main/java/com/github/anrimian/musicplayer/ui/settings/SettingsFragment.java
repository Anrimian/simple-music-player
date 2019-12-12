package com.github.anrimian.musicplayer.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsFragment;
import com.github.anrimian.musicplayer.ui.settings.themes.ThemeSettingsFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.BindView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

/**
 * Created on 19.10.2017.
 */

public class SettingsFragment extends Fragment implements FragmentLayerListener {

    @BindView(R.id.fl_container)
    View flContainer;

    @BindView(R.id.tv_display)
    TextView tvDisplay;

    @BindView(R.id.tv_player)
    TextView tvPlayer;

    @BindView(R.id.tv_theme_name)
    TextView tvTheme;

    @BindView(R.id.tv_headset)
    TextView tvHeadset;

    private FragmentNavigation navigation;

    long start;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        start = System.currentTimeMillis();

        Context context = requireContext();
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
//        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
//        linearLayout.setDividerPadding();
//        linearLayout.setDividerPadding(200);
//        linearLayout.setDividerDrawable(ContextCompat.getDrawable(context, R.drawable.divider));
        linearLayout.setBackgroundColor(getColorFromAttr(context, android.R.attr.colorBackground));
        flContainer = linearLayout;
        frameLayout.addView(linearLayout);

        TextView textView = new TextView(context, null, 0, R.style.TextStyleButton);
        textView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        textView.setText(R.string.display);
        tvDisplay = textView;
        linearLayout.addView(textView);

        View divider = LayoutBuilder.divider(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, 1);
        params.setMarginStart(context.getResources().getDimensionPixelSize(R.dimen.toolbar_content_start));
        divider.setLayoutParams(params);
        linearLayout.addView(divider);

        textView = new TextView(context, null, 0, R.style.TextStyleButton);
        textView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        textView.setText(R.string.playing);
        tvPlayer = textView;
        linearLayout.addView(textView);

        divider = LayoutBuilder.divider(context);
        params = new LinearLayout.LayoutParams(MATCH_PARENT, 1);
        params.setMarginStart(context.getResources().getDimensionPixelSize(R.dimen.toolbar_content_start));
        divider.setLayoutParams(params);
        linearLayout.addView(divider);

        textView = new TextView(context, null, 0, R.style.TextStyleButton);
        textView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        textView.setText(R.string.headset);
        tvHeadset = textView;
        linearLayout.addView(textView);

        divider = LayoutBuilder.divider(context);
        params = new LinearLayout.LayoutParams(MATCH_PARENT, 1);
        params.setMarginStart(context.getResources().getDimensionPixelSize(R.dimen.toolbar_content_start));
        divider.setLayoutParams(params);
        linearLayout.addView(divider);

        textView = new TextView(context, null, 0, R.style.TextStyleButton);
        textView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        textView.setText(R.string.theme);
        tvTheme = textView;
        linearLayout.addView(textView);

        return frameLayout;

//        View view = inflater.inflate(R.layout.fragment_settings, container, false);
//        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ButterKnife.bind(this, view);

        Log.d("KEK3", "view created for: " + (System.currentTimeMillis() - start) + "ms");

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);

        navigation = FragmentNavigation.from(requireFragmentManager());

        tvDisplay.setOnClickListener(v -> navigation.addNewFragment(new DisplaySettingsFragment()));
        tvPlayer.setOnClickListener(v -> navigation.addNewFragment(new PlayerSettingsFragment()));
        tvTheme.setOnClickListener(v -> navigation.addNewFragment(new ThemeSettingsFragment()));
        tvHeadset.setOnClickListener(v -> navigation.addNewFragment(new HeadsetSettingsFragment()));

        SlidrConfig slidrConfig = new SlidrConfig.Builder().position(SlidrPosition.LEFT).build();
        SlidrPanel.replace(flContainer,
                slidrConfig,
                () -> navigation.goBack(0),
                toolbar::onStackFragmentSlided);
    }

    @Override
    public void onFragmentMovedOnTop() {
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(null);
        toolbar.setTitleClickListener(null);
    }
}
