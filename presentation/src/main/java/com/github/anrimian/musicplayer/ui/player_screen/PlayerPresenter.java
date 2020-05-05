package com.github.anrimian.musicplayer.ui.player_screen;

import com.github.anrimian.musicplayer.domain.interactors.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerScreenInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.areSourcesTheSame;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;

/**
 * Created on 02.11.2017.
 */

@InjectViewState
public class PlayerPresenter extends MvpPresenter<PlayerView> {

    private final MusicPlayerInteractor playerInteractor;
    private final PlayListsInteractor playListsInteractor;
    private final PlayerScreenInteractor playerScreenInteractor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private final CompositeDisposable batterySafeDisposable = new CompositeDisposable();

    private List<PlayQueueItem> playQueue = new ArrayList<>();

    private PlayQueueItem currentItem;
    private boolean isCoversEnabled = false;

    private final List<Composition> compositionsForPlayList = new LinkedList<>();
    private final List<Composition> compositionsToDelete = new LinkedList<>();

    public PlayerPresenter(MusicPlayerInteractor musicPlayerInteractor,
                           PlayListsInteractor playListsInteractor,
                           PlayerScreenInteractor playerScreenInteractor,
                           ErrorParser errorParser,
                           Scheduler uiScheduler) {
        this.playerInteractor = musicPlayerInteractor;
        this.playListsInteractor = playListsInteractor;
        this.playerScreenInteractor = playerScreenInteractor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().setSkipToNextButtonEnabled(true);
        if (playerScreenInteractor.isPlayerPanelOpen()) {
            getViewState().expandBottomPanel();
        } else {
            getViewState().collapseBottomPanel();
        }
        subscribeOnUiSettings();
        subscribeOnRandomMode();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onStart() {
        subscribeOnRepeatMode();
        subscribeOnPlayerStateChanges();
        subscribeOnPlayQueue();
        subscribeOnCurrentCompositionChanging();
        subscribeOnCurrentPosition();
        subscribeOnTrackPositionChanging();
    }

    void onStop() {
        batterySafeDisposable.clear();
    }

    void onCurrentScreenRequested() {
        getViewState().showDrawerScreen(playerScreenInteractor.getSelectedDrawerScreen(),
                playerScreenInteractor.getSelectedPlayListScreenId());
    }

    void onOpenPlayQueueClicked() {
        playerScreenInteractor.setPlayerPanelOpen(true);
    }

    void onBottomPanelExpanded() {
        playerScreenInteractor.setPlayerPanelOpen(true);
        getViewState().expandBottomPanel();
    }

    void onBottomPanelCollapsed() {
        playerScreenInteractor.setPlayerPanelOpen(false);
        getViewState().collapseBottomPanel();
    }

    void onDrawerScreenSelected(int screenId) {
        playerScreenInteractor.setSelectedDrawerScreen(screenId);
        getViewState().showDrawerScreen(screenId, 0);
    }

    void onLibraryScreenSelected() {
        getViewState().showLibraryScreen(playerScreenInteractor.getSelectedLibraryScreen());
    }

    void onPlayButtonClicked() {
        playerInteractor.play();
    }

    void onStopButtonClicked() {
        playerInteractor.pause();
    }

    void onSkipToPreviousButtonClicked() {
        playerInteractor.skipToPrevious();
    }

    void onSkipToNextButtonClicked() {
        playerInteractor.skipToNext();
    }

    void onRepeatModeChanged(int mode) {
        playerInteractor.setRepeatMode(mode);
    }

    void onRandomPlayingButtonClicked(boolean enable) {
        playerInteractor.setRandomPlayingEnabled(enable);
    }

    void onShareCompositionButtonClicked() {
        getViewState().showShareMusicDialog(currentItem.getComposition());
    }

    void onCompositionItemClicked(int position, PlayQueueItem item) {
        this.currentItem = item;
        playerInteractor.skipToItem(item);

        onCurrentCompositionChanged(item, 0);
    }

    void onQueueItemIconClicked(int position, PlayQueueItem playQueueItem) {
        if (playQueueItem.equals(currentItem)) {
            playerInteractor.playOrPause();
        } else {
            playerInteractor.play();
            onCompositionItemClicked(position, playQueueItem);
        }
    }

    void onTrackRewoundTo(int progress) {
        playerInteractor.seekTo(progress);
    }

    void onDeleteCompositionButtonClicked(Composition composition) {
        compositionsToDelete.clear();
        compositionsToDelete.add(composition);
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    void onDeleteCurrentCompositionButtonClicked() {
        compositionsToDelete.clear();
        compositionsToDelete.add(currentItem.getComposition());
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    void onAddQueueItemToPlayListButtonClicked(Composition composition) {
        compositionsForPlayList.clear();
        compositionsForPlayList.add(composition);
        getViewState().showSelectPlayListDialog();
    }

    void onAddCurrentCompositionToPlayListButtonClicked() {
        compositionsForPlayList.clear();
        compositionsForPlayList.add(currentItem.getComposition());
        getViewState().showSelectPlayListDialog();
    }

    void onPlayListForAddingSelected(PlayList playList) {
        addPreparedCompositionsToPlayList(playList);
    }

    void onPlayListForAddingCreated(PlayList playList) {
        List<Composition> compositionsToAdd = mapList(playQueue, PlayQueueItem::getComposition);
        playListsInteractor.addCompositionsToPlayList(compositionsToAdd, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> getViewState().showAddingToPlayListComplete(playList, compositionsToAdd),
                        this::onAddingToPlayListError);
    }

    void onDeleteCompositionsDialogConfirmed() {
        deletePreparedCompositions();
    }

    void onSeekStart() {
        playerInteractor.onSeekStarted();
    }

    void onSeekStop(int progress) {
        playerInteractor.onSeekFinished(progress);
    }

    void onItemSwipedToDelete(Integer position) {
        deletePlayQueueItem(playQueue.get(position));
    }

    void onDeleteQueueItemClicked(PlayQueueItem item) {
        deletePlayQueueItem(item);
    }

    void onItemMoved(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                swapItems(i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                swapItems(i, i - 1);
            }
        }
    }

    void onEditCompositionButtonClicked() {
        getViewState().startEditCompositionScreen(currentItem.getComposition().getId());
    }

    void onRestoreDeletedItemClicked() {
        playerInteractor.restoreDeletedItem()
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
    }

    void onClearPlayQueueClicked() {
        playerInteractor.clearPlayQueue();
    }

    private void swapItems(int from, int to) {
        PlayQueueItem fromItem = playQueue.get(from);
        PlayQueueItem toItem = playQueue.get(to);

        Collections.swap(playQueue, from, to);
        getViewState().notifyItemMoved(from, to);

        playerInteractor.swapItems(fromItem, toItem);
    }

    private void deletePlayQueueItem(PlayQueueItem item) {
        playerInteractor.removeQueueItem(item)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::showDeletedItemMessage);
    }

    private void subscribeOnRepeatMode() {
        playerInteractor.getRepeatModeObservable()
                .observeOn(uiScheduler)
                .subscribe(getViewState()::showRepeatMode);
    }

    private void addPreparedCompositionsToPlayList(PlayList playList) {
        playListsInteractor.addCompositionsToPlayList(compositionsForPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> onAddingToPlayListCompleted(playList),
                        this::onAddingToPlayListError);
    }

    private void deletePreparedCompositions() {
        playerInteractor.deleteCompositions(compositionsToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteCompositionsSuccess, this::onDeleteCompositionError);
    }

    private void onDeleteCompositionsSuccess() {
        getViewState().showDeleteCompositionMessage(compositionsToDelete);
        compositionsToDelete.clear();
    }

    private void onDeleteCompositionError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteCompositionError(errorCommand);
        compositionsToDelete.clear();
    }

