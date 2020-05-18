package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.DownloadFileTask;
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
import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;
import com.github.anrimian.musicplayer.domain.utils.validation.DateUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
    private final FileSyncInteractor fileSyncInteractor;
    private final Scheduler scheduler;//single thread pool scheduler

    private final BehaviorSubject<RunningSyncState> syncStateSubject = BehaviorSubject.create();
    private final BehaviorSubject<RemoteRepositoryType> currentSyncingRepositorySubject = BehaviorSubject.create();

    public MetadataSyncInteractor(int metadataVersion,
                                  SyncSettingsRepository syncSettingsRepository,
                                  RemoteStoragesRepository remoteStoragesRepository,
                                  LibraryRepository libraryRepository,
                                  FileSyncInteractor fileSyncInteractor,
                                  Scheduler scheduler) {
        this.metadataVersion = metadataVersion;
        this.syncSettingsRepository = syncSettingsRepository;
        this.remoteStoragesRepository = remoteStoragesRepository;
        this.libraryRepository = libraryRepository;
        this.fileSyncInteractor = fileSyncInteractor;
        this.scheduler = scheduler;
    }

    public void runSync() {
        //filter possible often calls?
        Observable.fromIterable(syncSettingsRepository.getEnabledRemoteRepositories())
                .flatMapCompletable(this::runSyncFor)
                .doOnError(this::onSyncError)
                .doOnComplete(() -> syncStateSubject.onNext(new RunningSyncState.Idle()))
                .onErrorComplete()
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
        List<DownloadFileTask> remoteFilesToDownload = new LinkedList<>();

        //elements result lists
        List<FileMetadata> localItemsToAdd = new LinkedList<>();
        List<FileMetadata> localItemsToDelete = new LinkedList<>();
        List<Change<FileMetadata>> localChangedItems = new LinkedList<>();

        List<FileMetadata> remoteItemsToAdd = new LinkedList<>();
        List<FileMetadata> remoteItemsToDelete = new LinkedList<>();
        List<Change<FileMetadata>> remoteChangedItems = new LinkedList<>();

        Map<FileKey, RemovedFileMetadata> localRemovedItemToDelete = new HashMap<>();
        Map<FileKey, RemovedFileMetadata> remoteRemovedItemToDelete = new HashMap<>();

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
                (item, rescanAfterDownload) -> remoteFilesToDownload.add(new DownloadFileTask(item, rescanAfterDownload)),
                (key, item) -> localItemsToAdd.add(item),
                (key, item) -> localItemsToDelete.add(item),
                (key, oldLocalItem, newRemoteItem) -> localChangedItems.add(new Change<>(oldLocalItem, newRemoteItem)),
                (key, item) -> remoteItemsToAdd.add(item),
                (key, item) -> remoteItemsToDelete.add(item),
                (key, oldLocalItem, newRemoteItem) -> remoteChangedItems.add(new Change<>(oldLocalItem, newRemoteItem)),
                localRemovedItemToDelete::put,
                remoteRemovedItemToDelete::put);

        //merge removed items
        List<RemovedFileMetadata> localRemovedItemsToAdd = new LinkedList<>();
        List<RemovedFileMetadata> remoteRemovedItemsToAdd = new LinkedList<>();

        ListUtils.removeMap(localRemovedFiles, localRemovedItemToDelete);
        ListUtils.removeMap(remoteRemovedFiles, remoteRemovedItemToDelete);
        StructMerger.mergeMaps(localRemovedFiles,
                remoteRemovedFiles,
                this::isRemovedItemIsNotTooOld,
                (key, item) -> localRemovedItemsToAdd.add(item),
                localRemovedItemToDelete::put,
                (key, item) -> remoteRemovedItemsToAdd.add(item),
                localRemovedItemToDelete::put);

        //merge playlists

        //save remote metadata(if we can't save cause 'not enough place' or smth - error and disable repository
        if (!remoteItemsToAdd.isEmpty()
                || !remoteItemsToDelete.isEmpty()
                || !remoteChangedItems.isEmpty()
                || !remoteRemovedItemsToAdd.isEmpty()
                || !remoteRemovedItemToDelete.isEmpty()) {
            syncStateSubject.onNext(new RunningSyncState.SaveRemoteFileTable(repositoryType));

            remoteRepository.updateMetadata(remoteMetadata,
                    remoteItemsToAdd,
                    remoteItemsToDelete,
                    remoteChangedItems,
                    remoteRemovedItemsToAdd,
                    remoteRemovedItemToDelete);
        }

        //save local metadata
        if (!localItemsToAdd.isEmpty()
                || !localItemsToDelete.isEmpty()
                || !localChangedItems.isEmpty()
                || !localRemovedItemsToAdd.isEmpty()
                || !localRemovedItemToDelete.isEmpty()) {

            syncStateSubject.onNext(new RunningSyncState.SaveLocalFileTable());

            libraryRepository.updateLocalFilesMetadata(localFilesMetadata,
                    localItemsToAdd,
                    localItemsToDelete,
                    localChangedItems,
                    localRemovedItemsToAdd,
                    localRemovedItemToDelete);
        }

        //schedule file tasks(+move change(+ move command list))
        if (!localFilesToDelete.isEmpty()
                || !remoteFilesToDelete.isEmpty()
                || !localFilesToUpload.isEmpty()
                || !remoteFilesToDownload.isEmpty()) {

            syncStateSubject.onNext(new RunningSyncState.ScheduleFileTasks());

            fileSyncInteractor.scheduleFileTasks(repositoryType,
                    localFilesToDelete,
                    remoteFilesToDelete,
                    localFilesToUpload,
                    remoteFilesToDownload);
        }
    }

    private FileMetadata createMetadataForFile(FileKey key) {
        Date currentTime = new Date();
        return new FileMetadata(
                key,
                null,
                key.getName(),
                null,
                null,
                null,
                new String[0],
                0,
                0,//can we get size?
                currentTime,
                currentTime
        );
    }

    private boolean isRemovedItemActual(FileMetadata metadata, RemovedFileMetadata removedFile) {
        Date deleteDate = removedFile.getAddDate();
        return DateUtils.isAfter(deleteDate, metadata.getDateAdded())
                && DateUtils.isAfter(deleteDate, metadata.getDateModified());
    }

    private boolean isRemovedItemIsNotTooOld(RemovedFileMetadata removedFile) {
        return true;//return time compare result
    }

    private boolean isLocalItemNewerThanRemote(FileMetadata local, FileMetadata remote) {
        return DateUtils.isAfter(local.getDateAdded(), remote.getDateAdded())
                || DateUtils.isAfter(local.getDateModified(), remote.getDateModified());
    }

    private boolean hasItemChanges(FileMetadata first, FileMetadata second) {
        return !Objects.equals(first.getTitle(), second.getTitle())
                || !Objects.equals(first.getArtist(), second.getArtist())
                || !Objects.equals(first.getAlbum(), second.getAlbum())
                || !Objects.equals(first.getAlbumArtist(), second.getAlbumArtist())
                || !Arrays.equals(first.getGenres(), second.getGenres())
                || first.getDuration() != second.getDuration()
                || first.getSize() != second.getSize();
    }

}
