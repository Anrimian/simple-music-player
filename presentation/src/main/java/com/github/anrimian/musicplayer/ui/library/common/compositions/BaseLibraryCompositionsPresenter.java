package com.github.anrimian.musicplayer.ui.library.common.compositions;

import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.MvpPresenter;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.isInactive;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

public abstract class BaseLibraryCompositionsPresenter<T extends BaseLibraryCompositionsView>
        extends MvpPresenter<T> {

    private final LibraryPlayerInteractor playerInteractor;
    private final PlayListsInteractor playListsInteractor;
    private final DisplaySettingsInteractor displaySettingsInteractor;
    protected final ErrorParser errorParser;
    protected final Scheduler uiScheduler;

    private final CompositeDisposable presenterBatterySafeDisposable = new CompositeDisposable();
    protected final CompositeDisposable presenterDisposable = new CompositeDisposable();

    protected Disposable currentCompositionDisposable;
    private Disposable compositionsDisposable;

    private List<Composition> compositions = new ArrayList<>();
    private final LinkedHashSet<Composition> selectedCompositions = new LinkedHashSet<>();
    private final List<Composition> compositionsForPlayList = new LinkedList<>();
    private final List<Composition> compositionsToDelete = new LinkedList<>();

    @Nullable
    private Composition currentComposition;

    @Nullable
    private String searchText;

    public BaseLibraryCompositionsPresenter(LibraryPlayerInteractor playerInteractor,
                                            PlayListsInteractor playListsInteractor,
                                            DisplaySettingsInteractor displaySettingsInteractor,
                                            ErrorParser errorParser,
                                            Scheduler uiScheduler) {
        this.playerInteractor = playerInteractor;
        this.playListsInteractor = playListsInteractor;
        this.displaySettingsInteractor = displaySettingsInteractor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnUiSettings();
        subscribeOnCompositions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    public void onStart() {
        if (!compositions.isEmpty()) {
            subscribeOnCurrentComposition();
        }
    }

    public void onStop() {
        presenterBatterySafeDisposable.clear();
    }

    public void onTryAgainLoadCompositionsClicked() {
        subscribeOnCompositions();
    }

    public void onCompositionClicked(int position, Composition composition) {
        if (selectedCompositions.isEmpty()) {
            getViewState().showCompositionActionDialog(composition, position);
        } else {
            if (selectedCompositions.contains(composition)) {
                selectedCompositions.remove(composition);
                getViewState().onCompositionUnselected(composition, position);
            } else {
                selectedCompositions.add(composition);
                getViewState().onCompositionSelected(composition, position);
            }
            getViewState().showSelectionMode(selectedCompositions.size());
        }
    }

    public void onCompositionIconClicked(int position, Composition composition) {
        if (composition.equals(currentComposition)) {
            playerInteractor.playOrPause();
        } else {
            playerInteractor.startPlaying(compositions, position);
            getViewState().showCurrentComposition(new CurrentComposition(composition, true));
        }
    }

    public void onCompositionLongClick(int position, Composition composition) {
        selectedCompositions.add(composition);
        getViewState().showSelectionMode(selectedCompositions.size());
        getViewState().onCompositionSelected(composition, position);
    }

    public void onPlayAllButtonClicked() {
        if (selectedCompositions.isEmpty()) {
            playerInteractor.startPlaying(compositions);
        } else {
            playSelectedCompositions();
        }
    }

    public void onDeleteCompositionButtonClicked(Composition composition) {
        compositionsToDelete.clear();
        compositionsToDelete.add(composition);
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    public void onDeleteSelectedCompositionButtonClicked() {
        compositionsToDelete.clear();
        compositionsToDelete.addAll(selectedCompositions);
        getViewState().showConfirmDeleteDialog(compositionsToDelete);
    }

    public void onDeleteCompositionsDialogConfirmed() {
        deletePreparedCompositions();
    }

    public void onPlayNextCompositionClicked(Composition composition) {
        addCompositionsToPlayNext(asList(composition));
    }

    public void onAddToQueueCompositionClicked(Composition composition) {
        addCompositionsToEnd(asList(composition));
    }

    public void onAddToPlayListButtonClicked(Composition composition) {
        compositionsForPlayList.clear();
        compositionsForPlayList.add(composition);
        getViewState().showSelectPlayListDialog();
    }

    public void onAddSelectedCompositionToPlayListClicked() {
        compositionsForPlayList.clear();
        compositionsForPlayList.addAll(selectedCompositions);
        getViewState().showSelectPlayListDialog();
    }

    public void onPlayListToAddingSelected(PlayList playList) {
        playListsInteractor.addCompositionsToPlayList(compositionsForPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(() -> onAddingToPlayListCompleted(playList),
                        this::onAddingToPlayListError);
    }

    public void onSelectionModeBackPressed() {
        closeSelectionMode();
    }

    public void onShareSelectedCompositionsClicked() {
        getViewState().shareCompositions(selectedCompositions);
    }

    public void onPlayAllSelectedClicked() {
        playSelectedCompositions();
    }

    public void onSelectAllButtonClicked() {
        selectedCompositions.clear();//reselect previous feature
        selectedCompositions.addAll(compositions);
        getViewState().showSelectionMode(compositions.size());
        getViewState().setItemsSelected(true);
    }

    public void onPlayNextSelectedCompositionsClicked() {
        addCompositionsToPlayNext(new ArrayList<>(selectedCompositions));
        closeSelectionMode();
    }

    public void onAddToQueueSelectedCompositionsClicked() {
        addCompositionsToEnd(new ArrayList<>(selectedCompositions));
        closeSelectionMode();
    }

    public void onPlayActionSelected(int position) {
        playerInteractor.startPlaying(compositions, position);
    }

    public void onSearchTextChanged(String text) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text;
            subscribeOnCompositions();
        }
    }

    public HashSet<Composition> getSelectedCompositions() {
        return selectedCompositions;
    }

    @Nullable
    public String getSearchText() {
        return searchText;
    }

    protected void onDefaultError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showErrorMessage(errorCommand);
    }

    private void onDeleteCompositionError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteCompositionError(errorCommand);
    }

    private void onAddingToPlayListError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAddingToPlayListError(errorCommand);
    }

    private void onAddingToPlayListCompleted(PlayList playList) {
        getViewState().showAddingToPlayListComplete(playList, compositionsForPlayList);
        compositionsForPlayList.clear();

        if (!selectedCompositions.isEmpty()) {
            closeSelectionMode();
        }
    }

    private void addCompositionsToPlayNext(List<Composition> compositions) {
        playerInteractor.addCompositionsToPlayNext(compositions)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::onCompositionsAddedToPlayNext, this::onDefaultError);
    }

    private void addCompositionsToEnd(List<Composition> compositions) {
        playerInteractor.addCompositionsToEnd(compositions)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::onCompositionsAddedToQueue, this::onDefaultError);
    }

    private void playSelectedCompositions() {
        playerInteractor.startPlaying(new ArrayList<>(selectedCompositions));
        closeSelectionMode();
    }

    private void closeSelectionMode() {
        selectedCompositions.clear();
        getViewState().showSelectionMode(0);
        getViewState().setItemsSelected(false);
    }

    private void deletePreparedCompositions() {
        playerInteractor.deleteCompositions(compositionsToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteCompositionsSuccess, this::onDeleteCompositionError);
    }

    private void onDeleteCompositionsSuccess() {
        getViewState().showDeleteCompositionMessage(compositionsToDelete);
        compositionsToDelete.clear();

        if (!selectedCompositions.isEmpty()) {
            closeSelectionMode();
        }
    }

    protected void subscribeOnCurrentComposition() {
        currentCompositionDisposable = playerInteractor.getCurrentCompositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onCurrentCompositionReceived, errorParser::logError);
        presenterBatterySafeDisposable.add(currentCompositionDisposable);
    }

    protected void subscribeOnCompositions() {
        if (compositions.isEmpty()) {
            getViewState().showLoading();
        }
        dispose(compositionsDisposable, presenterDisposable);
        compositionsDisposable = getCompositionsObservable(searchText)
                .observeOn(uiScheduler)
                .subscribe(this::onCompositionsReceived, this::onCompositionsReceivingError);
        presenterDisposable.add(compositionsDisposable);
    }

    private void onCompositionsReceivingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showLoadingError(errorCommand);
    }

    private void onCompositionsReceived(List<Composition> compositions) {
        this.compositions = compositions;
        getViewState().updateList(compositions);
        if (compositions.isEmpty()) {
            if (TextUtils.isEmpty(searchText)) {
                getViewState().showEmptyList();
            } else {
                getViewState().showEmptySearchResult();
            }
        } else {
            getViewState().showList();

            if (isInactive(currentCompositionDisposable)) {
                subscribeOnCurrentComposition();
            }
        }
    }

    private void onCurrentCompositionReceived(CurrentComposition currentComposition) {
        this.currentComposition = currentComposition.getComposition();
        getViewState().showCurrentComposition(currentComposition);
    }

    private void subscribeOnUiSettings() {
        presenterDisposable.add(displaySettingsInteractor.getCoversEnabledObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onUiSettingsReceived, errorParser::logError));
    }

    private void onUiSettingsReceived(boolean isCoversEnabled) {
        getViewState().setDisplayCoversEnabled(isCoversEnabled);
    }

    protected abstract Observable<List<Composition>> getCompositionsObservable(String searchText);

}
