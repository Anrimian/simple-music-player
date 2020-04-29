package com.github.anrimian.musicplayer.ui.library.artists.list;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.utils.TextUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import moxy.InjectViewState;
import moxy.MvpPresenter;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;

@InjectViewState
public class ArtistsListPresenter extends MvpPresenter<ArtistsListView> {

    private final LibraryArtistsInteractor interactor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable artistsDisposable;
    private Disposable changeDisposable;

    private List<Artist> artists = new ArrayList<>();

    @Nullable
    private String searchText;

    public ArtistsListPresenter(LibraryArtistsInteractor interactor,
                                ErrorParser errorParser,
                                Scheduler uiScheduler) {
        this.interactor = interactor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnArtistsList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onTryAgainLoadCompositionsClicked() {
        subscribeOnArtistsList();
    }

    void onOrderMenuItemClicked() {
        getViewState().showSelectOrderScreen(interactor.getOrder());
    }

    void onOrderSelected(Order order) {
        interactor.setOrder(order);
    }

    void onSearchTextChanged(String text) {
        if (!TextUtils.equals(searchText, text)) {
            searchText = text;
            subscribeOnArtistsList();
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

    @Nullable
    String getSearchText() {
        return searchText;
    }

    private void subscribeOnArtistsList() {
        if (artists.isEmpty()) {
            getViewState().showLoading();
        }
        dispose(artistsDisposable, presenterDisposable);
        artistsDisposable = interactor.getArtistsObservable(searchText)
                .observeOn(uiScheduler)
                .subscribe(this::onArtistsReceived, this::onArtistsReceivingError);
        presenterDisposable.add(artistsDisposable);
    }

    private void onArtistsReceivingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showLoadingError(errorCommand);
    }

    private void onArtistsReceived(List<Artist> artists) {
        this.artists = artists;
        getViewState().submitList(artists);
        if (artists.isEmpty()) {
            if (TextUtils.isEmpty(searchText)) {
                getViewState().showEmptyList();
            } else {
                getViewState().showEmptySearchResult();
            }
        } else {
            getViewState().showList();
        }
    }

    private void onDefaultError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showErrorMessage(errorCommand);
    }
}
