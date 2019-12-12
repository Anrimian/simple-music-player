package com.github.anrimian.musicplayer.ui.common.layout;

import android.content.Context;
import android.view.View;

import com.github.anrimian.musicplayer.R;

public class LayoutBuilder {

    public static View divider(Context context) {
        View view = new View(context, null, 0, R.style.Divider_Horizontal);
        return view;
    }
}
