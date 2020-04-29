package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_swipe;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop.DragListener;

import static androidx.recyclerview.widget.ItemTouchHelper.DOWN;
import static androidx.recyclerview.widget.ItemTouchHelper.UP;

public class DragAndSwipeTouchHelperCallback extends ItemTouchHelper.Callback{

    private static final float DRAG_BOTTOM_Z = 0f;
    private static final float DRAG_TOP_Z = 8f;

    private static final int SWIPE_EFFECT_ANIMATION_TIME = 400;

    private final Callback<Integer> swipeCallback;

    private final int swipeFlags;
    private final int dragFlags;

    private OnMovedListener onMovedListener;
    private OnStartDragListener onStartDragListener;
    private OnEndDragListener onEndDragListener;

    private boolean dragging;
    private Boolean swipedFromSwipeEdge;

    private final Paint bgPaint = new Paint();
    private final Paint bgAnimationPaint = new Paint();

    @ColorInt
    private final int regularBgColor;
    @ColorInt
    private final int unswipedBgColor;

    private final StaticLayout textStaticLayout;

    private final Drawable icon;

    private final int panelWidth;
    private final int panelEndPadding;
    private final int textTopPadding;
    private final int iconSize;

    @Nullable
    private ValueAnimator swipeEffectAnimator;

    public static DragAndSwipeTouchHelperCallback withSwipeToDelete(RecyclerView recyclerView,
                                                                    @ColorInt int backgroundColor,
                                                                    Callback<Integer> swipeCallback,
                                                                    int swipeFlags,
                                                                    @DrawableRes int iconRes,
                                                                    @StringRes int textResId,
                                                                    @DimenRes int panelWidthRes,
                                                                    @DimenRes int panelEndPaddingRes,
                                                                    @DimenRes int textTopPaddingRes,
                                                                    @DimenRes int iconSizeRes,
                                                                    @DimenRes int textSizeRes) {
        return new DragAndSwipeTouchHelperCallback(backgroundColor,
                swipeCallback,
                swipeFlags,
                iconRes,
                textResId,
                panelWidthRes,
                panelEndPaddingRes,
                textTopPaddingRes,
                iconSizeRes,
                textSizeRes,
                recyclerView.getContext());
    }

    public DragAndSwipeTouchHelperCallback(@ColorInt int color,
                                           Callback<Integer> swipeCallback,
                                           int swipeFlags,
                                           @DrawableRes int iconRes,
                                           @StringRes int textResId,
                                           @DimenRes int panelWidthRes,
                                           @DimenRes int panelEndPaddingRes,
                                           @DimenRes int textTopPaddingRes,
                                           @DimenRes int iconSizeRes,
                                           @DimenRes int textSizeRes,
                                           Context context) {
        this(color,
                swipeCallback,
                swipeFlags,
                UP | DOWN,
                iconRes,
                textResId,
                panelWidthRes,
                panelEndPaddingRes,
                textTopPaddingRes,
                iconSizeRes,
                textSizeRes,
                context);
    }

