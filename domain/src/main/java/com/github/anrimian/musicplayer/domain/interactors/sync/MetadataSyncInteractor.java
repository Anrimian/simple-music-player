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
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryState.DISABLED_VERSION_TOO_HIGH;

public class MetadataSyncInteractor {

    private final int metadataVersion;
    private final long removedItemKeepMaxTime;
    private final SyncSettingsRepository syncSettingsRepository;
    private final RemoteStoragesRepository remoteStoragesRepository;
    private final LibraryRepository libraryRepository;
    private final FileSyncInteractor fileSyncInteractor;
    private final Scheduler scheduler;//single thread pool scheduler

    private final BehaviorSubject<RunningSyncState> syncStateSubject = BehaviorSubject.create();
    private final BehaviorSubject<RemoteRepositoryType> currentSyncingRepositorySubject = BehaviorSubject.create();

    public MetadataSyncInteractor(int metadataVersion,
                                  long removedItemKeepMaxTime, SyncSettingsRepository syncSettingsRepository,
                                  RemoteStoragesRepository remoteStoragesRepository,
                                  LibraryRepository libraryRepository,
                                  FileSyncInteractor fileSyncInteractor,
                                  Scheduler scheduler) {
        this.metadataVersion = metadataVersion;
        this.removedItemKeepMaxTime = removedItemKeepMaxTime;
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
        return Single.just(new SyncData(repositoryType, remoteStoragesRepository.getRemoteRepository(repositoryType)))
                .doOnSuccess(syncData -> syncStateSubject.onNext(new RunningSyncState.GetRemoteMetadata(syncData.repositoryType)))
                .flatMap(this::getRemoteFilesMetadata)
                .doOnSuccess(syncData -> syncStateSubject.onNext(new RunningSyncState.GetRemoteFileTable(repositoryType)))
                .flatMap(this::getRemoteRealFilesList)
                .doOnSuccess(syncData -> syncStateSubject.onNext(new RunningSyncState.CollectLocalFileInfo()))
                .flatMap(this::getLocalFilesMetadata)
                .doOnSuccess(syncData -> syncStateSubject.onNext(new RunningSyncState.CalculateChanges()))
                .flatMapCompletable(syncData -> Single.fromCallable(() -> calculateChanges(syncData))
                        .flatMapCompletable(dataMergeResult ->
                                saveRemoteMetadata(syncData, dataMergeResult)
                                .andThen(saveLocalMetadata(syncData, dataMergeResult))
                                .andThen(scheduleFileTasks(syncData, dataMergeResult))
                        )
                );
    }

    private Single<SyncData> getRemoteFilesMetadata(SyncData syncData) {
        return syncData.remoteRepository.getMetadata()
                .map(syncData::setRemoteMetadata)
                .doOnSuccess(this::checkRemoteMetadataVersion);
    }

    private void checkRemoteMetadataVersion(SyncData syncData) {
        //check metadata version
        if (syncData.remoteMetadata.getVersion() > metadataVersion) {
            remoteStoragesRepository.setEnabledState(syncData.repositoryType, DISABLED_VERSION_TOO_HIGH);
            throw new TooHighRemoteRepositoryVersion();
        }
    }

    private Single<SyncData> getRemoteRealFilesList(SyncData syncData) {
        return syncData.remoteRepository.getRealFileList()
                .map(syncData::setRemoteRealFiles);
    }

    private Single<SyncData> getLocalFilesMetadata(SyncData syncData) {
        return libraryRepository.getLocalFilesMetadata()
                .map(syncData::setLocalFilesMetadata);
    }

