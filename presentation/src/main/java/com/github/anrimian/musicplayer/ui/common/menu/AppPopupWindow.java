package com.github.anrimian.musicplayer.ui.common.menu;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.github.anrimian.musicplayer.R;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

@SuppressWarnings("WeakerAccess")
public class AppPopupWindow {

    public static PopupWindow showPopupWindow(View anchorView,
                                              View popupView,
                                              int gravity,
                                              int screenMargin) {
        Context context = anchorView.getContext();

        //margins
        FrameLayout popupViewWrapper = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.setMargins(screenMargin, screenMargin, screenMargin, screenMargin);
        popupViewWrapper.addView(popupView, params);

        popupView.setElevation(5f);

        PopupWindow popupWindow = new PopupWindow(popupViewWrapper,
                WRAP_CONTENT,
                WRAP_CONTENT,
                true);

        popupWindow.setAnimationStyle(R.style.PopupAnimationStyle);

        popupView.measure(
                makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );


        int[] location = new int[2];
        anchorView.getLocationInWindow(location);
        int anchorX = location[0];
        int anchorY = location[1];

        int anchorWidth = anchorView.getMeasuredWidth();
        int anchorHeight = anchorView.getMeasuredHeight();

        int viewWidth = popupView.getMeasuredWidth();
        int viewHeight = popupView.getMeasuredHeight();

        int showX = anchorX - viewWidth - screenMargin;
        int showY = anchorY - screenMargin;

        switch (gravity) {
            case Gravity.CENTER: {
                showX += anchorWidth;
                break;
            }
            case Gravity.BOTTOM: {
                showX += viewWidth;
                showY += anchorHeight;
                break;
            }
        }

        popupWindow.showAtLocation(anchorView, Gravity.START | Gravity.TOP, showX, showY);

        return popupWindow;
    }

}
