package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteStoragesRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.SyncSettingsRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

import static com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryState.DISABLED_VERSION_TOO_HIGH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetadataSyncInteractorTest {

    private SyncSettingsRepository syncSettingsRepository = mock(SyncSettingsRepository.class);
    private RemoteStoragesRepository remoteStoragesRepository = mock(RemoteStoragesRepository.class);
    private LibraryRepository libraryRepository = mock(LibraryRepository.class);
    private Scheduler scheduler = Schedulers.trampoline();

    private MetadataSyncInteractor syncInteractor = new MetadataSyncInteractor(
            1,
            syncSettingsRepository,
            remoteStoragesRepository,
            libraryRepository,
            scheduler);


    private RemoteRepositoryType remoteRepositoryType1 = new RemoteRepositoryType("1");
    private RemoteRepositoryType remoteRepositoryType2 = new RemoteRepositoryType("2");

    private RemoteRepository remoteRepository1 = mock(RemoteRepository.class);
    private RemoteRepository remoteRepository2 = mock(RemoteRepository.class);

    @Before
    public void setUp() {
        when(syncSettingsRepository.getEnabledRemoteRepositories())
                .thenReturn(ListUtils.asList(remoteRepositoryType1, remoteRepositoryType2));

        when(remoteStoragesRepository.getRemoteRepository(remoteRepositoryType1))
                .thenReturn(remoteRepository1);

        when(remoteStoragesRepository.getRemoteRepository(remoteRepositoryType2))
                .thenReturn(remoteRepository2);
    }

    @Test
    public void testRemoteVersionIsTooHigh() {
        when(remoteRepository1.getMetadata()).thenReturn(metadata(2));
        when(remoteRepository2.getMetadata()).thenReturn(metadata(1));

        syncInteractor.runSync();

        verify(remoteStoragesRepository).setEnabledState(remoteRepositoryType1, DISABLED_VERSION_TOO_HIGH);
        verify(remoteStoragesRepository, never()).setEnabledState(remoteRepositoryType2, DISABLED_VERSION_TOO_HIGH);
    }

    private RemoteFilesMetadata metadata(int version) {
        return new RemoteFilesMetadata(version, new Date(), Collections.emptyMap(), Collections.emptySet());
    }
}