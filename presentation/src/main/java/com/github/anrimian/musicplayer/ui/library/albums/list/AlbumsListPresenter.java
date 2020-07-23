package com.github.anrimian.musicplayer.ui.library.albums.list;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryAlbumsInteractor;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
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
import moxy.MvpPresenter;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.dispose;


public class AlbumsListPresenter extends MvpPresenter<AlbumsListView> {

    private final LibraryAlbumsInteractor interactor;
    private final ErrorParser errorParser;
    private final Scheduler uiScheduler;

    private final CompositeDisposable presenterDisposable = new CompositeDisposable();
    private Disposable albumsDisposable;

    private List<Album> albums = new ArrayList<>();

    @Nullable
    private String searchText;

    public AlbumsListPresenter(LibraryAlbumsInteractor interactor,
                               ErrorParser errorParser,
                               Scheduler uiScheduler) {
        this.interactor = interactor;
        this.errorParser = errorParser;
        this.uiScheduler = uiScheduler;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        subscribeOnAlbumsList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenterDisposable.dispose();
    }

    void onTryAgainLoadCompositionsClicked() {
        subscribeOnAlbumsList();
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
            subscribeOnAlbumsList();
        }
    }

    @Nullable
    String getSearchText() {
        return searchText;
    }

    private void subscribeOnAlbumsList() {
        if (albums.isEmpty()) {
            getViewState().showLoading();
        }
        dispose(albumsDisposable, presenterDisposable);
        albumsDisposable = interactor.getAlbumsObservable(searchText)
                .observeOn(uiScheduler)
                .subscribe(this::onAlbumsReceived, this::onAlbumsReceivingError);
        presenterDisposable.add(albumsDisposable);
    }

    private void onAlbumsReceivingError(Throwable throwable) {
        ErrorCommand errorCommand = errorParser.parseError(throwable);
        getViewState().showLoadingError(errorCommand);
    }

    private void onAlbumsReceived(List<Album> albums) {
        this.albums = albums;
        getViewState().submitList(albums);
        if (albums.isEmpty()) {
            if (TextUtils.isEmpty(searchText)) {
                getViewState().showEmptyList();
            } else {
                getViewState().showEmptySearchResult();
            }
        } else {
            getViewState().showList();
        }
    }
}
