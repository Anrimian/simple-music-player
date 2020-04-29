package com.github.anrimian.musicplayer.ui.utils.views.coordinator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Created on 25.02.2018.
 */

public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {

    private static final int TRANSLATE_HIDE_DURATION_MILLIS = 200;
    private static final int TRANSLATE_SHOW_DURATION_MILLIS = 170;

    private Animator hideAnimator;
    private Animator showAnimator;

    private boolean isAttachedToRecyclerView = false;

    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull FloatingActionButton child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target,
                                       int axes,
                                       int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent,
                                          @NonNull FloatingActionButton child,
                                          View dependency) {
        if (!isAttachedToRecyclerView) {
            RecyclerView recyclerView = (RecyclerView) dependency;

            int height = child.getHeight();
            int margin = child.getResources().getDimensionPixelSize(R.dimen.content_vertical_margin);
            recyclerView.setPadding(recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getPaddingRight(),
                    height + margin * 2);
            recyclerView.setClipToPadding(false);

            isAttachedToRecyclerView = true;
        }
        return false;
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                               @NonNull FloatingActionButton fab,
                               @NonNull View target,
                               int dxConsumed,
                               int dyConsumed,
                               int dxUnconsumed,
                               int dyUnconsumed,
                               int type,
                               @NonNull int[] consumed) {
        super.onNestedScroll(coordinatorLayout, fab, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed, type, consumed);
        if (type == ViewCompat.TYPE_TOUCH) {
            if (dyConsumed > 0 && hideAnimator == null) {
                if (showAnimator != null ) {
                    showAnimator.cancel();
                }

                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                float translation = fab.getHeight() +  params.bottomMargin;
                hideAnimator = ObjectAnimator.ofFloat(fab, "translationY", fab.getTranslationY(), translation);
                hideAnimator.setDuration(TRANSLATE_HIDE_DURATION_MILLIS);
                hideAnimator.setInterpolator(new AccelerateInterpolator());
                hideAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        hideAnimator = null;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        hideAnimator = null;
                    }
                });
                hideAnimator.start();
            } else if (dyConsumed < 0 && showAnimator == null) {
                if (hideAnimator != null) {
                    hideAnimator.cancel();
                }

                showAnimator = ObjectAnimator.ofFloat(fab, "translationY", fab.getTranslationY(), 0);
                showAnimator.setDuration(TRANSLATE_SHOW_DURATION_MILLIS);
                showAnimator.setInterpolator(new DecelerateInterpolator());
                showAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        showAnimator = null;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        showAnimator = null;
                    }
                });
                showAnimator.start();
            }
        }
    }
}
