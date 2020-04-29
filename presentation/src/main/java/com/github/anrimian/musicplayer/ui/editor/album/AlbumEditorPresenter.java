package com.github.anrimian.musicplayer.ui.editor.album;

import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;

@InjectViewState
public class AlbumEditorPresenter extends MvpPresenter<AlbumEditorView> {

    private final long albumId;
    private final EditorInteractor editorInteractor;
    private final Scheduler uiScheduler;
    private final ErrorParser errorParser;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable changeDisposable;

    private Album album;

    public AlbumEditorPresenter(long albumId,
                                EditorInteractor editorInteractor,
                                Scheduler uiScheduler,
                                ErrorParser errorParser) {
        this.albumId = albumId;
        this.editorInteractor = editorInteractor;
        this.uiScheduler = uiScheduler;
        this.errorParser = errorParser;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        loadAlbum();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onChangeAuthorClicked() {
        if (album == null) {
            return;
        }
        editorInteractor.getAuthorNames()
                .observeOn(uiScheduler)
                .doOnSuccess(artists -> getViewState().showEnterAuthorDialog(album, artists))
                .doOnError(throwable -> {
                    getViewState().showEnterAuthorDialog(album, null);
                    onDefaultError(throwable);
                })
                .ignoreElement()
                .onErrorComplete()
                .subscribe();
    }

    void onChangeNameClicked() {
        if (album == null) {
            return;
        }
        getViewState().showEnterNameDialog(album);
    }

    void onNewAuthorEntered(String author) {
        if (album == null) {
            return;
        }

        dispose(changeDisposable, presenterDisposable);
        changeDisposable = editorInteractor.updateAlbumArtist(author, album.getId())
                .observeOn(uiScheduler)
                .doOnSubscribe(d -> getViewState().showRenameProgress())
                .doFinally(() -> getViewState().hideRenameProgress())
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(changeDisposable);
    }

    void onNewNameEntered(String name) {
        if (album == null) {
            return;
        }

        dispose(changeDisposable, presenterDisposable);
        changeDisposable = editorInteractor.updateAlbumName(name, album.getId())
                .observeOn(uiScheduler)
                .doOnSubscribe(d -> getViewState().showRenameProgress())
                .doFinally(() -> getViewState().hideRenameProgress())
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(changeDisposable);
    }

    private void onDefaultError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showErrorMessage(errorCommand);
    }

    private void loadAlbum() {
        presenterDisposable.add(editorInteractor.getAlbumObservable(albumId)
                .observeOn(uiScheduler)
                .subscribe(this::onAlbumReceived,
                        this::onCompositionLoadingError,
                        getViewState()::closeScreen));
    }

    private void onAlbumReceived(Album album) {
        this.album = album;
        getViewState().showAlbum(album);
    }

    private void onCompositionLoadingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showAlbumLoadingError(errorCommand);
    }
}
