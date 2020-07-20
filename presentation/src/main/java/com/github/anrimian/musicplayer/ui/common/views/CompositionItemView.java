package com.github.anrimian.musicplayer.ui.common.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.github.anrimian.musicplayer.R;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateAlpha;

@SuppressLint("ViewConstructor")
public class CompositionItemView extends View {

    private final int horizontalItemMargin;
    private final int verticalItemMargin;
    private final int coverSize;
    private final int playIconSize;
    private final int titleHorizontalMargin;
    private final int descriptionMarginTop;
    private final float descriptionLineSpacing;
    private final int menuButtonPaddingTop;
    private final int menuButtonPaddingStart;
    private final int menuButtonSize;
    private final int menuButtonIconSize;
    private final int dividerColor;
    private final int dividerHeight = 1;
    private final int dividerVisibleAlpha;

    private final boolean showMenuButton;

    private final Drawable menuButtonDrawable;

    @Nullable
    private Drawable coverDrawable;

    @Nullable
    private Drawable playStateDrawable;

    @Nullable
    private CharSequence title;

    @Nullable
    private CharSequence description;


    private final Path coverPath = new Path();

    private final TextPaint titleTextPaint = new TextPaint();
    private StaticLayout titleStaticLayout;

    private final TextPaint descriptionTextPaint = new TextPaint();
    private StaticLayout descriptionStaticLayout;

    private final Paint dividerPaint = new Paint();

    public CompositionItemView(Context context, boolean showMenuButton) {
        this(context,
                showMenuButton,
                context.getResources().getDimensionPixelSize(R.dimen.content_horizontal_margin),
                context.getResources().getDimensionPixelSize(R.dimen.list_vertical_margin),
                context.getResources().getDimensionPixelSize(R.dimen.cover_item_size),
                context.getResources().getDimensionPixelSize(R.dimen.item_play_icon_size),
                context.getResources().getDimensionPixelSize(R.dimen.content_margin),
                context.getResources().getDimensionPixelSize(R.dimen.content_vertical_spacing_margin),
                context.getResources().getDimension(R.dimen.item_title_text_size),
                getColorFromAttr(context, android.R.attr.textColorPrimary),
                context.getResources().getDimension(R.dimen.item_subtitle_text_size),
                getColorFromAttr(context, android.R.attr.textColorSecondary),
                context.getResources().getDimension(R.dimen.subtitle_line_spacing),
                context.getResources().getDimensionPixelSize(R.dimen.menu_button_padding_top),
                context.getResources().getDimensionPixelSize(R.dimen.content_internal_margin),
                context.getResources().getDimensionPixelSize(R.dimen.menu_button_size),
                ContextCompat.getColor(context, R.color.color_button_secondary),
                context.getResources().getDimensionPixelSize(R.dimen.menu_button_icon_size),
                getColorFromAttr(context, android.R.attr.dividerHorizontal),
                context.getDrawable(R.drawable.ic_dots_vertical));
    }

