package com.github.anrimian.musicplayer.ui.settings.folders;

import android.annotation.SuppressLint;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import moxy.MvpPresenter;


public class ExcludedFoldersPresenter extends MvpPresenter<ExcludedFoldersView> {

    private final LibraryFoldersInteractor interactor;
    private final Scheduler uiScheduler;
    private final ErrorParser errorParser;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    @Nullable
    private IgnoredFolder recentlyRemovedFolder;

    public ExcludedFoldersPresenter(LibraryFoldersInteractor interactor,
                                    Scheduler uiScheduler,
                                    ErrorParser errorParser) {
        this.interactor = interactor;
        this.uiScheduler = uiScheduler;
        this.errorParser = errorParser;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnIgnoredFoldersList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    @SuppressLint("CheckResult")
    void onDeleteFolderClicked(IgnoredFolder folder) {
        //noinspection ResultOfMethodCallIgnored
        interactor.deleteIgnoredFolder(folder)
                .toSingleDefault(folder)
                .observeOn(uiScheduler)
                .subscribe(this::onFolderRemoved, this::onDefaultError);
    }

    void onRestoreRemovedFolderClicked() {
        if (recentlyRemovedFolder == null) {
            return;
        }
        presenterDisposable.add(interactor.addFolderToIgnore(recentlyRemovedFolder)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onFoldersListError));
    }

    private void onFolderRemoved(IgnoredFolder folder) {
        recentlyRemovedFolder = folder;
        getViewState().showRemovedFolderMessage(folder);
    }

    private void subscribeOnIgnoredFoldersList() {
        presenterDisposable.add(interactor.getIgnoredFoldersObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onFoldersListReceived, this::onFoldersListError));
    }

    private void onFoldersListReceived(List<IgnoredFolder> folders) {
        getViewState().showExcludedFoldersList(folders);
        if (folders.isEmpty()) {
            getViewState().showEmptyListState();
        } else {
            getViewState().showListState();
        }
    }

    private void onFoldersListError(Throwable throwable) {
        getViewState().showErrorState(errorParser.parseError(throwable));
    }

    private void onDefaultError(Throwable throwable) {
        getViewState().showErrorMessage(errorParser.parseError(throwable));
    }

}
