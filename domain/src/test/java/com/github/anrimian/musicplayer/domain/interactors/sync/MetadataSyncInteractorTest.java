package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileKey;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.LocalFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteStoragesRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.SyncSettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;

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
                any(),
                any(),
                any(),
                any()
        );

        verify(fileSyncInteractor).scheduleFileTasks(eq(remoteRepositoryType1),
                any(),
                any(),
                any(),
                eq(asList(metadataToAdd))
        );
    }

    //test create file from remote
    //test add from local
    //test delete from remote
    //test irrelevant delete from remote
    //test delete from local
    //test irrelevant delete from local
    //test change to remote
    //test irrelevant change to remote
    //test change to local
    //test irrelevant change to local
    //test sync without changes
    //test sync error state

    private FileMetadata simpleFileMetadata(String path, String name) {
        return new FileMetadata(new FileKey(name, path),
                "artist",
                "title",
                "album",
                "album-artist",
                new String[]{ "genre" },
                100,
                100,
                new Date(),
                new Date());
    }

    private void makeRemoteRepositoryReturnFiles(RemoteRepository repository, FileMetadata... metadataList) {
        when(repository.getMetadata()).thenReturn(remoteFilesMetadata(metadataList));
        when(repository.getRealFileList()).thenReturn(metadataKeySet(metadataList));
    }

    private RemoteFilesMetadata remoteFilesMetadata(FileMetadata... metadataList) {
        return new RemoteFilesMetadata(1, new Date(), metadataMap(metadataList), Collections.emptyMap());
    }

    private LocalFilesMetadata localFilesMetadata(FileMetadata... metadataList) {
        return new LocalFilesMetadata(metadataMap(metadataList), metadataKeySet(metadataList), Collections.emptyMap());
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
        return new RemoteFilesMetadata(version, new Date(), Collections.emptyMap(), Collections.emptyMap());
    }
}