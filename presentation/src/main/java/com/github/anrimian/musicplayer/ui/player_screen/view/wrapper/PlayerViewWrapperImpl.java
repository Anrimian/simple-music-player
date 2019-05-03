package com.github.anrimian.musicplayer.ui.player_screen.view.wrapper;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.JugglerView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.INVISIBLE;

public class PlayerViewWrapperImpl implements PlayerViewWrapper {

    @Nullable
    @BindView(R.id.coordinator_bottom_sheet)
    CoordinatorLayout bottomSheetCoordinator;

    @Nullable
    @BindView(R.id.bottom_sheet_left_shadow)
    View bottomSheetLeftShadow;

    @Nullable
    @BindView(R.id.bottom_sheet_top_left_shadow)
    View bottomSheetTopLeftShadow;

    @BindView(R.id.rv_playlist)
    RecyclerView rvPlayList;

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

    @BindView(R.id.bottom_sheet_top_shadow)
    View bottomSheetTopShadow;

    @BindView(R.id.top_panel)
    View topBottomSheetPanel;

    @BindView(R.id.iv_music_icon)
    ImageView ivMusicIcon;

    @BindView(R.id.btn_actions_menu)
    ImageView btnActionsMenu;

    @BindView(R.id.tv_current_composition_author)
    TextView tvCurrentCompositionAuthor;

    @BindView(R.id.cl_play_queue_container)
    CoordinatorLayout clPlayQueueContainer;

    @BindView(R.id.ml_bottom_sheet)
    @Nullable
    MotionLayout mlBottomSheet;

    @BindView(R.id.toolbar)
    AdvancedToolbar toolbar;

    @BindView(R.id.toolbar_content_container)
    View titleContainer;

    @BindView(R.id.acv_play_queue)
    ActionMenuView actionMenuView;

    @BindView(R.id.toolbar_play_queue)
    View playQueueTitleContainer;

    @BindView(R.id.tv_queue_subtitle)
    TextView tvQueueSubtitle;

    @BindView(R.id.title_container)
    View toolbarTitleContainer;

    public PlayerViewWrapperImpl(View view) {
        ButterKnife.bind(this, view);
    }

    @Override
    public void setViewStartState() {
        playQueueTitleContainer.setVisibility(INVISIBLE);
        titleContainer.setVisibility(INVISIBLE);
        bottomSheetTopShadow.setVisibility(INVISIBLE);
        rvPlayList.setVisibility(INVISIBLE);
        toolbarTitleContainer.setVisibility(INVISIBLE);
    }
}
