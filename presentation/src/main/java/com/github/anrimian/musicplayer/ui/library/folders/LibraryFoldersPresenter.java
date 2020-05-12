package com.github.anrimian.musicplayer.ui.library.folders;

import android.annotation.SuppressLint;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersScreenInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.MvpPresenter;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.isActive;
import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.isInactive;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

/**
 * Created on 23.10.2017.
 */


public class LibraryFoldersPresenter extends MvpPresenter<LibraryFoldersView> {

    private final LibraryFoldersScreenInteractor interactor;
    private final MusicPlayerInteractor playerInteractor;
    private final DisplaySettingsInteractor displaySettingsInteractor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private final CompositeDisposable presenterBatterySafeDisposable = new CompositeDisposable();

    private Disposable folderDisposable;
    private Disposable currentCompositionDisposable;

    private Disposable playActionDisposable;
    private Disposable deleteActionDisposable;
    private Disposable playlistActionDisposable;
    private Disposable shareActionDisposable;
    private Disposable fileActionDisposable;

    @Nullable
    private final Long folderId;

    private List<FileSource> sourceList = new ArrayList<>();

    private final List<FileSource> filesForPlayList = new LinkedList<>();
    private final List<FileSource> filesToDelete = new LinkedList<>();
    private final LinkedHashSet<FileSource> selectedFiles = new LinkedHashSet<>();

    @Nullable
    private String searchText;

    @Nullable
    private Composition currentComposition;

    @Nullable
    private IgnoredFolder recentlyAddedIgnoredFolder;

    public LibraryFoldersPresenter(@Nullable Long folderId,
                                   LibraryFoldersScreenInteractor interactor,
                                   MusicPlayerInteractor playerInteractor,
                                   DisplaySettingsInteractor displaySettingsInteractor,
                                   ErrorParser errorParser,
                                   Scheduler uiScheduler) {
        this.folderId = folderId;
        this.interactor = interactor;
        this.playerInteractor = playerInteractor;
        this.displaySettingsInteractor = displaySettingsInteractor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showSearchMode(false);
        getViewState().hideProgressDialog();

        subscribeOnFolder();
        subscribeOnChildFolders();
        subscribeOnUiSettings();
        subscribeOnMoveEnabledState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onStart() {
        if (!sourceList.isEmpty()) {
            subscribeOnCurrentComposition();
        }
    }

    void onStop() {
        presenterBatterySafeDisposable.clear();
    }

    void onTryAgainButtonClicked() {
        subscribeOnChildFolders();
    }

    void onCompositionClicked(int position, CompositionFileSource musicFileSource) {
        processMultiSelectClick(position, musicFileSource, () -> {
            Composition composition = musicFileSource.getComposition();
            getViewState().showCompositionActionDialog(composition);
        });
    }

    void onCompositionIconClicked(int position, Composition composition) {
        if (composition.equals(currentComposition)) {
            playerInteractor.playOrPause();
        } else {
            interactor.play(folderId, composition);
            getViewState().showCurrentComposition(new CurrentComposition(composition, true));
        }
    }

    void onPlayActionSelected(Composition composition) {
        interactor.play(folderId, composition);
    }

    void onPlayNextCompositionClicked(Composition composition) {
        addCompositionsToPlayNext(asList(composition));
    }

    void onAddToQueueCompositionClicked(Composition composition) {
        addCompositionsToEnd(asList(composition));
    }

    void onPlayFolderClicked(FolderFileSource folder) {
        interactor.play(asList(folder));
    }

    void onPlayNextFolderClicked(FolderFileSource folder) {
        dispose(playActionDisposable, presenterDisposable);
        playActionDisposable = interactor.getAllCompositionsInFolder(folder.getId())
                .flatMap(playerInteractor::addCompositionsToPlayNext)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::onCompositionsAddedToPlayNext, this::onDefaultError);
        presenterDisposable.add(playActionDisposable);
    }

