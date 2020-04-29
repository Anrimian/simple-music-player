package com.github.anrimian.musicplayer.ui.playlist_screens.rename;

import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

@InjectViewState
public class RenamePlayListPresenter extends MvpPresenter<RenamePlayListView> {

    private final PlayListsInteractor playListsInteractor;
    private final Scheduler uiScheduler;
    private final ErrorParser errorParser;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();

    private final long playListId;

    private String initialName;

    public RenamePlayListPresenter(long playListId,
                                   PlayListsInteractor playListsInteractor,
                                   Scheduler uiScheduler,
                                   ErrorParser errorParser) {
        this.playListId = playListId;
        this.playListsInteractor = playListsInteractor;
        this.uiScheduler = uiScheduler;
        this.errorParser = errorParser;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showInputState();
        loadPlayListInfo();
        //compare names and disable apply button, just a little feature
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onCompleteInputButtonClicked(String playListName) {
        presenterDisposable.add(playListsInteractor.updatePlayListName(playListId, playListName)
                .observeOn(uiScheduler)
                .doOnSubscribe(d -> getViewState().showProgress())
                .subscribe(getViewState()::closeScreen, this::onDefaultError));
    }

    private void onDefaultError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showError(errorCommand);
    }

    private void loadPlayListInfo() {
        playListsInteractor.getPlayListObservable(playListId)
                .observeOn(uiScheduler)
                .subscribe(this::onPlayListInfoReceived,
                        this::onDefaultError,
                        getViewState()::closeScreen
                );
    }

    private void onPlayListInfoReceived(PlayList playList) {
        initialName = playList.getName();
        getViewState().showPlayListName(initialName);
    }
}
