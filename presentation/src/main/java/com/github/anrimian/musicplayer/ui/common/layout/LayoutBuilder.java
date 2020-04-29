package com.github.anrimian.musicplayer.ui.common.layout;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.annotation.StringRes;

import com.github.anrimian.musicplayer.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class LayoutBuilder {

    public static TextView simpleTextView(Context context, @StringRes int text) {
        TextView textView = new TextView(context, null, 0, R.style.TextStyleButton);
        textView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        textView.setText(text);
        return textView;
    }

    public static View divider(Context context) {
        return divider(context, R.dimen.toolbar_content_start);
    }

    public static View divider(Context context, @DimenRes int marginStartRes) {
        View view = new View(context, null, 0, R.style.Divider_Horizontal);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, 1);
        params.setMarginStart(context.getResources().getDimensionPixelSize(marginStartRes));
        view.setLayoutParams(params);

        return view;
    }

}
