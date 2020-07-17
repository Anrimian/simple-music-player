package com.github.anrimian.musicplayer.ui.common.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.github.anrimian.musicplayer.R;

public class CompositionItemView extends View {

    private final int horizontalItemMargin;
    private final int verticalItemMargin;
    private final int coverSize;
    private final int playIconSize;

    @Nullable
    private Drawable coverDrawable;

    @Nullable
    private Drawable playStateDrawable;

    public CompositionItemView(Context context) {
        this(context,
                context.getResources().getDimensionPixelSize(R.dimen.content_horizontal_margin),
                context.getResources().getDimensionPixelSize(R.dimen.list_vertical_margin),
                context.getResources().getDimensionPixelSize(R.dimen.cover_item_size),
                context.getResources().getDimensionPixelSize(R.dimen.item_play_icon_size));
    }

    public CompositionItemView(Context context,
                               int horizontalItemMargin,
                               int verticalItemMargin,
                               int coverSize,
                               int playIconSize) {
        super(context);
        this.horizontalItemMargin = horizontalItemMargin;
        this.verticalItemMargin = verticalItemMargin;
        this.coverSize = coverSize;
        this.playIconSize = playIconSize;
    }

    private void init() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = horizontalItemMargin*2 + coverSize;

        setMeasuredDimension(width, height);
    }

    private Path coverPath = new Path();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(horizontalItemMargin, verticalItemMargin);

        if (coverDrawable != null) {
            canvas.save();

            Rect rect = coverDrawable.getBounds();
            float width = rect.right;
            float height = rect.bottom;
            coverPath.addCircle(width/2, height/2, width/2, Path.Direction.CW);
            canvas.clipPath(coverPath);
            coverDrawable.draw(canvas);

            canvas.restore();
        }

        if (playStateDrawable != null) {
            canvas.save();

            int startDrawPoint = coverSize/2 - playIconSize/2;
            canvas.translate(startDrawPoint, startDrawPoint);
            playStateDrawable.draw(canvas);

            canvas.restore();
        }
    }

    public void setAnimatedPlayStateDrawable(@DrawableRes int drawableRes,
                                             boolean animate) {
        Drawable drawable = AppCompatResources.getDrawable(getContext(), drawableRes);
        Integer tag = (Integer) getTag();
        if (tag != null && tag == drawableRes) {
            return;
        }
        setTag(drawableRes);
        setPlayStateDrawable(drawable);
        if (animate && tag != null && drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

    public void setPlayStateDrawable(@Nullable Drawable drawable) {
        this.playStateDrawable = drawable;
        if (playStateDrawable != null) {
            playStateDrawable.setBounds(0, 0, playIconSize, playIconSize);
            playStateDrawable.setTint(Color.WHITE);
        }
        invalidate();
    }

    public void setCoverDrawable(@DrawableRes int drawableRes) {
        setCoverDrawable(ContextCompat.getDrawable(getContext(), drawableRes));
    }

    public void setCoverDrawable(@Nullable Drawable drawable) {
        this.coverDrawable = drawable;
        if (coverDrawable != null) {
            coverDrawable.setBounds(0, 0, coverSize, coverSize);
        }
        invalidate();
    }

    @Nullable
    public Drawable getCoverDrawable() {
        return coverDrawable;
    }
}