    public DragAndSwipeTouchHelperCallback(@ColorInt int regularBgColor,
                                           Callback<Integer> swipeCallback,
                                           int swipeFlags,
                                           int dragFlags,
                                           @DrawableRes int iconRes,
                                           @StringRes int textResId,
                                           @DimenRes int panelWidthRes,
                                           @DimenRes int panelEndPaddingRes,
                                           @DimenRes int textTopPaddingRes,
                                           @DimenRes int iconSizeRes,
                                           @DimenRes int textSizeRes,
                                           Context context) {
        this.regularBgColor = regularBgColor;
        this.swipeFlags = swipeFlags;
        this.dragFlags = dragFlags;
        this.swipeCallback = swipeCallback;

        Resources resources = context.getResources();

        unswipedBgColor = AndroidUtils.getColorFromAttr(context, R.attr.colorAccent);
        icon = resources.getDrawable(iconRes);
        iconSize = resources.getDimensionPixelSize(iconSizeRes);
        textTopPadding = resources.getDimensionPixelSize(textTopPaddingRes);
        panelEndPadding = resources.getDimensionPixelSize(panelEndPaddingRes);
        panelWidth = resources.getDimensionPixelSize(panelWidthRes);

        //canvas
        int textColor = Color.WHITE;
        bgPaint.setColor(regularBgColor);
        icon.setTint(textColor);
        icon.setBounds(0,
                0,
                iconSize,
                iconSize);

        //canvas
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(textColor);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(resources.getDimension(textSizeRes));

        String text = context.getString(textResId);
        textStaticLayout = new StaticLayout(text,
                textPaint,
                panelWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.0f,
                0.0f,
                false);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            dragging = true;
            setIsDragging(viewHolder, true);
            if (onStartDragListener != null) {
                onStartDragListener.onStartDrag(viewHolder.getAdapterPosition());
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder) {
        if (dragging) {
            dragging = false;
            if (onEndDragListener != null) {
                onEndDragListener.onEndDrag(viewHolder.getAdapterPosition());
            }
            setIsDragging(viewHolder, false);
        }
        swipedFromSwipeEdge = null;
        if (swipeEffectAnimator != null) {
            swipeEffectAnimator.cancel();
        }
        super.clearView(recyclerView, viewHolder);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        if (onMovedListener != null) {
            onMovedListener.onItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        swipeCallback.call(viewHolder.getAdapterPosition());
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return defaultValue * 8;
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.33f;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;
            float top = itemView.getTop();
            float bottom = itemView.getBottom();
            float left = dX > 0? itemView.getLeft() : itemView.getRight() + dX;
            float right = dX > 0? dX : itemView.getRight();
            float centerY = top + (itemView.getHeight()/2f);
            float centerX = panelWidth >> 1;

            boolean draggedFromSwipeEdge = Math.abs(dX) > itemView.getWidth() * getSwipeThreshold(viewHolder);
            if (this.swipedFromSwipeEdge == null) {
                this.swipedFromSwipeEdge = draggedFromSwipeEdge;
                bgPaint.setColor(draggedFromSwipeEdge? regularBgColor: unswipedBgColor);
            } else if (this.swipedFromSwipeEdge != draggedFromSwipeEdge) {
                this.swipedFromSwipeEdge = draggedFromSwipeEdge;
                AndroidUtils.playShortVibration(recyclerView.getContext());

                bgAnimationPaint.setColor(regularBgColor);

                float currentAnimationValue = -1;
                long currentAnimationPlayTime = 0;
                if (swipeEffectAnimator != null && swipeEffectAnimator.isRunning()) {
                    currentAnimationValue = (float) swipeEffectAnimator.getAnimatedValue();
                    currentAnimationPlayTime = swipeEffectAnimator.getCurrentPlayTime();
                    swipeEffectAnimator.cancel();
                }

                int maxEndValue = itemView.getMeasuredWidth();
                float start = currentAnimationValue == -1? (draggedFromSwipeEdge? 0 : maxEndValue) : currentAnimationValue;
                float end = draggedFromSwipeEdge? maxEndValue : 0;
                swipeEffectAnimator = ValueAnimator.ofFloat(start, end);
                swipeEffectAnimator.setDuration(currentAnimationPlayTime == 0? SWIPE_EFFECT_ANIMATION_TIME : currentAnimationPlayTime);
                swipeEffectAnimator.addUpdateListener(valueAnimator -> recyclerView.invalidate());
                swipeEffectAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        bgPaint.setColor(draggedFromSwipeEdge? regularBgColor: unswipedBgColor);
                    }
                });
                swipeEffectAnimator.setInterpolator(draggedFromSwipeEdge? new AccelerateInterpolator(): new DecelerateInterpolator());
                swipeEffectAnimator.start();

                if (!draggedFromSwipeEdge) {
                    bgPaint.setColor(unswipedBgColor);
                }
            }

            c.clipRect(left, top, right, bottom);


            //draw bg
            c.drawRect(left, top, right, bottom, bgPaint);

            if (swipeEffectAnimator != null && swipeEffectAnimator.isRunning()) {
                c.drawCircle(itemView.getRight() - ((centerX + panelEndPadding)), centerY, (float) swipeEffectAnimator.getAnimatedValue(), bgAnimationPaint);
            }

            //draw icon
            c.save();
            c.translate(itemView.getRight() - ((centerX + panelEndPadding) + (iconSize >> 1)), centerY - iconSize);
            icon.draw(c);
            c.restore();

            //draw text
            c.translate(itemView.getRight() - (panelWidth + panelEndPadding), centerY + textTopPadding);
            textStaticLayout.draw(c);
            c.restore();
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    public void setOnMovedListener(OnMovedListener onMovedListener) {
        this.onMovedListener = onMovedListener;
    }

    public void setOnStartDragListener(OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
    }

    public void setOnEndDragListener(OnEndDragListener onEndDragListener) {
        this.onEndDragListener = onEndDragListener;
    }

    private void setIsDragging(RecyclerView.ViewHolder viewHolder, boolean dragging) {
        if (viewHolder == null) {
            return;
        }
        if (viewHolder instanceof DragListener) {
            ((DragListener) viewHolder).onDragStateChanged(dragging);
        }
        float dragElevation = dragging? DRAG_TOP_Z : DRAG_BOTTOM_Z;
        View itemView = viewHolder.itemView;
        ObjectAnimator elevationAnimator = ObjectAnimator.ofFloat(itemView,
                "translationZ",
                itemView.getElevation(),
                dragElevation);
        elevationAnimator.setDuration(DEFAULT_DRAG_ANIMATION_DURATION);
        elevationAnimator.start();
    }

    public interface OnMovedListener {
        void onItemMoved(int from, int to);
    }

    public interface OnStartDragListener {
        void onStartDrag(int position);
    }

    public interface OnEndDragListener {
        void onEndDrag(int position);
    }

}
