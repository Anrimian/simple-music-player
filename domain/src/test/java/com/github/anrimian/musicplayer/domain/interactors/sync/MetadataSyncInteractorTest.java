package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.DownloadFileTask;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileKey;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.LocalFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemovedFileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RunningSyncState;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteStoragesRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.SyncSettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.utils.changes.Change;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.reactivex.Scheduler;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

import static com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryState.DISABLED_VERSION_TOO_HIGH;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetadataSyncInteractorTest {

    private SyncSettingsRepository syncSettingsRepository = mock(SyncSettingsRepository.class);
    private RemoteStoragesRepository remoteStoragesRepository = mock(RemoteStoragesRepository.class);
    private LibraryRepository libraryRepository = mock(LibraryRepository.class);
    private FileSyncInteractor fileSyncInteractor = mock(FileSyncInteractor.class);
    private Scheduler scheduler = Schedulers.trampoline();

    private MetadataSyncInteractor syncInteractor = new MetadataSyncInteractor(
            1,
            0,
            syncSettingsRepository,
            remoteStoragesRepository,
            libraryRepository,
            fileSyncInteractor,
            scheduler);


    private RemoteRepositoryType remoteRepositoryType1 = new RemoteRepositoryType("1");
    private RemoteRepository remoteRepository = mock(RemoteRepository.class);

    @Before
    public void setUp() {
        when(syncSettingsRepository.getEnabledRemoteRepositories()).thenReturn(asList(remoteRepositoryType1));

        when(remoteStoragesRepository.getRemoteRepository(remoteRepositoryType1)).thenReturn(remoteRepository);
    }

    @Test
    public void testRemoteVersionIsTooHigh() {
        RemoteRepositoryType remoteRepositoryType2 = new RemoteRepositoryType("2");
        RemoteRepository remoteRepository2 = mock(RemoteRepository.class);

        when(syncSettingsRepository.getEnabledRemoteRepositories())
                .thenReturn(asList(remoteRepositoryType1, remoteRepositoryType2));

        when(remoteStoragesRepository.getRemoteRepository(remoteRepositoryType2))
                .thenReturn(remoteRepository2);

        when(remoteRepository.getMetadata()).thenReturn(versionMetadata(2));
        when(remoteRepository2.getMetadata()).thenReturn(versionMetadata(1));

        syncInteractor.runSync();

        verify(remoteStoragesRepository).setEnabledState(remoteRepositoryType1, DISABLED_VERSION_TOO_HIGH);
        verify(remoteStoragesRepository, never()).setEnabledState(remoteRepositoryType2, DISABLED_VERSION_TOO_HIGH);

        verify(libraryRepository, never()).getLocalFilesMetadata();
    }

    @Test
    public void testAddFromRemote() {
        FileMetadata metadataToAdd = simpleFileMetadata("", "file2");

        makeRemoteRepositoryReturnFiles(remoteRepository,
                simpleFileMetadata("", "file1"),
                metadataToAdd
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                simpleFileMetadata("", "file1")
        ));

        syncInteractor.runSync();

        verify(libraryRepository).updateLocalFilesMetadata(
                any(),
                eq(asList(metadataToAdd)),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new DownloadFileTask(metadataToAdd)))
        );
    }

    @Test
    public void testAddFromLocal() {
        FileMetadata metadataToAdd = simpleFileMetadata("", "file2");

        makeRemoteRepositoryReturnFiles(remoteRepository,
                simpleFileMetadata("", "file1")
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                simpleFileMetadata("", "file1"),
                metadataToAdd
        ));

        syncInteractor.runSync();

        verify(remoteRepository).updateMetadata(
                any(),
                eq(asList(metadataToAdd)),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(metadataToAdd)),
                eq(emptyList())
        );
    }

    @Test
    public void testDeleteFromRemote() {
        FileMetadata metadataToDelete = simpleFileMetadata("", "file2");
        RemovedFileMetadata removedFileMetadata = removedFileMetadata("", "file2", 1000);

        makeRemoteRepositoryReturnFiles(remoteRepository,
                simpleFileMetadata("", "file1"),
                metadataToDelete
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                removedMetadataMap(removedFileMetadata),
                simpleFileMetadata("", "file1")
        ));

        syncInteractor.runSync();

        verify(remoteRepository).updateMetadata(
                any(),
                eq(emptyList()),
                eq(asList(metadataToDelete)),
                eq(emptyList()),
                eq(asList(removedFileMetadata)),
                eq(emptyMap())
        );

        verify(libraryRepository, never()).updateLocalFilesMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                eq(emptyList()),
                eq(asList(metadataToDelete)),
                eq(emptyList()),
                eq(emptyList())
        );
    }

    @Test
    public void testIrrelevantDeleteFromRemote() {
        FileMetadata metadataToDelete = simpleFileMetadata("", "file2", 10000);
        RemovedFileMetadata removedFileMetadata = removedFileMetadata("", "file2", 5000);

        makeRemoteRepositoryReturnFiles(remoteRepository,
                simpleFileMetadata("", "file1"),
                metadataToDelete
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                removedMetadataMap(removedFileMetadata),
                simpleFileMetadata("", "file1")
        ));

        syncInteractor.runSync();

        verify(remoteRepository, never()).updateMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(libraryRepository).updateLocalFilesMetadata(
                any(),
                eq(asList(metadataToDelete)),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(removedMetadataMap(removedFileMetadata))
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new DownloadFileTask(metadataToDelete)))
        );
    }

    @Test
    public void testDeleteFromLocal() {
        FileMetadata metadataToDelete = simpleFileMetadata("", "file2");
        RemovedFileMetadata removedFileMetadata = removedFileMetadata("", "file2", 1000);

        makeRemoteRepositoryReturnFiles(remoteRepository,
                removedMetadataMap(removedFileMetadata),
                simpleFileMetadata("", "file1")
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                simpleFileMetadata("", "file1"),
                metadataToDelete
        ));

        syncInteractor.runSync();

        verify(remoteRepository, never()).updateMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(libraryRepository).updateLocalFilesMetadata(
                any(),
                eq(emptyList()),
                eq(asList(metadataToDelete)),
                eq(emptyList()),
                eq(asList(removedFileMetadata)),
                eq(emptyMap())
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                eq(asList(metadataToDelete)),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList())
        );
    }

    @Test
    public void testIrrelevantDeleteFromLocal() {
        FileMetadata metadataToDelete = simpleFileMetadata("", "file2", 1000);
        RemovedFileMetadata removedFileMetadata = removedFileMetadata("", "file2", 1);

        makeRemoteRepositoryReturnFiles(remoteRepository,
                removedMetadataMap(removedFileMetadata),
                simpleFileMetadata("", "file1")
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                simpleFileMetadata("", "file1"),
                metadataToDelete
        ));

        syncInteractor.runSync();

        verify(remoteRepository).updateMetadata(
                any(),
                eq(asList(metadataToDelete)),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(removedMetadataMap(removedFileMetadata))
        );

        verify(libraryRepository, never()).updateLocalFilesMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(metadataToDelete)),
                eq(emptyList())
        );
    }

    @Test
    public void testChangeFromLocalToRemote() {
        FileMetadata newMetadata = simpleFileMetadata("", "file2", new Date(1000), "title2");
        FileMetadata oldMetadata = simpleFileMetadata("", "file2", new Date(500), "title1");

        makeRemoteRepositoryReturnFiles(remoteRepository,
                simpleFileMetadata("", "file1"),
                oldMetadata
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                simpleFileMetadata("", "file1"),
                newMetadata
        ));

        syncInteractor.runSync();

        verify(remoteRepository).updateMetadata(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(oldMetadata, newMetadata))),
                eq(emptyList()),
                eq(emptyMap())
        );

        verify(libraryRepository, never()).updateLocalFilesMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(newMetadata)),
                eq(emptyList())
        );
    }

    @Test
    public void testIrrelevantChangeFromLocalToRemote() {
        FileMetadata newMetadata = simpleFileMetadata("", "file2", new Date(1000), "title2");
        FileMetadata oldMetadata = simpleFileMetadata("", "file2", new Date(1500), "title1");

        makeRemoteRepositoryReturnFiles(remoteRepository,
                simpleFileMetadata("", "file1"),
                oldMetadata
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                simpleFileMetadata("", "file1"),
                newMetadata
        ));

        syncInteractor.runSync();

        verify(remoteRepository, never()).updateMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(libraryRepository).updateLocalFilesMetadata(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(newMetadata, oldMetadata))),
                eq(emptyList()),
                eq(emptyMap())
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new DownloadFileTask(newMetadata)))
        );
    }

    @Test
    public void testChangeFromRemoteToLocal() {
        FileMetadata oldMetadata = simpleFileMetadata("", "file2", new Date(1000), "title2");
        FileMetadata newMetadata = simpleFileMetadata("", "file2", new Date(1500), "title1");

        makeRemoteRepositoryReturnFiles(remoteRepository,
                simpleFileMetadata("", "file1"),
                newMetadata
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                simpleFileMetadata("", "file1"),
                oldMetadata
        ));

        syncInteractor.runSync();

        verify(remoteRepository, never()).updateMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(libraryRepository).updateLocalFilesMetadata(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(oldMetadata, newMetadata))),
                eq(emptyList()),
                eq(emptyMap())
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new DownloadFileTask(newMetadata)))
        );
    }

    @Test
    public void testIrrelevantChangeFromRemoteToLocal() {
        FileMetadata oldMetadata = simpleFileMetadata("", "file2", new Date(1000), "title2");
        FileMetadata newMetadata = simpleFileMetadata("", "file2", new Date(500), "title1");

        makeRemoteRepositoryReturnFiles(remoteRepository,
                simpleFileMetadata("", "file1"),
                newMetadata
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                simpleFileMetadata("", "file1"),
                oldMetadata
        ));

        syncInteractor.runSync();

        verify(remoteRepository).updateMetadata(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new Change<>(newMetadata, oldMetadata))),
                eq(emptyList()),
                eq(emptyMap())
        );

        verify(libraryRepository, never()).updateLocalFilesMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(oldMetadata)),
                eq(emptyList())
        );
    }

    @Test
    public void testSyncWithoutChanges() {
        makeRemoteRepositoryReturnFiles(remoteRepository,
                simpleFileMetadata("", "file1"),
                simpleFileMetadata("", "file2")
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                simpleFileMetadata("", "file1"),
                simpleFileMetadata("", "file2")
        ));

        syncInteractor.runSync();

        verify(remoteRepository, never()).updateMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(libraryRepository, never()).updateLocalFilesMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(fileSyncInteractor, never()).scheduleFileTasks(eq(remoteRepositoryType1),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    public void testSyncError() {
        when(remoteRepository.getMetadata()).thenThrow(new RuntimeException("test error"));

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                simpleFileMetadata("", "file1"),
                simpleFileMetadata("", "file2")
        ));

        TestObserver<RunningSyncState> observer = syncInteractor.getRunningSyncStateObservable()
                .test();

        syncInteractor.runSync();

        verify(remoteRepository, never()).updateMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(libraryRepository, never()).updateLocalFilesMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(fileSyncInteractor, never()).scheduleFileTasks(eq(remoteRepositoryType1),
                any(),
                any(),
                any(),
                any()
        );

        observer.assertValueAt(0, value -> value instanceof RunningSyncState.GetRemoteMetadata);
        observer.assertValueAt(1, value -> value instanceof RunningSyncState.Error);
    }

    @Test
    public void testCreateFileFromRemote() {
        FileMetadata existsMetadata = simpleFileMetadata("", "file1");
        FileMetadata addedFileMetadata = simpleFileMetadata("", "file2");

        when(remoteRepository.getMetadata()).thenReturn(remoteFilesMetadata(
                emptyMap(),
                existsMetadata
        ));
        when(remoteRepository.getRealFileList()).thenReturn(metadataKeySet(
                existsMetadata,
                addedFileMetadata
        ));

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                existsMetadata
        ));

        syncInteractor.runSync();

        verify(remoteRepository).updateMetadata(
                any(),
                eq(asList(addedFileMetadata)),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
        );

        verify(libraryRepository).updateLocalFilesMetadata(
                any(),
                eq(asList(addedFileMetadata)),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(new DownloadFileTask(addedFileMetadata, true)))
        );
    }

    @Test
    public void testSyncWithDeletedRemoteMetadata() {
        FileMetadata file1 = simpleFileMetadata("", "file1");
        FileMetadata file2 = simpleFileMetadata("", "file2");

        when(remoteRepository.getMetadata()).thenReturn(remoteFilesMetadata(
                emptyMap()
        ));
        when(remoteRepository.getRealFileList()).thenReturn(metadataKeySet(
                file1,
                file2
        ));

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                file1,
                file2
        ));

        syncInteractor.runSync();

        verify(remoteRepository).updateMetadata(
                any(),
                eq(asList(file2, file1)),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
        );

        verify(libraryRepository, never()).updateLocalFilesMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(fileSyncInteractor, never()).scheduleFileTasks(eq(remoteRepositoryType1),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    public void testDeleteFromLocalWithoutRealFileOnLocal() {
        FileMetadata file1 = simpleFileMetadata("", "file1");
        FileMetadata metadataToDelete = simpleFileMetadata("", "file2");
        RemovedFileMetadata removedFileMetadata = removedFileMetadata("", "file2", 1000);

        makeRemoteRepositoryReturnFiles(remoteRepository,
                removedMetadataMap(removedFileMetadata),
                file1
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(new LocalFilesMetadata(
                metadataMap(file1, metadataToDelete),
                metadataKeySet(file1),
                Collections.emptyMap())
        );

        syncInteractor.runSync();

        verify(remoteRepository, never()).updateMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(libraryRepository).updateLocalFilesMetadata(
                any(),
                eq(emptyList()),
                eq(asList(metadataToDelete)),
                eq(emptyList()),
                eq(asList(removedFileMetadata)),
                eq(emptyMap())
        );

        verify(fileSyncInteractor, never()).scheduleFileTasks(eq(remoteRepositoryType1),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    public void testDeleteFromRemoteWithoutRealFileOnRemote() {
        FileMetadata file1 = simpleFileMetadata("", "file1");
        FileMetadata metadataToDelete = simpleFileMetadata("", "file2");
        RemovedFileMetadata removedFileMetadata = removedFileMetadata("", "file2", 1000);

        when(remoteRepository.getMetadata()).thenReturn(remoteFilesMetadata(
                emptyMap(),
                file1,
                metadataToDelete));
        when(remoteRepository.getRealFileList()).thenReturn(metadataKeySet(file1));

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                removedMetadataMap(removedFileMetadata),
                file1
        ));

        syncInteractor.runSync();

        verify(remoteRepository).updateMetadata(
                any(),
                eq(emptyList()),
                eq(asList(metadataToDelete)),
                eq(emptyList()),
                eq(asList(removedFileMetadata)),
                eq(emptyMap())
        );

        verify(libraryRepository, never()).updateLocalFilesMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(fileSyncInteractor, never()).scheduleFileTasks(eq(remoteRepositoryType1),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    public void testRemoveOutdatedRemovedFiles() {
        syncInteractor = new MetadataSyncInteractor(
                1,
                5000,
                syncSettingsRepository,
                remoteStoragesRepository,
                libraryRepository,
                fileSyncInteractor,
                scheduler);

        FileMetadata metadata = simpleFileMetadata("", "file1");
        RemovedFileMetadata removedLocalFileMetadata = removedFileMetadata("", "file2", -10000);
        RemovedFileMetadata removedRemoteFileMetadata = removedFileMetadata("", "file3", -10000);

        makeRemoteRepositoryReturnFiles(remoteRepository,
                removedMetadataMap(removedRemoteFileMetadata),
                metadata
        );

        when(libraryRepository.getLocalFilesMetadata()).thenReturn(localFilesMetadata(
                removedMetadataMap(removedLocalFileMetadata),
                metadata
        ));

        syncInteractor.runSync();

        verify(remoteRepository).updateMetadata(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(removedMetadataMap(removedRemoteFileMetadata))
        );

        verify(libraryRepository).updateLocalFilesMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                eq(removedMetadataMap(removedLocalFileMetadata))
        );

        verify(fileSyncInteractor, never()).scheduleFileTasks(eq(remoteRepositoryType1),
                any(),
                any(),
                any(),
                any()
        );
    }

    //test specific changes without file upload\download
    //test filepath change?
    //test sync with regular error, like timeout
    //test sync launch rules, often calls?
    //test wait_for_wifi, wait_for_charging state

    private FileMetadata simpleFileMetadata(String path, String name) {
        return simpleFileMetadata(path, name, 0);
    }

    private FileMetadata simpleFileMetadata(String path, String name, long dateAdded) {
        long now = System.currentTimeMillis();
        return simpleFileMetadata(path, name, new Date(now + dateAdded), "title");
    }

    private FileMetadata simpleFileMetadata(String path, String name, Date dateAdded, String title) {
        return new FileMetadata(new FileKey(name, path),
                "artist",
                "filename + " + title,
                title,
                "album",
                "album-artist",
                new String[]{ "genre" },
                100,
                100,
                dateAdded,
                new Date(0));
    }

    private void makeRemoteRepositoryReturnFiles(RemoteRepository repository,
                                                 FileMetadata... metadataList) {
        makeRemoteRepositoryReturnFiles(repository, emptyMap(), metadataList);
    }

    private void makeRemoteRepositoryReturnFiles(RemoteRepository repository,
                                                 Map<FileKey, RemovedFileMetadata> removedItems,
                                                 FileMetadata... metadataList) {
        when(repository.getMetadata()).thenReturn(remoteFilesMetadata(removedItems, metadataList));
        when(repository.getRealFileList()).thenReturn(metadataKeySet(metadataList));
    }

    private RemoteFilesMetadata remoteFilesMetadata(Map<FileKey, RemovedFileMetadata> removedItems,
                                                    FileMetadata... metadataList) {
        return new RemoteFilesMetadata(1, new Date(0), metadataMap(metadataList), removedItems);
    }

    private LocalFilesMetadata localFilesMetadata(Map<FileKey, RemovedFileMetadata> removedItems,
                                                  FileMetadata... metadataList) {
        return new LocalFilesMetadata(metadataMap(metadataList), metadataKeySet(metadataList), removedItems);
    }

    private LocalFilesMetadata localFilesMetadata(FileMetadata... metadataList) {
        return new LocalFilesMetadata(metadataMap(metadataList), metadataKeySet(metadataList), Collections.emptyMap());
    }

    private RemovedFileMetadata removedFileMetadata(String path, String name, long date) {
        long now = System.currentTimeMillis();
        return new RemovedFileMetadata(new FileKey(name, path), new Date(now + date));
    }

    private Map<FileKey, RemovedFileMetadata> removedMetadataMap(RemovedFileMetadata... metadataList) {
        Map<FileKey, RemovedFileMetadata> map = new HashMap<>();
        for (RemovedFileMetadata metadata : metadataList) {
            map.put(metadata.getFileKey(), metadata);
        }
        return map;
    }

    private Map<FileKey, FileMetadata> metadataMap(FileMetadata... metadataList) {
        Map<FileKey, FileMetadata> map = new HashMap<>();
        for (FileMetadata metadata : metadataList) {
            map.put(metadata.getFileKey(), metadata);
        }
        return map;
    }

    private Set<FileKey> metadataKeySet(FileMetadata... metadataList) {
        Set<FileKey> set = new HashSet<>();
        for (FileMetadata metadata : metadataList) {
            set.add(metadata.getFileKey());
        }
        return set;
    }

    private RemoteFilesMetadata versionMetadata(int version) {
        return new RemoteFilesMetadata(version, new Date(0), Collections.emptyMap(), Collections.emptyMap());
    }
}