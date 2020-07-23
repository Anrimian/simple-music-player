package com.github.anrimian.musicplayer.ui.library.genres.items;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryGenresInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;


public class GenreItemsPresenter extends BaseLibraryCompositionsPresenter<GenreItemsView> {

    private final long genreId;
    private final LibraryGenresInteractor interactor;

    private Disposable changeDisposable;

    private Genre genre;

    public GenreItemsPresenter(long genreId,
                               LibraryGenresInteractor interactor,
                               PlayListsInteractor playListsInteractor,
                               LibraryPlayerInteractor playerInteractor,
                               DisplaySettingsInteractor displaySettingsInteractor,
                               ErrorParser errorParser,
                               Scheduler uiScheduler) {
        super(playerInteractor,
                playListsInteractor,
                displaySettingsInteractor,
                errorParser,
                uiScheduler);
        this.genreId = genreId;
        this.interactor = interactor;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnGenreInfo();
    }

    @Override
    protected Observable<List<Composition>> getCompositionsObservable(String searchText) {
        return interactor.getGenreItemsObservable(genreId);
    }

    void onFragmentMovedToTop() {
        //save selected genre screen. Wait a little for all screens
    }

    void onRenameGenreClicked() {
        if (genre != null) {
            getViewState().showRenameGenreDialog(genre);
        }
    }

    void onNewGenreNameEntered(String name, long genreId) {
        dispose(changeDisposable);
        changeDisposable = interactor.updateGenreName(name, genreId)
                .observeOn(uiScheduler)
                .doOnSubscribe(d -> getViewState().showRenameProgress())
                .doFinally(() -> getViewState().hideRenameProgress())
                .subscribe(() -> {}, this::onDefaultError);
    }

    private void subscribeOnGenreInfo() {
        presenterDisposable.add(interactor.getGenreObservable(genreId)
                .observeOn(uiScheduler)
                .subscribe(this::onGenreInfoReceived,
                        t -> getViewState().closeScreen(),
                        getViewState()::closeScreen));
    }

    private void onGenreInfoReceived(Genre genre) {
        this.genre = genre;
        getViewState().showGenreInfo(genre);
    }
}
