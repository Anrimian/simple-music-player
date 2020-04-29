package com.github.anrimian.musicplayer.ui.player_screen.view.wrappers;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.player_screen.view.slide.ToolbarDelegate;
import com.github.anrimian.musicplayer.ui.player_screen.view.slide.ToolbarVisibilityDelegate;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.JugglerView;
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.BoundValuesDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.DelegateManager;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ExpandViewDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.LeftBottomShadowDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MotionLayoutDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MoveXDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MoveYDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ReverseDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.TextSizeDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.VisibilityDelegate;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.INVISIBLE;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getResourceIdFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.run;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

public class PlayerPanelWrapperImpl implements PlayerPanelWrapper {

    @Nullable
    @BindView(R.id.coordinator_bottom_sheet)
    CoordinatorLayout bottomSheetCoordinator;

    @Nullable
    @BindView(R.id.bottom_sheet_left_shadow)
    View bottomSheetLeftShadow;

    @Nullable
    @BindView(R.id.bottom_sheet_top_left_shadow)
    View bottomSheetTopLeftShadow;

    @BindView(R.id.iv_play_pause)
    ImageView ivPlayPause;

    @BindView(R.id.iv_skip_to_previous)
    ImageView ivSkipToPrevious;

    @BindView(R.id.iv_skip_to_next)
    ImageView ivSkipToNext;

    @BindView(R.id.drawer_fragment_container)
    JugglerView fragmentContainer;

    @BindView(R.id.tv_current_composition)
    TextView tvCurrentComposition;

    @BindView(R.id.btn_infinite_play)
    ImageView btnRepeatMode;

    @BindView(R.id.btn_random_play)
    ImageView btnRandomPlay;

    @BindView(R.id.tv_played_time)
    TextView tvPlayedTime;

    @BindView(R.id.tv_total_time)
    TextView tvTotalTime;

    @BindView(R.id.sb_track_state)
    AppCompatSeekBar sbTrackState;

    @BindView(R.id.iv_music_icon)
    ImageView ivMusicIcon;

    @BindView(R.id.btn_actions_menu)
    ImageView btnActionsMenu;

    @BindView(R.id.tv_current_composition_author)
    TextView tvCurrentCompositionAuthor;

    @BindView(R.id.cl_play_queue_container)
    CoordinatorLayout clPlayQueueContainer;

    @BindView(R.id.ml_bottom_sheet)
    MotionLayout mlBottomSheet;

    @BindView(R.id.toolbar)
    AdvancedToolbar toolbar;

    @BindView(R.id.toolbar_content_container)
    View titleContainer;

    @BindView(R.id.toolbar_play_queue)
    View playQueueTitleContainer;

    @BindView(R.id.title_container)
    View toolbarTitleContainer;

    private final Activity activity;
    private final Runnable onBottomSheetDragCollapsed;
    private final Runnable onBottomSheetDragExpanded;
    private final Callback<Boolean> bottomSheetStateListener;

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private SlideDelegate bottomSheetDelegate;

    public PlayerPanelWrapperImpl(View view,
                                  Activity activity,
                                  Bundle savedInstanceState,
                                  Runnable onBottomSheetDragCollapsed,
                                  Runnable onBottomSheetDragExpanded,
                                  Callback<Boolean> bottomSheetStateListener) {
        this.activity = activity;
        this.onBottomSheetDragCollapsed = onBottomSheetDragCollapsed;
        this.onBottomSheetDragExpanded = onBottomSheetDragExpanded;
        this.bottomSheetStateListener = bottomSheetStateListener;

        ButterKnife.bind(this, view);

        setViewStartState();
        if (savedInstanceState == null) {
            mlBottomSheet.setVisibility(INVISIBLE);
        }

        bottomSheetDelegate = createBottomSheetDelegate();
        bottomSheetBehavior = BottomSheetBehavior.from(mlBottomSheet);
        mlBottomSheet.setClickable(true);
        bottomSheetBehavior.addBottomSheetCallback(new SimpleBottomSheetCallback(
                this::onBottomSheetStateChanged,
                bottomSheetDelegate::onSlide

        ));
    }

    @Override
    public boolean isBottomPanelExpanded() {
        return bottomSheetBehavior.getState() == STATE_EXPANDED;
    }

    @Override
    public void collapseBottomPanel() {
        bottomSheetStateListener.call(false);

        setButtonsSelectableBackground(
                getResourceIdFromAttr(activity, R.attr.selectableItemBackgroundBorderless)
        );

        bottomSheetDelegate.onSlide(0f);
        if (bottomSheetBehavior.getState() != STATE_COLLAPSED) {
            bottomSheetBehavior.setState(STATE_COLLAPSED);
        }
    }

    @Override
    public void collapseBottomPanelSmoothly() {
        bottomSheetStateListener.call(false);

        if (bottomSheetBehavior.getState() != STATE_COLLAPSED) {
            bottomSheetBehavior.setState(STATE_COLLAPSED);
        }
    }

    @Override
    public void expandBottomPanel() {
        bottomSheetStateListener.call(true);

        setButtonsSelectableBackground(R.drawable.bg_selectable_round_shape);
        toolbar.setControlButtonProgress(1f);

        bottomSheetDelegate.onSlide(1f);
        if (bottomSheetBehavior.getState() != STATE_EXPANDED) {
            bottomSheetBehavior.setState(STATE_EXPANDED);
        }
    }