    private void onAddingToPlayListError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAddingToPlayListError(errorCommand);
    }

    private void onAddingToPlayListCompleted(PlayList playList) {
        getViewState().showAddingToPlayListComplete(playList, compositionsForPlayList);
        compositionsForPlayList.clear();
    }

    private void subscribeOnCurrentCompositionChanging() {
        batterySafeDisposable.add(playerInteractor.getCurrentQueueItemObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayQueueEventReceived));
    }

    private void subscribeOnCurrentPosition() {
        batterySafeDisposable.add(playerInteractor.getCurrentItemPositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onItemPositionReceived));
    }

    private void onItemPositionReceived(int position) {
        getViewState().scrollQueueToPosition(position);
    }

    private void onPlayQueueEventReceived(PlayQueueEvent playQueueEvent) {
        PlayQueueItem newItem = playQueueEvent.getPlayQueueItem();
        if (currentItem == null || !currentItem.equals(newItem)
                || !areSourcesTheSame(newItem.getComposition(), currentItem.getComposition())) {
            onCurrentCompositionChanged(newItem, playQueueEvent.getTrackPosition());
        }
    }

    private void onCurrentCompositionChanged(PlayQueueItem newItem, long trackPosition) {
        this.currentItem = newItem;
        getViewState().showCurrentQueueItem(newItem, isCoversEnabled);
        if (newItem != null) {
            getViewState().showTrackState(trackPosition, newItem.getComposition().getDuration());
        }
    }

    private void subscribeOnPlayerStateChanges() {
        batterySafeDisposable.add(playerInteractor.getPlayerStateObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayerStateChanged));
    }

    private void onPlayerStateChanged(PlayerState playerState) {
        switch (playerState) {
            case PLAY: {
                getViewState().showPlayState();
                return;
            }
            default: {
                getViewState().showStopState();
            }
        }
    }

    private void subscribeOnPlayQueue() {
        batterySafeDisposable.add(playerInteractor.getPlayQueueObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayQueueChanged, this::onPlayQueueReceivingError));
    }

    private void onPlayQueueChanged(List<PlayQueueItem> list) {
        playQueue = list;
        getViewState().showPlayQueueSubtitle(playQueue.size());
        getViewState().setMusicControlsEnabled(!playQueue.isEmpty());
        getViewState().setSkipToNextButtonEnabled(playQueue.size() > 1);
        getViewState().updatePlayQueue(list);
    }

    private void onPlayQueueReceivingError(Throwable throwable) {
        errorParser.parseError(throwable);
        getViewState().setMusicControlsEnabled(false);
    }

    private void subscribeOnTrackPositionChanging() {
        batterySafeDisposable.add(playerInteractor.getTrackPositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onTrackPositionChanged));
    }

    private void onTrackPositionChanged(Long currentPosition) {
        if (currentItem != null) {
            long duration = currentItem.getComposition().getDuration();
            getViewState().showTrackState(currentPosition, duration);
        }
    }

    private void subscribeOnUiSettings() {
        presenterDisposable.add(playerScreenInteractor.getCoversEnabledObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onUiSettingsReceived, errorParser::logError));
    }

    private void onUiSettingsReceived(boolean isCoversEnabled) {
        this.isCoversEnabled = isCoversEnabled;
        getViewState().setPlayQueueCoversEnabled(isCoversEnabled);
        if (currentItem != null) {
            getViewState().showCurrentQueueItem(currentItem, isCoversEnabled);
        }
    }

    private void onDefaultError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showErrorMessage(errorCommand);
    }

    private void subscribeOnRandomMode() {
        presenterDisposable.add(playerInteractor.getRandomPlayingObservable()
                .observeOn(uiScheduler)
                .subscribe(getViewState()::showRandomPlayingButton));
    }
}
