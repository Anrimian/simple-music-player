package com.github.anrimian.musicplayer.data.repositories.scanner;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.database.dao.genre.GenresDaoWrapper;
import com.github.anrimian.musicplayer.data.database.entities.IdPair;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenre;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenreItem;
import com.github.anrimian.musicplayer.data.storage.providers.genres.StorageGenresProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.musicplayer.data.utils.collections.AndroidCollectionUtils;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class MediaScannerRepositoryImpl implements MediaScannerRepository {

    private final StorageMusicProvider musicProvider;
    private final StoragePlayListsProvider playListsProvider;
    private final StorageGenresProvider genresProvider;
    private final GenresDaoWrapper genresDao;
    private final StorageCompositionAnalyzer compositionAnalyzer;
    private final StoragePlaylistAnalyzer playlistAnalyzer;
    private final Scheduler scheduler;

    private CompositeDisposable mediaStoreDisposable = new CompositeDisposable();
    private LongSparseArray<Disposable> genreEntriesDisposable = new LongSparseArray<>();

    public MediaScannerRepositoryImpl(StorageMusicProvider musicProvider,
                                      StoragePlayListsProvider playListsProvider,
                                      StorageGenresProvider genresProvider,
                                      GenresDaoWrapper genresDao,
                                      StorageCompositionAnalyzer compositionAnalyzer,
                                      StoragePlaylistAnalyzer playlistAnalyzer,
                                      Scheduler scheduler) {
        this.musicProvider = musicProvider;
        this.playListsProvider = playListsProvider;
        this.genresProvider = genresProvider;
        this.genresDao = genresDao;
        this.compositionAnalyzer = compositionAnalyzer;
        this.playlistAnalyzer = playlistAnalyzer;
        this.scheduler = scheduler;
    }

    @Override
    public void runStorageObserver() {
        runRescanStorage()
                .doOnComplete(this::subscribeOnMediaStoreChanges)
                .subscribe();
    }

    @Override
    public synchronized void rescanStorage() {
        runRescanStorage().subscribe();
    }

    @Override
    public Completable runStorageScanner() {
        return runRescanStorage();
    }

    private void subscribeOnMediaStoreChanges() {
        mediaStoreDisposable.add(musicProvider.getCompositionsObservable()
                .subscribeOn(scheduler)
                .subscribe(compositionAnalyzer::applyCompositionsData));
        mediaStoreDisposable.add(playListsProvider.getPlayListsObservable()
                .subscribeOn(scheduler)
                .subscribe(playlistAnalyzer::applyPlayListData));

        //genre in files and genre in media store are ofter different, we need deep file scanner for them
        //<return genres after deep scan implementation>
//        mediaStoreDisposable.add(genresProvider.getGenresObservable()
//                .subscribeOn(scheduler)
//                .subscribe(this::onStorageGenresReceived));
//        subscribeOnGenresData();
    }

    private Completable runRescanStorage() {
        return Completable.fromAction(() -> {
            compositionAnalyzer.applyCompositionsData(musicProvider.getCompositions());
            playlistAnalyzer.applyPlayListData(playListsProvider.getPlayLists());

            //<return genres after deep scan implementation>
//            applyGenresData(genresProvider.getGenres());
//            List<IdPair> genresIds = genresDao.getGenresIds();
//            for (IdPair genreId: genresIds) {
//                long storageId = genreId.getStorageId();
//                long dbId = genreId.getDbId();
//                applyGenreItemsData(dbId, genresProvider.getGenreItems(storageId));
//            }
        }).subscribeOn(scheduler);
    }

    private void onStorageGenresReceived(Map<String, StorageGenre> newGenres) {
        applyGenresData(newGenres);
        subscribeOnGenresData();
    }

    private void subscribeOnGenresData() {
        List<IdPair> genresIds = genresDao.getGenresIds();
        for (IdPair genreId: genresIds) {
            long storageId = genreId.getStorageId();
            long dbId = genreId.getDbId();
            if (!genreEntriesDisposable.containsKey(dbId)) {
                Disposable disposable = genresProvider.getGenreItemsObservable(storageId)
                        .startWith(genresProvider.getGenreItems(storageId))
                        .subscribeOn(scheduler)
                        .subscribe(entries -> applyGenreItemsData(dbId, entries));
                genreEntriesDisposable.put(dbId, disposable);
            }
        }
    }

    private synchronized void applyGenreItemsData(long genreId,
                                                  LongSparseArray<StorageGenreItem> newGenreItems) {
        if (newGenreItems.isEmpty() || !genresDao.isGenreExists(genreId)) {
            genresDao.deleteGenre(genreId);
            genreEntriesDisposable.remove(genreId);
            return;
        }

        LongSparseArray<StorageGenreItem> currentItems = genresDao.selectAllAsStorageGenreItems(genreId);
        List<StorageGenreItem> addedItems = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processChanges(currentItems,
                newGenreItems,
                (o1, o2) -> false,
                item -> {},
                addedItems::add,
                item -> {});

        if (hasChanges) {
            genresDao.applyChanges(addedItems, genreId);
        }
    }

    private synchronized void applyGenresData(Map<String, StorageGenre> newGenres) {
        Set<String> currentGenres = genresDao.selectAllGenreNames();

        List<StorageGenre> addedGenres = new ArrayList<>();
        boolean hasChanges = AndroidCollectionUtils.processChanges(currentGenres,
                newGenres,
                name -> name,
                StorageGenre::getName,
                (o1, o2) -> false,
                item -> {},
                addedGenres::add,
                (name, item) -> {});

        if (hasChanges) {
            genresDao.applyChanges(addedGenres);
        }
    }
}