    private DataMergeResult calculateChanges(SyncData syncData) {
        RemoteFilesMetadata remoteMetadata = syncData.remoteMetadata;
        LocalFilesMetadata localFilesMetadata = syncData.localFilesMetadata;

        Map<FileKey, FileMetadata> remoteFiles = remoteMetadata.getFiles();
        Set<FileKey> remoteRealFiles = syncData.remoteRealFiles;
        Map<FileKey, RemovedFileMetadata> remoteRemovedFiles = remoteMetadata.getRemovedFiles();

        Map<FileKey, FileMetadata> localFiles = localFilesMetadata.getLocalFiles();
        Set<FileKey> localRealFiles = localFilesMetadata.getRealFilesList();
        Map<FileKey, RemovedFileMetadata> localRemovedFiles = localFilesMetadata.getRemovedFiles();

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
                remoteRemovedItemToDelete::put);

        return new DataMergeResult(
                localFilesToDelete,
                remoteFilesToDelete,
                localFilesToUpload,
                remoteFilesToDownload,
                localItemsToAdd,
                localItemsToDelete,
                localChangedItems,
                remoteItemsToAdd,
                remoteItemsToDelete,
                remoteChangedItems,
                localRemovedItemToDelete,
                remoteRemovedItemToDelete,
                localRemovedItemsToAdd,
                remoteRemovedItemsToAdd
        );
    }

    private Completable saveRemoteMetadata(SyncData syncData, DataMergeResult dataMergeResult) {
        //save remote metadata(if we can't save cause 'not enough place' or smth - error and disable repository
        if (!dataMergeResult.remoteItemsToAdd.isEmpty()
                || !dataMergeResult.remoteItemsToDelete.isEmpty()
                || !dataMergeResult.remoteChangedItems.isEmpty()
                || !dataMergeResult.remoteRemovedItemsToAdd.isEmpty()
                || !dataMergeResult.remoteRemovedItemToDelete.isEmpty()) {
            syncStateSubject.onNext(new RunningSyncState.SaveRemoteFileMetadata(syncData.repositoryType));

            return syncData.remoteRepository.updateMetadata(syncData.remoteMetadata,
                    dataMergeResult.remoteItemsToAdd,
                    dataMergeResult.remoteItemsToDelete,
                    dataMergeResult.remoteChangedItems,
                    dataMergeResult.remoteRemovedItemsToAdd,
                    dataMergeResult.remoteRemovedItemToDelete);
        }
        return Completable.complete();
    }

    private Completable saveLocalMetadata(SyncData syncData, DataMergeResult dataMergeResult) {
        if (!dataMergeResult.localItemsToAdd.isEmpty()
                || !dataMergeResult.localItemsToDelete.isEmpty()
                || !dataMergeResult.localChangedItems.isEmpty()
                || !dataMergeResult.localRemovedItemsToAdd.isEmpty()
                || !dataMergeResult.localRemovedItemToDelete.isEmpty()) {

            syncStateSubject.onNext(new RunningSyncState.SaveLocalFileTable());

            return libraryRepository.updateLocalFilesMetadata(syncData.localFilesMetadata,
                    dataMergeResult.localItemsToAdd,
                    dataMergeResult.localItemsToDelete,
                    dataMergeResult.localChangedItems,
                    dataMergeResult.localRemovedItemsToAdd,
                    dataMergeResult.localRemovedItemToDelete);
        }
        return Completable.complete();
    }

    private Completable scheduleFileTasks(SyncData syncData, DataMergeResult dataMergeResult) {
        //schedule file tasks(+move change(+ move command list))
        if (!dataMergeResult.localFilesToDelete.isEmpty()
                || !dataMergeResult.remoteFilesToDelete.isEmpty()
                || !dataMergeResult.localFilesToUpload.isEmpty()
                || !dataMergeResult.remoteFilesToDownload.isEmpty()) {

            syncStateSubject.onNext(new RunningSyncState.ScheduleFileTasks());

            return fileSyncInteractor.scheduleFileTasks(syncData.repositoryType,
                    dataMergeResult.localFilesToDelete,
                    dataMergeResult.remoteFilesToDelete,
                    dataMergeResult.localFilesToUpload,
                    dataMergeResult.remoteFilesToDownload);
        }
        return Completable.complete();
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
        return removedFile.getAddDate().getTime() + removedItemKeepMaxTime > System.currentTimeMillis();
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

    private static class SyncData {
        final RemoteRepositoryType repositoryType;
        final RemoteRepository remoteRepository;
        RemoteFilesMetadata remoteMetadata;
        Set<FileKey> remoteRealFiles;
        LocalFilesMetadata localFilesMetadata;

        private SyncData(RemoteRepositoryType repositoryType, RemoteRepository remoteRepository) {
            this.repositoryType = repositoryType;
            this.remoteRepository = remoteRepository;
        }

        private SyncData setRemoteMetadata(RemoteFilesMetadata remoteMetadata) {
            this.remoteMetadata = remoteMetadata;
            return this;
        }

        private SyncData setRemoteRealFiles(Set<FileKey> remoteRealFiles) {
            this.remoteRealFiles = remoteRealFiles;
            return this;
        }

        private SyncData setLocalFilesMetadata(LocalFilesMetadata localFilesMetadata) {
            this.localFilesMetadata = localFilesMetadata;
            return this;
        }
    }

    private static class DataMergeResult {

        final List<FileMetadata> localFilesToDelete;
        final List<FileMetadata> remoteFilesToDelete;
        final List<FileMetadata> localFilesToUpload;
        final List<DownloadFileTask> remoteFilesToDownload;

        final List<FileMetadata> localItemsToAdd;
        final List<FileMetadata> localItemsToDelete;
        final List<Change<FileMetadata>> localChangedItems;

        final List<FileMetadata> remoteItemsToAdd;
        final List<FileMetadata> remoteItemsToDelete;
        final List<Change<FileMetadata>> remoteChangedItems;

        final Map<FileKey, RemovedFileMetadata> localRemovedItemToDelete;
        final Map<FileKey, RemovedFileMetadata> remoteRemovedItemToDelete;
        final List<RemovedFileMetadata> localRemovedItemsToAdd;
        final List<RemovedFileMetadata> remoteRemovedItemsToAdd;

        private DataMergeResult(List<FileMetadata> localFilesToDelete,
                                List<FileMetadata> remoteFilesToDelete,
                                List<FileMetadata> localFilesToUpload,
                                List<DownloadFileTask> remoteFilesToDownload,
                                List<FileMetadata> localItemsToAdd,
                                List<FileMetadata> localItemsToDelete,
                                List<Change<FileMetadata>> localChangedItems,
                                List<FileMetadata> remoteItemsToAdd,
                                List<FileMetadata> remoteItemsToDelete,
                                List<Change<FileMetadata>> remoteChangedItems,
                                Map<FileKey, RemovedFileMetadata> localRemovedItemToDelete,
                                Map<FileKey, RemovedFileMetadata> remoteRemovedItemToDelete,
                                List<RemovedFileMetadata> localRemovedItemsToAdd,
                                List<RemovedFileMetadata> remoteRemovedItemsToAdd) {
            this.localFilesToDelete = localFilesToDelete;
            this.remoteFilesToDelete = remoteFilesToDelete;
            this.localFilesToUpload = localFilesToUpload;
            this.remoteFilesToDownload = remoteFilesToDownload;
            this.localItemsToAdd = localItemsToAdd;
            this.localItemsToDelete = localItemsToDelete;
            this.localChangedItems = localChangedItems;
            this.remoteItemsToAdd = remoteItemsToAdd;
            this.remoteItemsToDelete = remoteItemsToDelete;
            this.remoteChangedItems = remoteChangedItems;
            this.localRemovedItemToDelete = localRemovedItemToDelete;
            this.remoteRemovedItemToDelete = remoteRemovedItemToDelete;
            this.localRemovedItemsToAdd = localRemovedItemsToAdd;
            this.remoteRemovedItemsToAdd = remoteRemovedItemsToAdd;
        }
    }
}
