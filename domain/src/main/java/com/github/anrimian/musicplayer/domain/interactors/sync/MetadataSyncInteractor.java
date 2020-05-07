package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileKey;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.LocalFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemovedFileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RunningSyncState;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.exceptions.TooHighRemoteRepositoryVersion;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteStoragesRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.SyncSettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryState.DISABLED_VERSION_TOO_HIGH;

public class MetadataSyncInteractor {

    private final int metadataVersion;
    private final SyncSettingsRepository syncSettingsRepository;
    private final RemoteStoragesRepository remoteStoragesRepository;
    private final LibraryRepository libraryRepository;
    private final Scheduler scheduler;//single thread pool scheduler

    private final BehaviorSubject<RunningSyncState> syncStateSubject = BehaviorSubject.create();
    private final BehaviorSubject<RemoteRepositoryType> currentSyncingRepositorySubject = BehaviorSubject.create();

    public MetadataSyncInteractor(int metadataVersion,
                                  SyncSettingsRepository syncSettingsRepository,
                                  RemoteStoragesRepository remoteStoragesRepository,
                                  LibraryRepository libraryRepository,
                                  Scheduler scheduler) {
        this.metadataVersion = metadataVersion;
        this.syncSettingsRepository = syncSettingsRepository;
        this.remoteStoragesRepository = remoteStoragesRepository;
        this.libraryRepository = libraryRepository;
        this.scheduler = scheduler;
    }

    public void runSync() {
        //filter possible often calls?
        Observable.fromIterable(syncSettingsRepository.getEnabledRemoteRepositories())
                .flatMapCompletable(this::runSyncFor)
                .doOnError(this::onSyncError)
                .onErrorComplete()
                .doFinally(() -> syncStateSubject.onNext(new RunningSyncState.Idle()))
                .subscribeOn(scheduler)
                .subscribe();
    }

    public Observable<RunningSyncState> getRunningSyncStateObservable() {
        return syncStateSubject;
    }

    public Observable<RemoteRepositoryType> getCurrentSyncingRepositoryObservable() {
        return currentSyncingRepositorySubject;
    }

    private void onSyncError(Throwable throwable) {
        syncStateSubject.onNext(new RunningSyncState.Error(throwable));
    }

    private Completable runSyncFor(RemoteRepositoryType repositoryType) {
        return Completable.fromAction(() -> {
            RemoteRepository remoteRepository = remoteStoragesRepository.getRemoteRepository(repositoryType);
            runSyncForRepository(remoteRepository, repositoryType);
        });
    }

    //on error stop sync
    private void runSyncForRepository(RemoteRepository remoteRepository, RemoteRepositoryType repositoryType) {
        //get metadata from remote
        syncStateSubject.onNext(new RunningSyncState.GetRemoteMetadata(repositoryType));
        RemoteFilesMetadata remoteMetadata = remoteRepository.getMetadata();

        //check metadata version
        if (remoteMetadata.getVersion() > metadataVersion) {
            remoteStoragesRepository.setEnabledState(repositoryType, DISABLED_VERSION_TOO_HIGH);
            throw new TooHighRemoteRepositoryVersion();
        }

        Map<FileKey, FileMetadata> remoteFiles = remoteMetadata.getFiles();
        Map<FileKey, RemovedFileMetadata> remoteRemovedFiles = remoteMetadata.getRemovedFiles();

        //get remote real file list
        syncStateSubject.onNext(new RunningSyncState.GetRemoteFileTable(repositoryType));
        Set<FileKey> remoteRealFiles = remoteRepository.getRealFileList();

        //get metadata from local
        syncStateSubject.onNext(new RunningSyncState.CollectLocalFileInfo());
        LocalFilesMetadata localFilesMetadata = libraryRepository.getLocalFilesMetadata();
        Map<FileKey, FileMetadata> localFiles = localFilesMetadata.getLocalFiles();
        Set<FileKey> localRealFiles = localFilesMetadata.getRealFilesList();
        Map<FileKey, RemovedFileMetadata> localRemovedFiles = localFilesMetadata.getRemovedFiles();

        syncStateSubject.onNext(new RunningSyncState.CalculateChanges());

        //calculate changes
        //file task lists
        List<FileMetadata> localFilesToDelete = new LinkedList<>();
        List<FileMetadata> remoteFilesToDelete = new LinkedList<>();
        List<FileMetadata> localFilesToUpload = new LinkedList<>();
        List<FileMetadata> remoteFilesToDownload = new LinkedList<>();

        //elements result lists
        List<FileMetadata> localItemsToAdd = new LinkedList<>();
        List<FileMetadata> localItemToDelete = new LinkedList<>();
        List<Change<FileMetadata>> localChangedItems = new LinkedList<>();

        List<FileMetadata> remoteItemsToAdd = new LinkedList<>();
        List<FileMetadata> remoteItemToDelete = new LinkedList<>();
        List<Change<FileMetadata>> remoteChangedItems = new LinkedList<>();

        StructMerger.mergeFilesMap(localFiles,
                remoteFiles,
                localRemovedFiles,
                remoteRemovedFiles,
                localRealFiles,
                remoteRealFiles,
                this::hasItemChanges,
                this::isLocalItemNewerThanRemote,
                this::isRemovedItemActual,
                this::createMetadataForFile,
                localFilesToDelete::add,
                remoteFilesToDelete::add,
                localFilesToUpload::add,
                remoteFilesToDownload::add,
                (key, item) -> localItemsToAdd.add(item),
                (key, item) -> localItemToDelete.add(item),
                (key, oldLocalItem, newRemoteItem) -> localChangedItems.add(new Change<>(oldLocalItem, newRemoteItem)),
                (key, item) -> remoteItemsToAdd.add(item),
                (key, item) -> remoteItemToDelete.add(item),
                (key, oldLocalItem, newRemoteItem) -> remoteChangedItems.add(new Change<>(oldLocalItem, newRemoteItem)));

        //merge removed items
        List<RemovedFileMetadata> localRemovedItemsToAdd = new LinkedList<>();
        List<RemovedFileMetadata> localRemovedItemToDelete = new LinkedList<>();
        List<RemovedFileMetadata> remoteRemovedItemsToAdd = new LinkedList<>();
        List<RemovedFileMetadata> remoteRemovedItemToDelete = new LinkedList<>();
        StructMerger.mergeMaps(localRemovedFiles,
                remoteRemovedFiles,
                this::isRemovedItemActual,
                (key, item) -> localRemovedItemsToAdd.add(item),
                (key, item) -> localRemovedItemToDelete.add(item),
                (key, item) -> remoteRemovedItemsToAdd.add(item),
                (key, item) -> remoteRemovedItemToDelete.add(item));

        //merge playlists

        //save remote metadata(if we can't save cause 'not enough place' or smth - error and disable repository
        //save local metadata
        //schedule file tasks(+move change(+ move command list))
    }

    //async creation?
    private FileMetadata createMetadataForFile(FileKey key) {
        return null;
    }

    private boolean isRemovedItemActual(FileMetadata metadata, RemovedFileMetadata removedFile) {
        return true;
    }

    private boolean isRemovedItemActual(RemovedFileMetadata removedFile) {
        return true;//return time compare result
    }

    private boolean isLocalItemNewerThanRemote(FileMetadata local, FileMetadata remote) {
        return false;
    }

    private boolean hasItemChanges(FileMetadata first, FileMetadata decond) {
        return false;
    }

}
