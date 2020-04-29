package com.github.anrimian.musicplayer.ui.utils.slidr;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;
import com.r0adkll.slidr.widget.SliderPanel;

public class SlidrPanel {

    public static SlidrInterface simpleSwipeBack(@NonNull View oldScreen,
                                                 @NonNull Fragment fragment,
                                                 @Nullable SlideListener slideListener) {
        SlidrConfig slidrConfig = new SlidrConfig.Builder().position(SlidrPosition.LEFT).build();
        return SlidrPanel.replace(oldScreen,
                slidrConfig,
                () -> FragmentNavigation.from(fragment.requireFragmentManager()).goBack(0),
                slideListener);
    }

    @NonNull
    public static SlidrInterface replace(@NonNull View oldScreen,
                                         @NonNull Runnable onClose,
                                         @NonNull SlidrConfig config) {
        return replace(oldScreen, config, onClose, null);
    }

    @NonNull
    public static SlidrInterface replace(@NonNull View oldScreen,
                                         @NonNull SlidrConfig config,
                                         @NonNull Runnable onClose,
                                         @Nullable SlideListener slideListener) {
        ViewGroup parent = (ViewGroup) oldScreen.getParent();
        ViewGroup.LayoutParams params = oldScreen.getLayoutParams();
        parent.removeView(oldScreen);

        // Setup the slider panel and attach it
        final SliderPanel panel = new SliderPanel(oldScreen.getContext(), oldScreen, config);
        panel.setId(com.r0adkll.slidr.R.id.slidable_panel);
        oldScreen.setId(com.r0adkll.slidr.R.id.slidable_content);

        panel.addView(oldScreen);
        parent.addView(panel, 0, params);

        // Set the panel slide listener for when it becomes closed or opened
        panel.setOnPanelSlideListener(new PanelSlideListener(onClose, config, slideListener));

        // Return the lock interface
        return panel.getDefaultInterface();
    }

    @NonNull
    public static SlidrInterface attachWithNavBarChange(@NonNull Activity activity,
                                                        @AttrRes int prevScreenColorAttr,
                                                        @AttrRes int curScreenColorAttr) {

        // Setup the slider panel and attach it to the decor
        final SliderPanel panel = attachSliderPanel(activity, null);

        // Set the panel slide listener for when it becomes closed or opened
        panel.setOnPanelSlideListener(new ColorNavBarSlideListener(activity,
                prevScreenColorAttr,
                curScreenColorAttr)
        );

        // Return the lock interface
        return panel.getDefaultInterface();
    }

    @NonNull
    private static SliderPanel attachSliderPanel(@NonNull Activity activity, SlidrConfig config) {
        // Hijack the decorview
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        View oldScreen = decorView.getChildAt(0);
        decorView.removeViewAt(0);

        // Setup the slider panel and attach it to the decor
        SliderPanel panel = new SliderPanel(activity, oldScreen, config);
        panel.setId(com.r0adkll.slidr.R.id.slidable_panel);
        oldScreen.setId(com.r0adkll.slidr.R.id.slidable_content);
        panel.addView(oldScreen);
        decorView.addView(panel, 0);
        return panel;
    }

    public interface SlideListener {
        void onSlideChange(float percent);
    }
}