    @Override
    public void openPlayQueue() {
        setButtonsSelectableBackground(R.drawable.bg_selectable_round_shape);
        if (bottomSheetBehavior.getState() == STATE_COLLAPSED) {
            bottomSheetBehavior.setState(STATE_EXPANDED);
            bottomSheetDelegate.onSlide(1f);
        }
    }

    private void setContentBottomHeight(int heightInPixels) {
        bottomSheetBehavior.setPeekHeight(heightInPixels);

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fragmentContainer.getLayoutParams();
        layoutParams.bottomMargin = heightInPixels;
        fragmentContainer.setLayoutParams(layoutParams);
    }

    private void setButtonsSelectableBackground(@DrawableRes int resId) {
        ivPlayPause.setBackgroundResource(resId);
        ivSkipToNext.setBackgroundResource(resId);
        ivSkipToPrevious.setBackgroundResource(resId);
    }

    private void onBottomSheetStateChanged(Integer newState) {
        switch (newState) {
            case STATE_COLLAPSED: {
                onBottomSheetDragCollapsed.run();
                return;
            }
            case STATE_EXPANDED: {
                onBottomSheetDragExpanded.run();
            }
        }
    }

    private SlideDelegate createBottomSheetDelegate() {
        DelegateManager boundDelegateManager = new DelegateManager();
        boundDelegateManager
                .addDelegate(new BoundValuesDelegate(0.4f, 1f, new VisibilityDelegate(playQueueTitleContainer)))
                .addDelegate(new ReverseDelegate(new BoundValuesDelegate(0.0f, 0.8f, new ToolbarVisibilityDelegate(toolbar))))
                .addDelegate(new BoundValuesDelegate(0f, 0.6f, new ReverseDelegate(new VisibilityDelegate(titleContainer))))
                .addDelegate(new TextSizeDelegate(tvCurrentComposition, R.dimen.current_composition_expand_text_size, R.dimen.current_composition_expand_text_size))
                .addDelegate(new MotionLayoutDelegate(mlBottomSheet))
                .addDelegate(new BoundValuesDelegate(0.7f, 0.95f, new ReverseDelegate(new VisibilityDelegate(fragmentContainer))))
                .addDelegate(new BoundValuesDelegate(0.3f, 1.0f, new ExpandViewDelegate(R.dimen.icon_size, ivMusicIcon)))
                .addDelegate(new BoundValuesDelegate(0.95f, 1.0f, new VisibilityDelegate(tvCurrentCompositionAuthor)))
                .addDelegate(new BoundValuesDelegate(0.4f, 1.0f, new VisibilityDelegate(btnActionsMenu)))
                .addDelegate(new BoundValuesDelegate(0.93f, 1.0f, new VisibilityDelegate(sbTrackState)))
                .addDelegate(new BoundValuesDelegate(0.98f, 1.0f, new VisibilityDelegate(btnRepeatMode)))
                .addDelegate(new BoundValuesDelegate(0.98f, 1.0f, new VisibilityDelegate(btnRandomPlay)))
                .addDelegate(new BoundValuesDelegate(0.97f, 1.0f, new VisibilityDelegate(tvPlayedTime)))
                .addDelegate(new ToolbarDelegate(toolbar, activity.getWindow()))
                .addDelegate(new BoundValuesDelegate(0.97f, 1.0f, new VisibilityDelegate(tvTotalTime)));

        DelegateManager delegateManager = new DelegateManager();
        if (isInLandscapeOrientation()) {//landscape
            boundDelegateManager.addDelegate(new MoveXDelegate(
                    0.5f,
                    bottomSheetCoordinator));
            boundDelegateManager.addDelegate(new LeftBottomShadowDelegate(
                    bottomSheetLeftShadow,
                    bottomSheetTopLeftShadow,
                    mlBottomSheet,
                    bottomSheetCoordinator));
            delegateManager.addDelegate(new MoveYDelegate(clPlayQueueContainer,
                    0.85f,
                    activity.getResources().getDimensionPixelSize(R.dimen.bottom_sheet_height)
            ));
            boundDelegateManager.addDelegate(new BoundValuesDelegate(0f, 0.1f, new VisibilityDelegate(clPlayQueueContainer)));
        } else {
            boundDelegateManager.addDelegate(new BoundValuesDelegate(0.90f, 1f, new VisibilityDelegate(clPlayQueueContainer)));
            delegateManager.addDelegate(new MoveYDelegate(clPlayQueueContainer, 0.3f));
        }
        delegateManager.addDelegate(new BoundValuesDelegate(0.008f, 0.95f, boundDelegateManager));
        //ellipsize TextView workaround. Find better later
        delegateManager.addDelegate(slideOffset -> {
            if (slideOffset == 1f) {
                run(tvCurrentComposition, () -> {
                    tvCurrentComposition.requestLayout();
                    tvCurrentComposition.invalidate();
                });
            }
        });
        return delegateManager;
    }

    private void setViewStartState() {
        clPlayQueueContainer.setVisibility(INVISIBLE);
        playQueueTitleContainer.setVisibility(INVISIBLE);
        titleContainer.setVisibility(INVISIBLE);
        toolbarTitleContainer.setVisibility(INVISIBLE);
    }

    private boolean isInLandscapeOrientation() {
        return bottomSheetCoordinator != null;
    }
}
