package com.github.anrimian.musicplayer.ui.playlist_screens.playlist;

import com.github.anrimian.musicplayer.domain.interactors.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.utils.model.Item;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.MvpPresenter;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.isInactive;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapList;
import static java.util.Objects.requireNonNull;


public class PlayListPresenter extends MvpPresenter<PlayListView> {

    private final MusicPlayerInteractor playerInteractor;
    private final PlayListsInteractor playListsInteractor;
    private final DisplaySettingsInteractor displaySettingsInteractor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private final CompositeDisposable presenterBatterySafeDisposable = new CompositeDisposable();
    private Disposable currentItemDisposable;

    private final long playListId;

    private List<PlayListItem> items = new ArrayList<>();

    @Nullable
    private PlayList playList;

    private final List<Composition> compositionsForPlayList = new LinkedList<>();
    private final List<Composition> compositionsToDelete = new LinkedList<>();

    private int startDragPosition;

    private PlayQueueItem currentItem;

    private Item<PlayListItem> deletedItem;

    public PlayListPresenter(long playListId,
                             MusicPlayerInteractor playerInteractor,
                             PlayListsInteractor playListsInteractor,
                             DisplaySettingsInteractor displaySettingsInteractor,
                             ErrorParser errorParser,
                             Scheduler uiScheduler) {
        this.playListId = playListId;
        this.playerInteractor = playerInteractor;
        this.playListsInteractor = playListsInteractor;
        this.displaySettingsInteractor = displaySettingsInteractor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnCompositions();
        subscribePlayList();
        subscribeOnCurrentComposition();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onStart() {
        if (!items.isEmpty()) {
            subscribeOnCurrentComposition();
        }
    }

    void onStop() {
        presenterBatterySafeDisposable.clear();
    }

    void onCompositionClicked(PlayListItem playListItem, int position) {
        getViewState().showCompositionActionDialog(playListItem, position);
    }

    void onItemIconClicked(int position) {
        playerInteractor.startPlaying(mapList(items, PlayListItem::getComposition), position);
    }

    void onPlayAllButtonClicked() {
        playerInteractor.startPlaying(mapList(items, PlayListItem::getComposition));
    }

    void onDeleteCompositionButtonClicked(Composition composition) {
        compositionsToDelete.clear();
        compositionsToDelete.add(composition);
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    void onDeleteCompositionsDialogConfirmed() {
        deletePreparedCompositions();
    }

    void onAddToPlayListButtonClicked(Composition composition) {
        compositionsForPlayList.clear();
        compositionsForPlayList.add(composition);
        getViewState().showSelectPlayListDialog();
    }

    void onPlayNextCompositionClicked(Composition composition) {
        addCompositionsToPlayNext(asList(composition));
    }

    void onAddToQueueCompositionClicked(Composition composition) {
        addCompositionsToEnd(asList(composition));
    }

    void onPlayListToAddingSelected(PlayList playList) {
        addPreparedCompositionsToPlayList(playList);
    }

    void onDeleteFromPlayListButtonClicked(PlayListItem playListItem, int position) {
        deleteItem(playListItem, position);
    }

    void onDeletePlayListButtonClicked() {
        getViewState().showConfirmDeletePlayListDialog(playList);
    }

    void onDeletePlayListDialogConfirmed() {
        playListsInteractor.deletePlayList(requireNonNull(playList).getId())
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListDeleted, this::onPlayListDeletingError);
    }

    void onFragmentMovedToTop() {
        playListsInteractor.setSelectedPlayListScreen(playListId);
    }

    void onItemSwipedToDelete(int position) {
        deleteItem(items.get(position), position);
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

    void onDragStarted(int position) {
        startDragPosition = position;
    }

    void onDragEnded(int position) {
        playListsInteractor.moveItemInPlayList(playList, startDragPosition, position);
    }

    void onPlayActionSelected(int position) {
        playerInteractor.startPlaying(
                mapList(items, PlayListItem::getComposition),
                position
        );
    }

    void onRestoreRemovedItemClicked() {
        Composition removedComposition = deletedItem.getData().getComposition();
        playListsInteractor.addCompositionsToPlayList(asList(removedComposition),
                playList,
                deletedItem.getPosition())
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
    }

    void onChangePlayListNameButtonClicked() {
        if (playList != null) {
            getViewState().showEditPlayListNameDialog(playList);
        }
    }

    boolean isCoversEnabled() {
        return displaySettingsInteractor.isCoversEnabled();
    }

    private void addCompositionsToPlayNext(List<Composition> compositions) {
        playerInteractor.addCompositionsToPlayNext(compositions)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::onCompositionsAddedToPlayNext, this::onDefaultError);
    }

    private void addCompositionsToEnd(List<Composition> compositions) {
        playerInteractor.addCompositionsToEnd(compositions)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::onCompositionsAddedToPlayNext, this::onDefaultError);
    }

