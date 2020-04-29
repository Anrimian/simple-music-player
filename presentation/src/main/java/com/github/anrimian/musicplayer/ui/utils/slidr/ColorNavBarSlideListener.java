package com.github.anrimian.musicplayer.ui.utils.slidr;

import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.os.Build;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.r0adkll.slidr.widget.SliderPanel;

class ColorNavBarSlideListener implements SliderPanel.OnPanelSlideListener {

    private final Activity activity;
    private final int prevScreenColorAttr;
    private final int curScreenColorAttr;

    private final ArgbEvaluator evaluator = new ArgbEvaluator();

    ColorNavBarSlideListener(Activity activity,
                             @AttrRes int prevScreenColorAttr,
                             @AttrRes int curScreenColorAttr) {
        this.activity = activity;
        this.prevScreenColorAttr = prevScreenColorAttr;
        this.curScreenColorAttr = curScreenColorAttr;
    }

    @Override
    public void onStateChanged(int state) {
        // Unused.
    }

    @Override
    public void onClosed() {
        activity.finish();
        activity.overridePendingTransition(0, 0);
    }

    @Override
    public void onOpened() {
        // Unused.
    }

    @Override
    public void onSlideChange(float percent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && areColorsValid()) {
            int newColor = (int) evaluator.evaluate(percent, getPreviousColor(), getCurrentColor());
            AndroidUtils.setNavigationBarColor(activity, newColor);
        }
    }

    private int getPreviousColor() {
        return AndroidUtils.getColorFromAttr(activity, prevScreenColorAttr);
    }

    private int getCurrentColor() {
        return AndroidUtils.getColorFromAttr(activity, curScreenColorAttr);
    }

    private boolean areColorsValid() {
        return getPreviousColor() != -1 && getCurrentColor() != -1;
    }
}