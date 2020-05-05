package com.github.anrimian.musicplayer.ui.player_screen;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.Screens;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.ScreensMap;
import com.github.anrimian.musicplayer.ui.about.AboutAppFragment;
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils;
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.editor.composition.CompositionEditorActivity;
import com.github.anrimian.musicplayer.ui.library.albums.list.AlbumsListFragment;
import com.github.anrimian.musicplayer.ui.library.artists.list.ArtistsListFragment;
import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsFragment;
import com.github.anrimian.musicplayer.ui.library.folders.root.LibraryFoldersRootFragment;
import com.github.anrimian.musicplayer.ui.library.genres.list.GenresListFragment;
import com.github.anrimian.musicplayer.ui.player_screen.view.adapter.PlayQueueAdapter;
import com.github.anrimian.musicplayer.ui.player_screen.view.drawer.DrawerLockStateProcessor;
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.PlayerPanelWrapper;
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.PlayerPanelWrapperImpl;
import com.github.anrimian.musicplayer.ui.player_screen.view.wrappers.TabletPlayerPanelWrapper;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.PlayListFragment;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.PlayListsFragment;
import com.github.anrimian.musicplayer.ui.settings.SettingsFragment;
import com.github.anrimian.musicplayer.ui.start.StartFragment;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.JugglerView;
import com.github.anrimian.musicplayer.ui.utils.views.drawer.SimpleDrawerListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_swipe.DragAndSwipeTouchHelperCallback;
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static android.view.View.VISIBLE;
import static androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static com.github.anrimian.musicplayer.Constants.Arguments.OPEN_PLAY_QUEUE_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.CREATE_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getRepeatModeIcon;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.clearVectorAnimationInfo;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility;
import static com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil.setupMenu;

/**
 * Created on 19.10.2017.
 */

public class PlayerFragment extends MvpAppCompatFragment implements BackButtonListener, PlayerView {

    private static final int NO_ITEM = -1;
    private static final String SELECTED_DRAWER_ITEM = "selected_drawer_item";

    @InjectPresenter
    PlayerPresenter presenter;

    @BindView(R.id.drawer)
    DrawerLayout drawer;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

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

    @BindView(R.id.acv_play_queue)
    ActionMenuView acvPlayQueueMenu;

    @BindView(R.id.tv_queue_subtitle)
    TextView tvQueueSubtitle;

    private PlayQueueAdapter playQueueAdapter;

    private int selectedDrawerItemId = NO_ITEM;
    private int itemIdToStart = NO_ITEM;

    private final Handler secondScrollHandler = new Handler(Looper.getMainLooper());
    private int currentPosition = -2;//for immediate first scroll

    private LinearLayoutManager playQueueLayoutManager;
    private SeekBarViewWrapper seekBarViewWrapper;

    private DrawerLockStateProcessor drawerLockStateProcessor;
    private CompositeDisposable viewDisposable = new CompositeDisposable();

    private FragmentNavigation navigation;

    private PlayerPanelWrapper playerPanelWrapper;

    public static PlayerFragment newInstance() {
        return newInstance(false);
    }