    private void onDefaultError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showErrorMessage(errorCommand);
    }

    private void deleteItem(PlayListItem playListItem, int position) {
        playListsInteractor.deleteItemFromPlayList(playListItem, playListId)
                .observeOn(uiScheduler)
                .subscribe(() -> onDeleteItemCompleted(playListItem, position), this::onDeleteItemError);
    }

    private void swapItems(int from, int to) {
        Collections.swap(items, from, to);
        getViewState().notifyItemMoved(from, to);
    }

    private void onPlayListDeletingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeletePlayListError(errorCommand);
    }

    private void onPlayListDeleted() {
        getViewState().showPlayListDeleteSuccess(playList);
    }

    private void onDeleteItemCompleted(PlayListItem item, int position) {
        if (playList != null) {
            deletedItem = new Item<>(item, position);
//            getViewState().notifyItemRemoved(position);
            getViewState().showDeleteItemCompleted(playList, asList(item));
        }
    }

    private void onDeleteItemError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteItemError(errorCommand);
    }

    private void addPreparedCompositionsToPlayList(PlayList playList) {
        playListsInteractor.addCompositionsToPlayList(compositionsForPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> onAddingToPlayListCompleted(playList),
                        this::onAddingToPlayListError);
    }

    private void onAddingToPlayListError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAddingToPlayListError(errorCommand);
    }

    private void onAddingToPlayListCompleted(PlayList playList) {
        getViewState().showAddingToPlayListComplete(playList, compositionsForPlayList);
        compositionsForPlayList.clear();
    }

    private void deletePreparedCompositions() {
        playerInteractor.deleteCompositions(compositionsToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteCompositionsSuccess, this::onDeleteCompositionsError);
    }

    private void onDeleteCompositionsSuccess() {
        getViewState().showDeletedCompositionMessage(compositionsToDelete);
    }

    private void onDeleteCompositionsError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteCompositionError(errorCommand);
    }

    private void subscribeOnCompositions() {
        getViewState().showLoading();
        presenterDisposable.add(playListsInteractor.getCompositionsObservable(playListId)
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListsReceived,
                        t -> getViewState().closeScreen(),
                        getViewState()::closeScreen));
    }

    private void subscribePlayList() {
        presenterDisposable.add(playListsInteractor.getPlayListObservable(playListId)
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListInfoReceived,
                        t -> getViewState().closeScreen(),
                        getViewState()::closeScreen));
    }

    private void onPlayListInfoReceived(PlayList playList) {
        this.playList = playList;
        getViewState().showPlayListInfo(playList);
    }

    private void onPlayListsReceived(List<PlayListItem> list) {
        this.items = list;
        getViewState().updateItemsList(list);
        if (items.isEmpty()) {
            getViewState().showEmptyList();
        } else {
            getViewState().showList();

            if (isInactive(currentItemDisposable)) {
                subscribeOnCurrentComposition();
            }
        }
    }

    private void subscribeOnCurrentComposition() {
        currentItemDisposable = playerInteractor.getCurrentQueueItemObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onCurrentCompositionReceived, errorParser::logError);
        presenterBatterySafeDisposable.add(currentItemDisposable);
    }

    private void onCurrentCompositionReceived(PlayQueueEvent playQueueEvent) {
        currentItem = playQueueEvent.getPlayQueueItem();
    }

}