    public CompositionItemView(Context context,
                               boolean showMenuButton,
                               int horizontalItemMargin,
                               int verticalItemMargin,
                               int coverSize,
                               int playIconSize,
                               int titleHorizontalMargin,
                               int descriptionMarginTop,
                               float titleTextSize,
                               int titleTextColor,
                               float descriptionTextSize,
                               int descriptionTextColor,
                               float descriptionLineSpacing,
                               int menuButtonPaddingTop,
                               int menuButtonPaddingStart,
                               int menuButtonSize,
                               int menuButtonColor,
                               int menuButtonIconSize,
                               int dividerColor,
                               Drawable menuButtonDrawable) {
        super(context);
        this.showMenuButton = showMenuButton;
        this.horizontalItemMargin = horizontalItemMargin;
        this.verticalItemMargin = verticalItemMargin;
        this.coverSize = coverSize;
        this.playIconSize = playIconSize;
        this.titleHorizontalMargin = titleHorizontalMargin;
        this.descriptionMarginTop = descriptionMarginTop;
        this.descriptionLineSpacing = descriptionLineSpacing;
        this.menuButtonPaddingTop = menuButtonPaddingTop;
        this.menuButtonPaddingStart = menuButtonPaddingStart;
        this.menuButtonSize = menuButtonSize;
        this.menuButtonIconSize = menuButtonIconSize;
        this.dividerColor = dividerColor;
        this.menuButtonDrawable = menuButtonDrawable;

        menuButtonDrawable.setTint(menuButtonColor);
        menuButtonDrawable.setBounds(0, 0, menuButtonIconSize, menuButtonIconSize);

        //+line spacing
        titleTextPaint.setTextSize(titleTextSize);
        titleTextPaint.setColor(titleTextColor);
//        titleTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titleTextPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
//        titleTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        titleTextPaint.setAntiAlias(true);

        descriptionTextPaint.setTextSize(descriptionTextSize);
        descriptionTextPaint.setColor(descriptionTextColor);

        dividerPaint.setColor(dividerColor);
        dividerVisibleAlpha = Color.alpha(dividerColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int textWidth = width - horizontalItemMargin*2
                - coverSize
                - titleHorizontalMargin
                - (showMenuButton? menuButtonSize + menuButtonPaddingStart: 0);
        titleStaticLayout = createStaticLayout(title == null? "" : title,
                titleTextPaint,
                textWidth,
                0);
        descriptionStaticLayout = createStaticLayout(description == null? "" : description,
                descriptionTextPaint,
                textWidth,
                descriptionLineSpacing);

        int height = verticalItemMargin*2
                + titleStaticLayout.getHeight()
                + descriptionMarginTop
                + descriptionStaticLayout.getHeight()
                + dividerHeight;

        setMeasuredDimension(width, height);
    }

    private Paint testPaint = new Paint();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(horizontalItemMargin, verticalItemMargin);

        float coverEndX = 0;

        if (coverDrawable != null) {
            canvas.save();

            Rect rect = coverDrawable.getBounds();
            float width = rect.right;
            float height = rect.bottom;
            coverPath.addCircle(width/2, height/2, width/2, Path.Direction.CW);
            canvas.clipPath(coverPath);
            coverDrawable.draw(canvas);

            canvas.restore();

            coverEndX = width;
        }

        if (playStateDrawable != null) {
            canvas.save();

            int startDrawPoint = coverSize/2 - playIconSize/2;
            canvas.translate(startDrawPoint, startDrawPoint);
            playStateDrawable.draw(canvas);

            canvas.restore();
        }

        if (title != null) {
            canvas.save();

            canvas.translate(coverEndX + titleHorizontalMargin, 0);//titleHorizontalMargin = 0 if no covers
            titleStaticLayout.draw(canvas);

            canvas.restore();
        }

        if (description != null) {
            canvas.save();

            int translateY = descriptionMarginTop;
            if (titleStaticLayout != null) {
                translateY += titleStaticLayout.getHeight();
            }
            canvas.translate(coverEndX + titleHorizontalMargin, translateY);
            descriptionStaticLayout.draw(canvas);

            canvas.restore();
        }
        if (showMenuButton) {
            canvas.save();

            canvas.translate(
                    getMeasuredWidth() - menuButtonSize - horizontalItemMargin + menuButtonIconSize/2f,
                    menuButtonPaddingTop - verticalItemMargin + menuButtonIconSize/2f
            );
            menuButtonDrawable.draw(canvas);

            canvas.restore();
        }

        //divider
        canvas.save();

        canvas.drawRect(
                coverEndX + titleHorizontalMargin,
                getMeasuredHeight() - dividerHeight - verticalItemMargin,
                getMeasuredWidth(),
                getMeasuredHeight(),
                dividerPaint);

        canvas.restore();
    }

    public void setTitle(CharSequence title) {
        this.title = title;
        invalidate();
    }

    public void setDescription(CharSequence text) {
        this.description = text;
        invalidate();
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

    public void setDividerVisible(boolean visible) {
        animateAlpha(this, visible? dividerVisibleAlpha: 0, dividerPaint);
    }

    @Nullable
    public Drawable getCoverDrawable() {
        return coverDrawable;
    }

    private static StaticLayout createStaticLayout(CharSequence text,
                                                   TextPaint textPaint,
                                                   int width,
                                                   float spacingAdd) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return StaticLayout.Builder.obtain(text, 0, text.length(), textPaint, width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(spacingAdd, 1.0f)
                    .build();
        }
        return new StaticLayout(text,
                textPaint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                spacingAdd,
                false);
    }
}