    void onAddToQueueFolderClicked(FolderFileSource folder) {
        dispose(playActionDisposable, presenterDisposable);
        playActionDisposable = interactor.getAllCompositionsInFolder(folder.getId())
                .flatMap(playerInteractor::addCompositionsToEnd)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::onCompositionsAddedToQueue, this::onDefaultError);
        presenterDisposable.add(playActionDisposable);
    }

    void onPlayAllButtonClicked() {
        if (selectedFiles.isEmpty()) {
            interactor.playAllMusicInFolder(folderId);
        } else {
            playSelectedCompositions();
        }
    }

    void onBackPathButtonClicked() {
        if (folderId == null) {
            throw new IllegalStateException("can not go back in root screen");
        }
        closeSelectionMode();
        goBackToPreviousScreen();
    }

    void onDeleteCompositionButtonClicked(Composition composition) {
        filesToDelete.clear();
        filesToDelete.add(new CompositionFileSource(composition));
        getViewState().showConfirmDeleteDialog(asList(composition));
    }

    void onDeleteFolderButtonClicked(FolderFileSource folder) {
        getViewState().showConfirmDeleteDialog(folder);
    }

    void onDeleteCompositionsDialogConfirmed() {
        deletePreparedFiles();
    }

    void onDeleteFolderDialogConfirmed(FolderFileSource folder) {
        dispose(deleteActionDisposable, presenterDisposable);
        deleteActionDisposable = interactor.deleteFolder(folder)
                .observeOn(uiScheduler)
                .doOnSubscribe(o -> getViewState().showDeleteProgress())
                .doFinally(() -> getViewState().hideProgressDialog())
                .subscribe(this::onDeleteFolderSuccess, this::onDeleteCompositionsError);
        presenterDisposable.add(deleteActionDisposable);
    }

    void onOrderMenuItemClicked() {
        getViewState().showSelectOrderScreen(interactor.getFolderOrder());
    }

    void onOrderSelected(Order order) {
        interactor.setFolderOrder(order);
    }

    void onAddToPlayListButtonClicked(Composition composition) {
        filesForPlayList.clear();
        filesForPlayList.add(new CompositionFileSource(composition));
        getViewState().showSelectPlayListDialog();
    }

    void onPlayListToAddingSelected(PlayList playList) {
        addPreparedCompositionsToPlayList(playList);
    }

    void onAddFolderToPlayListButtonClicked(FolderFileSource folder) {
        getViewState().showSelectPlayListForFolderDialog(folder);
    }

    void onPlayListForFolderSelected(Long folderId, PlayList playList) {
        dispose(playlistActionDisposable, presenterDisposable);
        playlistActionDisposable = interactor.addCompositionsToPlayList(folderId, playList)
                .observeOn(uiScheduler)
                .subscribe(addedCompositions ->
                                getViewState().showAddingToPlayListComplete(playList, addedCompositions),
                        this::onAddingToPlayListError);
        presenterDisposable.add(playlistActionDisposable);
    }

    void onSearchTextChanged(String text) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text;
            subscribeOnChildFolders();
        }
    }

    void onSearchButtonClicked() {
        getViewState().showSearchMode(true);
    }

    void onShareFolderClicked(FolderFileSource folder) {
        shareFileSources(asList(folder));
    }

    void onFolderClicked(int position, FolderFileSource folder) {
        processMultiSelectClick(position, folder, () ->
                getViewState().goToMusicStorageScreen(folder.getId())
        );
    }

    void onItemLongClick(int position, FileSource folder) {
        interactor.stopMoveMode();
        getViewState().updateMoveFilesList();

        selectedFiles.add(folder);
        getViewState().showSelectionMode(selectedFiles.size());
        getViewState().onItemSelected(folder, position);
    }

    void onFragmentDisplayed() {
        interactor.saveCurrentFolder(folderId);
    }

    void onRenameFolderClicked(FolderFileSource folder) {
        getViewState().showInputFolderNameDialog(folder);
    }

    void onNewFolderNameEntered(long folderId, String name) {
        if (isActive(fileActionDisposable)) {
            return;
        }
        dispose(fileActionDisposable);
        fileActionDisposable = interactor.renameFolder(folderId, name)
                .observeOn(uiScheduler)
                .doOnSubscribe(o -> getViewState().showRenameProgress())
                .doFinally(() -> getViewState().hideProgressDialog())
                .subscribe(() -> {}, this::onDefaultError);
    }

    void onSelectionModeBackPressed() {
        closeSelectionMode();
    }

    void onPlayAllSelectedClicked() {
        playSelectedCompositions();
    }

    void onSelectAllButtonClicked() {
        selectedFiles.clear();//reselect previous feature
        selectedFiles.addAll(sourceList);
        getViewState().showSelectionMode(selectedFiles.size());
        getViewState().setItemsSelected(true);
    }

    private void playSelectedCompositions() {
        interactor.play(new ArrayList<>(selectedFiles));
        closeSelectionMode();
    }

    @SuppressLint("CheckResult")
    void onPlayNextSelectedSourcesClicked() {
        interactor.addCompositionsToPlayNext(new ArrayList<>(selectedFiles))
                .observeOn(uiScheduler)
                .subscribe(getViewState()::onCompositionsAddedToPlayNext, this::onDefaultError);
        closeSelectionMode();
    }

    @SuppressLint("CheckResult")
    void onAddToQueueSelectedSourcesClicked() {
        interactor.addCompositionsToEnd(new ArrayList<>(selectedFiles))
                .observeOn(uiScheduler)
                .subscribe(getViewState()::onCompositionsAddedToQueue, this::onDefaultError);
        closeSelectionMode();
    }

    void onAddSelectedSourcesToPlayListClicked() {
        filesForPlayList.clear();
        filesForPlayList.addAll(selectedFiles);
        getViewState().showSelectPlayListDialog();
    }

    void onShareSelectedSourcesClicked() {
        shareFileSources(new ArrayList<>(selectedFiles));
    }

    void onDeleteSelectedCompositionButtonClicked() {
        filesToDelete.clear();
        filesToDelete.addAll(selectedFiles);
        deleteActionDisposable = interactor.getAllCompositionsInFileSources(new ArrayList<>(selectedFiles))
                .observeOn(uiScheduler)
                .doOnSubscribe(o -> getViewState().showDeleteProgress())
                .doFinally(() -> getViewState().hideProgressDialog())
                .subscribe(getViewState()::showConfirmDeleteDialog, this::onDefaultError);
    }

    void onMoveSelectedFoldersButtonClicked() {
        interactor.addFilesToMove(folderId, selectedFiles);
        closeSelectionMode();
        getViewState().updateMoveFilesList();
    }

    void onCopySelectedFoldersButtonClicked() {
        interactor.addFilesToCopy(folderId, selectedFiles);
        closeSelectionMode();
    }

    void onCloseMoveMenuClicked() {
        if (isActive(fileActionDisposable)) {
            return;
        }
        interactor.stopMoveMode();
        getViewState().updateMoveFilesList();
    }

    void onPasteButtonClicked() {
        if (isActive(fileActionDisposable)) {
            return;
        }
        dispose(fileActionDisposable);
        fileActionDisposable = interactor.moveFilesTo(folderId)
                .observeOn(uiScheduler)
                .doOnSubscribe(o -> getViewState().showMoveProgress())
                .doFinally(() -> getViewState().hideProgressDialog())
                .subscribe(getViewState()::updateMoveFilesList, this::onDefaultError);
    }

    void onPasteInNewFolderButtonClicked() {
        getViewState().showInputNewFolderNameDialog();
    }

    void onNewFileNameForPasteEntered(String name) {
        if (isActive(fileActionDisposable)) {
            return;
        }
        dispose(fileActionDisposable);
        fileActionDisposable = interactor.moveFilesToNewFolder(folderId, name)
                .observeOn(uiScheduler)
                .doOnSubscribe(o -> getViewState().showMoveProgress())
                .doFinally(() -> getViewState().hideProgressDialog())
                .subscribe(getViewState()::updateMoveFilesList, this::onDefaultError);
    }

    @SuppressLint("CheckResult")
    void onExcludeFolderClicked(FolderFileSource folder) {
        //noinspection ResultOfMethodCallIgnored
        interactor.addFolderToIgnore(folder)
                .observeOn(uiScheduler)
                .subscribe(this::onIgnoreFolderAdded, this::onDefaultError);
    }

    @SuppressLint("CheckResult")
    void onRemoveIgnoredFolderClicked() {
        //noinspection ResultOfMethodCallIgnored
        interactor.deleteIgnoredFolder(recentlyAddedIgnoredFolder)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
    }

    LinkedHashSet<FileSource> getSelectedFiles() {
        return selectedFiles;
    }

    LinkedHashSet<FileSource> getSelectedMoveFiles() {
        return interactor.getFilesToMove();
    }

    private void onIgnoreFolderAdded(IgnoredFolder folder) {
        recentlyAddedIgnoredFolder = folder;
        getViewState().showAddedIgnoredFolderMessage(folder);
    }

    private void shareFileSources(List<FileSource> fileSources) {
        dispose(shareActionDisposable, presenterDisposable);
        shareActionDisposable = interactor.getAllCompositionsInFileSources(fileSources)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::sendCompositions, this::onReceiveCompositionsError);
        presenterDisposable.add(shareActionDisposable);
    }

    private void processMultiSelectClick(int position, FileSource folder, Runnable onClick) {
        if (selectedFiles.isEmpty()) {
            onClick.run();
            closeSelectionMode();
        } else {
            if (selectedFiles.contains(folder)) {
                selectedFiles.remove(folder);
                getViewState().onItemUnselected(folder, position);
            } else {
                selectedFiles.add(folder);
                getViewState().onItemSelected(folder, position);
            }
            getViewState().showSelectionMode(selectedFiles.size());
        }
    }

    private void closeSelectionMode() {
        selectedFiles.clear();
        getViewState().showSelectionMode(0);
        getViewState().setItemsSelected(false);
    }

    private void addCompositionsToPlayNext(List<Composition> compositions) {
        dispose(playActionDisposable, presenterDisposable);
        playActionDisposable = playerInteractor.addCompositionsToPlayNext(compositions)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::onCompositionsAddedToPlayNext, this::onDefaultError);
        presenterDisposable.add(playActionDisposable);
    }

    private void addCompositionsToEnd(List<Composition> compositions) {
        dispose(playActionDisposable, presenterDisposable);
        playActionDisposable = playerInteractor.addCompositionsToEnd(compositions)
                .observeOn(uiScheduler)
                .subscribe(getViewState()::onCompositionsAddedToQueue, this::onDefaultError);
        presenterDisposable.add(playActionDisposable);
    }

    private void onDefaultError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showErrorMessage(errorCommand);
    }

    private void onReceiveCompositionsError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showReceiveCompositionsForSendError(errorCommand);
    }

    private void deletePreparedFiles() {
        dispose(deleteActionDisposable, presenterDisposable);
        deleteActionDisposable = interactor.deleteFiles(filesToDelete)
                .observeOn(uiScheduler)
                .subscribe(this::onDeleteCompositionsSuccess, this::onDeleteCompositionsError);
        presenterDisposable.add(deleteActionDisposable);
    }

    private void onDeleteFolderSuccess(List<Composition> deletedCompositions) {
        getViewState().showDeleteCompositionMessage(deletedCompositions);
    }

    private void onDeleteCompositionsSuccess(List<Composition> compositions) {
        getViewState().showDeleteCompositionMessage(compositions);
        filesToDelete.clear();

        if (!selectedFiles.isEmpty()) {
            closeSelectionMode();
        }
    }

    private void onDeleteCompositionsError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showDeleteCompositionError(errorCommand);
    }

    private void addPreparedCompositionsToPlayList(PlayList playList) {
        dispose(playlistActionDisposable, presenterDisposable);
        playlistActionDisposable = interactor.addCompositionsToPlayList(filesForPlayList, playList)
                .observeOn(uiScheduler)
                .subscribe(compositions -> onAddingToPlayListCompleted(compositions, playList),
                        this::onAddingToPlayListError);
        presenterDisposable.add(playlistActionDisposable);
    }

    private void onAddingToPlayListError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAddingToPlayListError(errorCommand);
    }

    private void onAddingToPlayListCompleted(List<Composition> compositions, PlayList playList) {
        getViewState().showAddingToPlayListComplete(playList, compositions);
        filesForPlayList.clear();

        if (!selectedFiles.isEmpty()) {
            closeSelectionMode();
        }
    }

    private void goBackToPreviousScreen() {
        if (folderId != null) {
            getViewState().goBackToParentFolderScreen();
        }
    }

    private void subscribeOnFolder() {
        if (folderId == null) {
            getViewState().hideFolderInfo();
            return;
        }
        presenterDisposable.add(interactor.getFolderObservable(folderId)
                .observeOn(uiScheduler)
                .subscribe(
                        getViewState()::showFolderInfo,
                        this::onDefaultError,
                        this::goBackToPreviousScreen)
        );
    }

    private void subscribeOnChildFolders() {
        if (sourceList.isEmpty()) {
            getViewState().showLoading();
        }
        dispose(folderDisposable, presenterDisposable);
        folderDisposable = interactor.getFoldersInFolder(folderId, searchText)
                .observeOn(uiScheduler)
                .subscribe(this::onFilesLoaded, this::onMusicLoadingError);
        presenterDisposable.add(folderDisposable);
    }

    private void onFilesLoaded(List<FileSource> files) {
        this.sourceList = files;
        getViewState().updateList(sourceList);
        if (sourceList.isEmpty()) {
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

    private void onMusicLoadingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showError(errorCommand);
    }

    private void subscribeOnCurrentComposition() {
        currentCompositionDisposable = playerInteractor.getCurrentCompositionObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onCurrentCompositionReceived, errorParser::logError);
        presenterBatterySafeDisposable.add(currentCompositionDisposable);
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

    private void subscribeOnMoveEnabledState() {
        presenterDisposable.add(interactor.getMoveModeObservable()
                .observeOn(uiScheduler)
                .subscribe(getViewState()::showMoveFileMenu));
    }

}