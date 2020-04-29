package com.github.anrimian.musicplayer.ui.library.folders.root;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersScreenInteractor;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;

@InjectViewState
public class FolderRootPresenter extends MvpPresenter<FolderRootView> {

    private final LibraryFoldersScreenInteractor interactor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable filesDisposable;

    public FolderRootPresenter(LibraryFoldersScreenInteractor interactor,
                               ErrorParser errorParser,
                               Scheduler uiScheduler) {
        this.interactor = interactor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showIdle();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onEmptyFolderStackArrived() {
        getViewState().showProgress();
        dispose(filesDisposable, presenterDisposable);
        filesDisposable = interactor.getCurrentFolderScreens()
                .observeOn(uiScheduler)
                .subscribe(this::onScreensReceived, this::onScreensReceivingError);
        presenterDisposable.add(filesDisposable);
    }

    private void onScreensReceivingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showError(errorCommand);
    }

    private void onScreensReceived(List<Long> ids) {
        getViewState().showIdle();
        getViewState().showFolderScreens(ids);
    }
}
