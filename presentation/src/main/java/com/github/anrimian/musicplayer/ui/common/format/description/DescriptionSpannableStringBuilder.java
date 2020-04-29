package com.github.anrimian.musicplayer.ui.common.format.description;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;

public class DescriptionSpannableStringBuilder extends SpannableStringBuilder {

    private static final int DEFAULT_DIVIDER = R.drawable.ic_description_text_circle;

    private final Context context;
    private final @DrawableRes int dividerDrawableRes;

    public DescriptionSpannableStringBuilder(Context context) {
        this.context = context;
        dividerDrawableRes = DEFAULT_DIVIDER;
    }

    public DescriptionSpannableStringBuilder(Context context, @DrawableRes int dividerDrawableRes) {
        this.context = context;
        this.dividerDrawableRes = dividerDrawableRes;
    }

    public DescriptionSpannableStringBuilder(Context context, CharSequence text) {
        super(text);
        this.context = context;
        dividerDrawableRes = DEFAULT_DIVIDER;
    }

    public DescriptionSpannableStringBuilder(Context context, CharSequence text, int start, int end) {
        super(text, start, end);
        this.context = context;
        dividerDrawableRes = DEFAULT_DIVIDER;
    }

    @NonNull
    @Override
    public SpannableStringBuilder append(CharSequence text) {
        if (length() > 0) {
//            super.append(" ● ");//just for compare
            super.append("  \u00A0");//3rd character not space for line break handling

            ImageSpan imageSpan = new CenteredImageSpan(context, dividerDrawableRes);
            setSpan(imageSpan, length() - 2, length() - 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return super.append(text);
    }
}
