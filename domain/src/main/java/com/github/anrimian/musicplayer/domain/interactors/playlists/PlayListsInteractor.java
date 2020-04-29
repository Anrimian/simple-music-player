package com.github.anrimian.musicplayer.domain.interactors.playlists;

import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.interactors.playlists.validators.PlayListNameValidator;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.repositories.PlayListsRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class PlayListsInteractor {

    private final PlayListNameValidator nameValidator = new PlayListNameValidator();

    private final PlayListsRepository playListsRepository;
    private final UiStateRepository uiStateRepository;
    private final Analytics analytics;

    public PlayListsInteractor(PlayListsRepository playListsRepository,
                               UiStateRepository uiStateRepository,
                               Analytics analytics) {
        this.playListsRepository = playListsRepository;
        this.uiStateRepository = uiStateRepository;
        this.analytics = analytics;
    }

    public Observable<List<PlayList>> getPlayListsObservable() {
        return playListsRepository.getPlayListsObservable();
    }

    public Observable<PlayList> getPlayListObservable(long playListId) {
        return playListsRepository.getPlayListObservable(playListId);
    }

    public Observable<List<PlayListItem>> getCompositionsObservable(long playlistId) {
        return playListsRepository.getCompositionsObservable(playlistId);
    }

    public Single<PlayList> createPlayList(String name) {
        return nameValidator.validate(name)
                .flatMap(playListsRepository::createPlayList);
    }

    public Completable addCompositionsToPlayList(List<Composition> compositions,
                                                 PlayList playList,
                                                 int position) {
        return playListsRepository.addCompositionsToPlayList(compositions, playList, position);
    }

    public Completable addCompositionsToPlayList(List<Composition> compositions, PlayList playList) {
        return playListsRepository.addCompositionsToPlayList(compositions, playList);
    }

    public Completable deleteItemFromPlayList(PlayListItem playListItem, long playListId) {
        return playListsRepository.deleteItemFromPlayList(playListItem, playListId);
    }

    public Completable deletePlayList(long playListId) {
        return playListsRepository.deletePlayList(playListId);
    }

    public void moveItemInPlayList(PlayList playList, int from, int to) {
        playListsRepository.moveItemInPlayList(playList, from, to)
                .doOnError(analytics::processNonFatalError)
                .onErrorComplete()
                .subscribe();
    }

    public Completable updatePlayListName(long playListId, String name) {
        return nameValidator.validate(name)
                .flatMapCompletable(o -> playListsRepository.updatePlayListName(playListId, name));
    }

    public void setSelectedPlayListScreen(long playListId) {
        uiStateRepository.setSelectedPlayListScreenId(playListId);
    }
}
