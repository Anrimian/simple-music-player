package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileKey;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.LocalFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemovedFileMetadata;
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
                eq(asList(metadataToAdd))
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
        RemovedFileMetadata removedFileMetadata = removedFileMetadata("", "file2", new Date(1000));

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

        verify(libraryRepository).updateLocalFilesMetadata(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
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
        FileMetadata metadataToDelete = simpleFileMetadata("", "file2", new Date(1000));
        RemovedFileMetadata removedFileMetadata = removedFileMetadata("", "file2", new Date(1));

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
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
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
                eq(asList(metadataToDelete))
        );
    }

    @Test
    public void testDeleteFromLocal() {
        FileMetadata metadataToDelete = simpleFileMetadata("", "file2");
        RemovedFileMetadata removedFileMetadata = removedFileMetadata("", "file2", new Date(1000));

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
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
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
        FileMetadata metadataToDelete = simpleFileMetadata("", "file2", new Date(1000));
        RemovedFileMetadata removedFileMetadata = removedFileMetadata("", "file2", new Date(1));

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

        verify(libraryRepository).updateLocalFilesMetadata(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
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

        verify(libraryRepository).updateLocalFilesMetadata(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
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

        verify(remoteRepository).updateMetadata(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
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
                eq(asList(newMetadata))
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

        verify(remoteRepository).updateMetadata(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
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
                eq(asList(newMetadata))
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

        verify(libraryRepository).updateLocalFilesMetadata(
                any(),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyList()),
                eq(emptyMap())
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                eq(emptyList()),
                eq(emptyList()),
                eq(asList(oldMetadata)),
                eq(emptyList())
        );
    }

    //test sync without changes
    //test sync error state
    //test create file from remote
    //test sync with files but without remote metadata
    //test delete file from local but without real file in local
    //test delete file from remote but without real file in remote
    //test sync with outdated local and remote removed items
    //test specific changes without file upload\download
    //test filepath change?

    private FileMetadata simpleFileMetadata(String path, String name) {
        return simpleFileMetadata(path, name, new Date(0));
    }

    private FileMetadata simpleFileMetadata(String path, String name, Date dateAdded) {
        return simpleFileMetadata(path, name, dateAdded, "title");
    }

    private FileMetadata simpleFileMetadata(String path, String name, Date dateAdded, String title) {
        return new FileMetadata(new FileKey(name, path),
                "artist",
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

    private RemovedFileMetadata removedFileMetadata(String path, String name, Date date) {
        return new RemovedFileMetadata(new FileKey(name, path), date);
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