    public static PlayerFragment newInstance(boolean openPlayQueue) {
        Bundle args = new Bundle();
        args.putBoolean(OPEN_PLAY_QUEUE_ARG, openPlayQueue);
        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @ProvidePresenter
    PlayerPresenter providePresenter() {
        return Components.getLibraryComponent().playerPresenter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        AndroidUtils.setNavigationBarColorAttr(requireActivity(), R.attr.playerPanelBackground);

        toolbar.initializeViews(requireActivity().getWindow());
        toolbar.setupWithActivity((AppCompatActivity) requireActivity());

        navigation = FragmentNavigation.from(getChildFragmentManager());
        navigation.initialize(fragmentContainer, savedInstanceState);
        navigation.checkForEqualityOnReplace(true);
        navigation.setExitAnimation(R.anim.anim_slide_out_right);
        navigation.setEnterAnimation(R.anim.anim_slide_in_right);
        navigation.setRootExitAnimation(R.anim.anim_alpha_disappear);

        drawerLockStateProcessor = new DrawerLockStateProcessor(drawer);
        drawerLockStateProcessor.setupWithNavigation(navigation);
        viewDisposable.add(toolbar.getSearchModeObservable()
                .subscribe(drawerLockStateProcessor::onSearchModeChanged)
        );
        viewDisposable.add(toolbar.getSelectionModeObservable()
                .subscribe(drawerLockStateProcessor::onSelectionModeChanged)
        );

        if (mlBottomSheet == null) {
            playerPanelWrapper = new TabletPlayerPanelWrapper(view,
                    drawerLockStateProcessor::onBottomSheetOpened);
        } else {
            playerPanelWrapper = new PlayerPanelWrapperImpl(view,
                    requireActivity(),
                    savedInstanceState,
                    presenter::onBottomPanelCollapsed,
                    presenter::onBottomPanelExpanded,
                    drawerLockStateProcessor::onBottomSheetOpened);
        }


        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
        navigationView.inflateHeaderView(R.layout.partial_drawer_header);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(requireActivity(), drawer, R.string.open_drawer, R.string.close_drawer);
        DrawerArrowDrawable drawerArrowDrawable = createDrawerArrowDrawable();
        drawerToggle.setDrawerArrowDrawable(drawerArrowDrawable);

        drawer.addDrawerListener(new SimpleDrawerListener(this::onDrawerClosed));

        setupMenu(acvPlayQueueMenu, R.menu.play_queue_menu, this::onPlayQueueMenuItemClicked);

        toolbar.setupWithNavigation(navigation,
                drawerArrowDrawable,
                () -> playerPanelWrapper.isBottomPanelExpanded());

        ivSkipToPrevious.setOnClickListener(v -> presenter.onSkipToPreviousButtonClicked());
        ivSkipToNext.setOnClickListener(v -> presenter.onSkipToNextButtonClicked());
        btnRepeatMode.setOnClickListener(this::onRepeatModeButtonClicked);

        playQueueLayoutManager = new LinearLayoutManager(requireContext());
        rvPlayList.setLayoutManager(playQueueLayoutManager);

        playQueueAdapter = new PlayQueueAdapter(rvPlayList);
        playQueueAdapter.setOnCompositionClickListener(presenter::onCompositionItemClicked);
        playQueueAdapter.setMenuClickListener(this::onPlayItemMenuClicked);
        playQueueAdapter.setIconClickListener(presenter::onQueueItemIconClicked);
        rvPlayList.setAdapter(playQueueAdapter);

        DragAndSwipeTouchHelperCallback callback = FormatUtils.withSwipeToDelete(rvPlayList,
                getColorFromAttr(requireContext(), R.attr.listItemBottomBackground),
                presenter::onItemSwipedToDelete,
                ItemTouchHelper.START,
                R.drawable.ic_remove_from_queue,
                R.string.delete_from_queue);
        callback.setOnMovedListener(presenter::onItemMoved);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvPlayList);

        if (savedInstanceState != null) {
            selectedDrawerItemId = savedInstanceState.getInt(SELECTED_DRAWER_ITEM, NO_ITEM);
        } else {
            presenter.onCurrentScreenRequested();
        }

        btnActionsMenu.setOnClickListener(this::onCompositionMenuClicked);
        topBottomSheetPanel.setOnClickListener(v -> openPlayQueue());

        seekBarViewWrapper = new SeekBarViewWrapper(sbTrackState);
        seekBarViewWrapper.setProgressChangeListener(presenter::onTrackRewoundTo);
        seekBarViewWrapper.setOnSeekStartListener(presenter::onSeekStart);
        seekBarViewWrapper.setOnSeekStopListener(presenter::onSeekStop);

        CompatUtils.setMainButtonStyle(ivPlayPause);
        CompatUtils.setMainButtonStyle(ivSkipToNext);
        CompatUtils.setMainButtonStyle(ivSkipToPrevious);
        CompatUtils.setMainButtonStyle(btnRandomPlay);
        CompatUtils.setMainButtonStyle(btnRepeatMode);
        CompatUtils.setSecondaryButtonStyle(btnActionsMenu);

        ChoosePlayListDialogFragment fragment = (ChoosePlayListDialogFragment) getChildFragmentManager()
                .findFragmentByTag(SELECT_PLAYLIST_TAG);
        if (fragment != null) {
            fragment.setOnCompleteListener(presenter::onPlayListForAddingSelected);
        }

        CreatePlayListDialogFragment createPlayListFragment = (CreatePlayListDialogFragment) getChildFragmentManager()
                .findFragmentByTag(CREATE_PLAYLIST_TAG);
        if (createPlayListFragment != null) {
            createPlayListFragment.setOnCompleteListener(presenter::onPlayListForAddingCreated);
        }

        //noinspection ConstantConditions
        if (getArguments().getBoolean(OPEN_PLAY_QUEUE_ARG)) {
            getArguments().remove(OPEN_PLAY_QUEUE_ARG);
            openPlayQueue();
        }

        RxPermissions rxPermissions = new RxPermissions(this);
        if (!rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requireFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, new StartFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawer.getDrawerLockMode(GravityCompat.START) != LOCK_MODE_LOCKED_CLOSED) {
                drawer.openDrawer(GravityCompat.START);
            } else {
                onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        navigation.onSaveInstanceState(outState);
        outState.putInt(SELECTED_DRAWER_ITEM, selectedDrawerItemId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        toolbar.release();
        drawerLockStateProcessor.release();
        viewDisposable.clear();
    }

    @Override
    public boolean onBackPressed() {
        if (playerPanelWrapper.isBottomPanelExpanded()) {
            playerPanelWrapper.collapseBottomPanelSmoothly();
            return true;
        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        Fragment fragment = navigation.getFragmentOnTop();
        boolean processed = fragment instanceof BackButtonListener
                && ((BackButtonListener) fragment).onBackPressed();
        if (!processed) {
            processed = FragmentNavigation.from(getChildFragmentManager()).goBack();
        }
        return processed;
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        //battery saving
        presenter.onStop();

        clearVectorAnimationInfo(ivPlayPause);
    }

    @Override
    public void expandBottomPanel() {
        playerPanelWrapper.expandBottomPanel();
    }

    @Override
    public void collapseBottomPanel() {
        playerPanelWrapper.collapseBottomPanel();
    }

    @Override
    public void showDrawerScreen(int screenId, long selectedPlayListScreenId) {
        int itemId = ScreensMap.getMenuId(screenId);
        selectedDrawerItemId = itemId;
        navigationView.setCheckedItem(itemId);

        switch (screenId) {
            case Screens.LIBRARY: {
                presenter.onLibraryScreenSelected();
                break;
            }
            case Screens.PLAY_LISTS: {
                List<Fragment> fragments = new ArrayList<>();
                fragments.add(new PlayListsFragment());
                if (selectedPlayListScreenId != 0) {
                    fragments.add(PlayListFragment.newInstance(selectedPlayListScreenId));
                }
                navigation.newRootFragmentStack(fragments, 0, R.anim.anim_alpha_appear);
                break;
            }
        }
    }

    @Override
    public void showLibraryScreen(int selectedLibraryScreen) {
        Fragment fragment;
        switch (selectedLibraryScreen) {
            case Screens.LIBRARY_COMPOSITIONS: {
                fragment = new LibraryCompositionsFragment();
                break;
            }
            case Screens.LIBRARY_FOLDERS: {
                fragment = new LibraryFoldersRootFragment();
                break;
            }
            case Screens.LIBRARY_ARTISTS: {
                fragment = new ArtistsListFragment();
                break;
            }
            case Screens.LIBRARY_ALBUMS: {
                fragment = new AlbumsListFragment();
                break;
            }
            case Screens.LIBRARY_GENRES: {
                fragment = new GenresListFragment();
                break;
            }
            default: {
                fragment = new LibraryCompositionsFragment();
            }
        }
        startFragment(fragment);
    }

    @Override
    public void showStopState() {
        AndroidUtils.setAnimatedVectorDrawable(ivPlayPause, R.drawable.anim_pause_to_play);
        ivPlayPause.setContentDescription(getString(R.string.play));
        ivPlayPause.setOnClickListener(v -> presenter.onPlayButtonClicked());
        playQueueAdapter.showPlaying(false);
    }

    @Override
    public void showPlayState() {
        AndroidUtils.setAnimatedVectorDrawable(ivPlayPause, R.drawable.anim_play_to_pause);
        ivPlayPause.setContentDescription(getString(R.string.pause));
        ivPlayPause.setOnClickListener(v -> presenter.onStopButtonClicked());
        playQueueAdapter.showPlaying(true);
    }

    @Override
    public void setMusicControlsEnabled(boolean show) {
        ivSkipToNext.setEnabled(show);
        ivSkipToPrevious.setEnabled(show);
        ivPlayPause.setEnabled(show);
        btnRepeatMode.setEnabled(show);
        btnRandomPlay.setEnabled(show);
        sbTrackState.setEnabled(show);
        acvPlayQueueMenu.getMenu().findItem(R.id.menu_save_as_playlist).setEnabled(show);
    }

    @Override
    public void showCurrentQueueItem(@Nullable PlayQueueItem item, boolean showCover) {
        animateVisibility(bottomSheetTopShadow, VISIBLE);
        animateVisibility(rvPlayList, VISIBLE);

        btnActionsMenu.setEnabled(item != null);
        if (item == null) {
            tvPlayedTime.setText(formatMilliseconds(0));
            tvTotalTime.setText(formatMilliseconds(0));
            sbTrackState.setProgress(0);

            tvCurrentComposition.setText(R.string.no_current_composition);
            tvCurrentCompositionAuthor.setText(R.string.unknown_author);
            ivMusicIcon.setImageResource(R.drawable.ic_music_placeholder);
            String noCompositionMessage = getString(R.string.no_current_composition);
            topBottomSheetPanel.setContentDescription(getString(R.string.now_playing_template, noCompositionMessage));
            rvPlayList.setContentDescription(noCompositionMessage);
            sbTrackState.setContentDescription(noCompositionMessage);
        } else {
            Composition composition = item.getComposition();
            String compositionName = formatCompositionName(composition);
            tvCurrentComposition.setText(compositionName);
            tvTotalTime.setText(formatMilliseconds(composition.getDuration()));
            tvCurrentCompositionAuthor.setText(formatCompositionAuthor(composition, requireContext()));
            seekBarViewWrapper.setMax(composition.getDuration());
            topBottomSheetPanel.setContentDescription(getString(R.string.now_playing_template, compositionName));
            sbTrackState.setContentDescription(null);

            if (showCover) {
                Components.getAppComponent()
                        .imageLoader()
                        .displayImage(ivMusicIcon, composition, R.drawable.ic_music_placeholder);
            } else {
                ivMusicIcon.setImageResource(R.drawable.ic_music_placeholder);
            }

            playQueueAdapter.onCurrentItemChanged(item);
        }
    }

    //check by:
    //switch order mode(working)
    //new queue(so-so, but pass)
    //remove queue item(working)
    @Override
    public void scrollQueueToPosition(int position) {
        secondScrollHandler.removeCallbacksAndMessages(null);
        int positionDiff = Math.abs(position - currentPosition);
        currentPosition = position;
        if (RecyclerViewUtils.isPositionVisible(playQueueLayoutManager, position)) {
            return;
        }

        boolean smooth = positionDiff == 1
                || position == playQueueLayoutManager.findFirstVisibleItemPosition()
                || position == playQueueLayoutManager.findLastVisibleItemPosition();

        RecyclerViewUtils.scrollToPosition(rvPlayList,
                playQueueLayoutManager,
                position,
                smooth);

        //sometimes can not scroll, check twice
        secondScrollHandler.postDelayed(() -> {
            if (!RecyclerViewUtils.isPositionVisible(playQueueLayoutManager, position)) {
                RecyclerViewUtils.scrollToPosition(rvPlayList,
                        playQueueLayoutManager,
                        position,
                        false);
            }
        }, 300);
    }

    @Override
    public void updatePlayQueue(List<PlayQueueItem> items) {
        playQueueAdapter.submitList(items);
    }

    @Override
    public void showRepeatMode(int mode) {
        @DrawableRes int iconRes = getRepeatModeIcon(mode);
        btnRepeatMode.setImageResource(iconRes);
    }

    @Override
    public void showRandomPlayingButton(boolean active) {
        btnRandomPlay.setSelected(active);
        if (active) {
            btnRandomPlay.setOnClickListener(v -> presenter.onRandomPlayingButtonClicked(false));
        } else {
            btnRandomPlay.setOnClickListener(v -> presenter.onRandomPlayingButtonClicked(true));
        }
    }

    @Override
    public void showTrackState(long currentPosition, long duration) {
        seekBarViewWrapper.setProgress(currentPosition);
        String formattedTime = formatMilliseconds(currentPosition);
        sbTrackState.setContentDescription(getString(R.string.position_template, formattedTime));
        tvPlayedTime.setText(formattedTime);
    }

    @Override
    public void showShareMusicDialog(Composition composition) {
        DialogUtils.shareComposition(requireContext(), composition);
    }

    @Override
    public void showPlayQueueSubtitle(int size) {
        tvQueueSubtitle.setText(getResources().getQuantityString(R.plurals.compositions_count, size, size));
    }

    @Override
    public void setSkipToNextButtonEnabled(boolean enabled) {
        ivSkipToNext.setEnabled(enabled);
    }

    @Override
    public void notifyItemMoved(int from, int to) {
        playQueueAdapter.notifyItemMoved(from, to);
    }

    @Override
    public void setPlayQueueCoversEnabled(boolean isCoversEnabled) {
        playQueueAdapter.setCoversEnabled(isCoversEnabled);
    }

    @Override
    public void startEditCompositionScreen(long id) {
        startActivity(CompositionEditorActivity.newIntent(requireContext(), id));
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clPlayQueueContainer, errorCommand.getMessage()).show();
    }

    @Override
    public void showDeletedItemMessage() {
        MessagesUtils.makeSnackbar(clPlayQueueContainer, R.string.queue_item_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.cancel, presenter::onRestoreDeletedItemClicked)
                .show();
    }

    @Override
    public void showAddingToPlayListError(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clPlayQueueContainer,
                getString(R.string.add_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions) {
        String text = getAddToPlayListCompleteMessage(requireActivity(), playList, compositions);
        MessagesUtils.makeSnackbar(clPlayQueueContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showSelectPlayListDialog() {
        ChoosePlayListDialogFragment dialog = new ChoosePlayListDialogFragment();
        dialog.setOnCompleteListener(presenter::onPlayListForAddingSelected);
        dialog.show(getChildFragmentManager(), SELECT_PLAYLIST_TAG);
    }

    @Override
    public void showConfirmDeleteDialog(List<Composition> compositionsToDelete) {
        DialogUtils.showConfirmDeleteDialog(requireContext(),
                compositionsToDelete,
                presenter::onDeleteCompositionsDialogConfirmed);
    }

    @Override
    public void showDeleteCompositionError(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clPlayQueueContainer,
                getString(R.string.add_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showDeleteCompositionMessage(List<Composition> compositionsToDelete) {
        String text = getDeleteCompleteMessage(requireActivity(), compositionsToDelete);
        MessagesUtils.makeSnackbar(clPlayQueueContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    public void openPlayQueue() {
        presenter.onOpenPlayQueueClicked();
        playerPanelWrapper.openPlayQueue();
    }

    private void onPlayQueueMenuItemClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_save_as_playlist: {
                CreatePlayListDialogFragment fragment = new CreatePlayListDialogFragment();
                fragment.setOnCompleteListener(presenter::onPlayListForAddingCreated);
                fragment.show(getChildFragmentManager(), CREATE_PLAYLIST_TAG);
                break;
            }
            case R.id.menu_clear_play_queue: {
                presenter.onClearPlayQueueClicked();
                break;
            }
        }
    }

    private void onNavigationIconClicked() {
        if (drawer.getDrawerLockMode(GravityCompat.START) != LOCK_MODE_LOCKED_CLOSED) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            onBackPressed();
        }
    }

    private void startFragment(Fragment fragment) {
        navigation.newRootFragment(fragment, 0, R.anim.anim_alpha_appear);
    }

    private void clearFragment() {
        navigation.clearFragmentStack(R.anim.anim_alpha_disappear);
    }

    private void onCompositionMenuClicked(View view) {
        PopupMenuWindow.showPopup(view,
                R.menu.composition_short_actions_menu,
                item -> {
                    switch (item.getItemId()) {
                        case R.id.menu_add_to_playlist: {
                            presenter.onAddCurrentCompositionToPlayListButtonClicked();
                            break;
                        }
                        case R.id.menu_share: {
                            presenter.onShareCompositionButtonClicked();
                            break;
                        }
                        case R.id.menu_delete: {
                            presenter.onDeleteCurrentCompositionButtonClicked();
                            break;
                        }
                        case R.id.menu_edit: {
                            presenter.onEditCompositionButtonClicked();
                            break;
                        }
                    }
                });
    }

    private DrawerArrowDrawable createDrawerArrowDrawable() {
        DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(requireActivity());
        drawerArrowDrawable.setColor(getColorFromAttr(requireActivity(), R.attr.toolbarTextColorPrimary));
        return drawerArrowDrawable;
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        boolean selected = false;
        if (itemId == R.id.menu_settings) {
            navigation.addNewFragment(new SettingsFragment());
        } else if (itemId == R.id.menu_about) {
            navigation.addNewFragment(new AboutAppFragment());
        } else if (selectedDrawerItemId != itemId) {
            selectedDrawerItemId = itemId;
            itemIdToStart = itemId;
            clearFragment();
            selected = true;
        }
        drawer.closeDrawer(GravityCompat.START);
        return selected;
    }

    private void onDrawerClosed() {
        if (itemIdToStart != NO_ITEM) {
            int screenId = ScreensMap.getScreenId(itemIdToStart);
            presenter.onDrawerScreenSelected(screenId);
            itemIdToStart = NO_ITEM;
        }
    }

    private void onPlayItemMenuClicked(View view, PlayQueueItem playQueueItem) {
        Composition composition = playQueueItem.getComposition();

        PopupMenuWindow.showPopup(view,
                R.menu.play_queue_item_menu,
                item -> {
                    switch (item.getItemId()) {
                        case R.id.menu_add_to_playlist: {
                            presenter.onAddQueueItemToPlayListButtonClicked(composition);
                            break;
                        }
                        case R.id.menu_edit: {
                            startActivity(CompositionEditorActivity.newIntent(requireContext(), composition.getId()));
                            break;
                        }
                        case R.id.menu_share: {
                            onShareCompositionClicked(composition);
                            break;
                        }
                        case R.id.menu_delete_from_queue: {
                            presenter.onDeleteQueueItemClicked(playQueueItem);
                            break;
                        }
                        case R.id.menu_delete: {
                            presenter.onDeleteCompositionButtonClicked(composition);
                            break;
                        }
                    }
                });
    }

    private void onRepeatModeButtonClicked(View view) {
        PopupMenuWindow.showPopup(view,
                R.menu.repeat_mode_menu,
                item -> {
                    int repeatMode = RepeatMode.NONE;
                    switch (item.getItemId()) {
                        case R.id.menu_repeat_playlist: {
                            repeatMode = RepeatMode.REPEAT_PLAY_LIST;
                            break;
                        }
                        case R.id.menu_repeat_composition: {
                            repeatMode = RepeatMode.REPEAT_COMPOSITION;
                            break;
                        }
                        case R.id.menu_do_not_repeat: {
                            repeatMode = RepeatMode.NONE;
                            break;
                        }
                    }
                    presenter.onRepeatModeChanged(repeatMode);
                });
    }

    private void onShareCompositionClicked(Composition composition) {
        DialogUtils.shareComposition(requireContext(), composition);
    }
}
