package com.github.anrimian.musicplayer.ui.editor.composition;

import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.MvpPresenter;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;


public class CompositionEditorPresenter extends MvpPresenter<CompositionEditorView> {

    private final long compositionId;
    private final EditorInteractor editorInteractor;
    private final Scheduler uiScheduler;
    private final ErrorParser errorParser;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable changeDisposable;

    private FullComposition composition;

    private ShortGenre removedGenre;

    public CompositionEditorPresenter(long compositionId,
                                      EditorInteractor editorInteractor,
                                      Scheduler uiScheduler,
                                      ErrorParser errorParser) {
        this.compositionId = compositionId;
        this.editorInteractor = editorInteractor;
        this.uiScheduler = uiScheduler;
        this.errorParser = errorParser;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        loadComposition();
        loadGenres();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onChangeAuthorClicked() {
        if (composition == null) {
            return;
        }
        editorInteractor.getAuthorNames()
                .observeOn(uiScheduler)
                .doOnSuccess(artists -> getViewState().showEnterAuthorDialog(composition, artists))
                .doOnError(throwable -> {
                    getViewState().showEnterAuthorDialog(composition, null);
                    onDefaultError(throwable);
                })
                .ignoreElement()
                .onErrorComplete()
                .subscribe();
    }

    void onChangeTitleClicked() {
        if (composition == null) {
            return;
        }
        getViewState().showEnterTitleDialog(composition);
    }

    void onChangeFileNameClicked() {
        if (composition == null) {
            return;
        }
        getViewState().showEnterFileNameDialog(composition);
    }

    void onChangeAlbumClicked() {
        if (composition == null) {
            return;
        }
        editorInteractor.getAlbumNames()
                .observeOn(uiScheduler)
                .doOnSuccess(albums -> getViewState().showEnterAlbumDialog(composition, albums))
                .doOnError(throwable -> {
                    getViewState().showEnterAlbumDialog(composition, null);
                    onDefaultError(throwable);
                })
                .ignoreElement()
                .onErrorComplete()
                .subscribe();
    }

    void onChangeAlbumArtistClicked() {
        if (composition == null) {
            return;
        }
        editorInteractor.getAuthorNames()
                .observeOn(uiScheduler)
                .doOnSuccess(albums -> getViewState().showEnterAlbumArtistDialog(composition, albums))
                .doOnError(throwable -> {
                    getViewState().showEnterAlbumArtistDialog(composition, null);
                    onDefaultError(throwable);
                })
                .ignoreElement()
                .onErrorComplete()
                .subscribe();
    }

    void onAddGenreItemClicked() {
        editorInteractor.getGenreNames()
                .observeOn(uiScheduler)
                .doOnSuccess(genres -> getViewState().showAddGenreDialog(genres))
                .doOnError(throwable -> {
                    getViewState().showAddGenreDialog(null);
                    onDefaultError(throwable);
                })
                .ignoreElement()
                .onErrorComplete()
                .subscribe();
    }

    void onGenreItemClicked(ShortGenre genre) {
        editorInteractor.getGenreNames()
                .observeOn(uiScheduler)
                .doOnSuccess(genres -> getViewState().showEditGenreDialog(genre, genres))
                .doOnError(throwable -> {
                    getViewState().showEditGenreDialog(genre, null);
                    onDefaultError(throwable);
                })
                .ignoreElement()
                .onErrorComplete()
                .subscribe();
    }

    void onNewGenreEntered(String genre) {
        if (composition == null) {
            return;
        }

        dispose(changeDisposable, presenterDisposable);
        changeDisposable = editorInteractor.addCompositionGenre(composition, genre)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(changeDisposable);
    }

    void onNewGenreNameEntered(String newName, ShortGenre oldGenre) {
        if (composition == null) {
            return;
        }

        dispose(changeDisposable, presenterDisposable);
        changeDisposable = editorInteractor.changeCompositionGenre(composition, oldGenre, newName)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(changeDisposable);
    }

    void onRemoveGenreClicked(ShortGenre genre) {
        if (composition == null) {
            return;
        }

        dispose(changeDisposable, presenterDisposable);
        changeDisposable = editorInteractor.removeCompositionGenre(composition, genre)
                .observeOn(uiScheduler)
                .subscribe(() -> onGenreRemoved(genre), this::onDefaultError);
        presenterDisposable.add(changeDisposable);
    }

    void onRestoreRemovedGenreClicked() {
        if (removedGenre == null) {
            return;
        }
        onNewGenreEntered(removedGenre.getName());
    }

    void onNewAuthorEntered(String author) {
        if (composition == null) {
            return;
        }

        dispose(changeDisposable, presenterDisposable);
        changeDisposable = editorInteractor.editCompositionAuthor(composition, author)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(changeDisposable);
    }

    void onNewAlbumEntered(String album) {
        if (composition == null) {
            return;
        }

        dispose(changeDisposable, presenterDisposable);
        changeDisposable = editorInteractor.editCompositionAlbum(composition, album)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(changeDisposable);
    }

    void onNewAlbumArtistEntered(String artist) {
        if (composition == null) {
            return;
        }

        dispose(changeDisposable, presenterDisposable);
        changeDisposable = editorInteractor.editCompositionAlbumArtist(composition, artist)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(changeDisposable);
    }

    void onNewTitleEntered(String title) {
        if (composition == null) {
            return;
        }

        dispose(changeDisposable, presenterDisposable);
        changeDisposable = editorInteractor.editCompositionTitle(composition, title)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(changeDisposable);
    }

    void onNewFileNameEntered(String fileName) {
        if (composition == null) {
            return;
        }

        dispose(changeDisposable, presenterDisposable);
        changeDisposable = editorInteractor.editCompositionFileName(composition, fileName)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError);
        presenterDisposable.add(changeDisposable);
    }

    void onCopyFileNameClicked() {
        if (composition == null) {
            return;
        }
        getViewState().copyFileNameText(composition.getFileName());
    }

    private void onDefaultError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showErrorMessage(errorCommand);
    }

    private void loadComposition() {
        presenterDisposable.add(editorInteractor.getCompositionObservable(compositionId)
                .observeOn(uiScheduler)
                .subscribe(this::onCompositionReceived,
                        this::onCompositionLoadingError,
                        getViewState()::closeScreen));
    }

    private void loadGenres() {
        presenterDisposable.add(editorInteractor.getShortGenresInComposition(compositionId)
                .observeOn(uiScheduler)
                .subscribe(this::onGenresReceived, this::onDefaultError));
    }

    private void onGenresReceived(List<ShortGenre> shortGenres) {
        getViewState().showGenres(shortGenres);
    }

    private void onCompositionReceived(FullComposition composition) {
        if (this.composition == null) {
            checkCompositionTagsInSource(composition);
        }
        this.composition = composition;
        getViewState().showComposition(composition);
    }

    private void checkCompositionTagsInSource(FullComposition composition) {
        presenterDisposable.add(editorInteractor.updateTagsFromSource(composition)
                .observeOn(uiScheduler)
                .subscribe(() -> {}, this::onDefaultError));

    }

    private void onCompositionLoadingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showCompositionLoadingError(errorCommand);
    }

    private void onGenreRemoved(ShortGenre genre) {
        removedGenre = genre;
        getViewState().showRemovedGenreMessage(genre);
    }
}
