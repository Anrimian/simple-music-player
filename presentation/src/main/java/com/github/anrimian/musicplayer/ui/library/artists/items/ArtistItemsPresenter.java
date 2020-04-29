package com.github.anrimian.musicplayer.ui.library.artists.items;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;

@InjectViewState
public class ArtistItemsPresenter extends BaseLibraryCompositionsPresenter<ArtistItemsView> {

    private final long artistId;
    private final LibraryArtistsInteractor interactor;

    private Disposable changeDisposable;

    private Artist artist;

    public ArtistItemsPresenter(long artistId,
                                LibraryArtistsInteractor interactor,
                                PlayListsInteractor playListsInteractor,
                                MusicPlayerInteractor playerInteractor,
                                DisplaySettingsInteractor displaySettingsInteractor,
                                ErrorParser errorParser,
                                Scheduler uiScheduler) {
        super(playerInteractor,
                playListsInteractor,
                displaySettingsInteractor,
                errorParser,
                uiScheduler);
        this.artistId = artistId;
        this.interactor = interactor;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnArtistInfo();
        subscribeOnArtistAlbums();
    }

    @Override
    protected Observable<List<Composition>> getCompositionsObservable(String searchText) {
        return interactor.getCompositionsByArtist(artistId);
    }

    void onFragmentMovedToTop() {
        //save selected screen. Wait a little for all screens
        if (artist != null) {
            getViewState().showArtistInfo(artist);
        }
    }

    void onRenameArtistClicked() {
        if (artist != null) {
            getViewState().showRenameArtistDialog(artist);
        }
    }

    void onNewArtistNameEntered(String name, long artistId) {
        dispose(changeDisposable);
        changeDisposable = interactor.updateArtistName(name, artistId)
                .observeOn(uiScheduler)
                .doOnSubscribe(d -> getViewState().showRenameProgress())
                .doFinally(() -> getViewState().hideRenameProgress())
                .subscribe(() -> {}, this::onDefaultError);
    }

    private void subscribeOnArtistInfo() {
        presenterDisposable.add(interactor.getArtistObservable(artistId)
                .observeOn(uiScheduler)
                .subscribe(this::onArtistInfoReceived,
                        t -> getViewState().closeScreen(),
                        getViewState()::closeScreen));
    }

    private void onArtistInfoReceived(Artist artist) {
        this.artist = artist;
        getViewState().showArtistInfo(artist);
    }

    private void subscribeOnArtistAlbums() {
        presenterDisposable.add(interactor.getAllAlbumsForArtist(artistId)
                .observeOn(uiScheduler)
                .subscribe(this::onArtistInfoReceived,
                        t -> getViewState().closeScreen(),
                        getViewState()::closeScreen));
    }

    private void onArtistInfoReceived(List<Album> albums) {
        getViewState().showArtistAlbums(albums);
    }
}
