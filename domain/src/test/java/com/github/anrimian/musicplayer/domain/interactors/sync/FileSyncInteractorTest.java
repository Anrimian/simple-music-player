package com.github.anrimian.musicplayer.domain.interactors.sync;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.FileTasksRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteRepository;
import com.github.anrimian.musicplayer.domain.interactors.sync.repositories.RemoteStoragesRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FileSyncInteractorTest {

    private final FileTasksRepository fileTasksRepository = mock(FileTasksRepository.class);
    private final RemoteStoragesRepository remoteStoragesRepository = mock(RemoteStoragesRepository.class);
    private final LibraryRepository libraryRepository = mock(LibraryRepository.class);
    private final Scheduler scheduler = Schedulers.trampoline();

    private RemoteRepositoryType remoteRepositoryType1 = new RemoteRepositoryType("remote repo type 1");
    private RemoteRepository remoteRepository1 = mock(RemoteRepository.class);

    private FileSyncInteractor fileSyncInteractor;

    private TestObserver<Boolean> syncStateObserver;

    @Before
    public void setUp() {
        fileSyncInteractor = new FileSyncInteractor(fileTasksRepository,
                remoteStoragesRepository,
                libraryRepository,
                scheduler);

        syncStateObserver = fileSyncInteractor.getFileSyncStateObservable().test();

        when(fileTasksRepository.getRepositoriesToSync()).thenReturn(Single.just(asList(remoteRepositoryType1)));

        when(remoteStoragesRepository.getRemoteRepository(remoteRepositoryType1)).thenReturn(remoteRepository1);

        when(fileTasksRepository.getNextRemoteFileToDelete(any())).thenReturn(Maybe.empty());
        when(fileTasksRepository.getNextLocalFileToUpload(any())).thenReturn(Maybe.empty());
        when(fileTasksRepository.getNextLocalFileToDelete()).thenReturn(Maybe.empty());
        when(fileTasksRepository.getNextRemoteFileToDownload(any())).thenReturn(Maybe.empty());

        when(fileTasksRepository.removeRemoteFileToDelete(any(), any())).thenReturn(Completable.complete());
        when(fileTasksRepository.removeLocalFileToUpload(any(), any())).thenReturn(Completable.complete());
        when(fileTasksRepository.removeLocalFileToDelete(any())).thenReturn(Completable.complete());
        when(fileTasksRepository.removeRemoteFileToDownload(any(), any())).thenReturn(Completable.complete());
    }

    //run sync with multiple files
    //run sync with error

    @Test
    public void testEmptySync() {
        fileSyncInteractor.requestFileSync();

        verify(remoteRepository1, never()).deleteRemoteFile(any());
        verify(remoteRepository1, never()).uploadFile(any());
        verify(libraryRepository, never()).deleteFile(any());
        verify(remoteRepository1, never()).downloadFile(any());

        verify(fileTasksRepository, never()).removeRemoteFileToDelete(any(), any());
        verify(fileTasksRepository, never()).removeLocalFileToUpload(any(), any());
        verify(fileTasksRepository, never()).removeLocalFileToDelete(any());
        verify(fileTasksRepository, never()).removeRemoteFileToDownload(any(), any());

        syncStateObserver.assertValueAt(1, true);
        syncStateObserver.assertValueAt(2, false);
    }

    static int i = 0;

    @Test
    public void testRx() {
        Maybe<Integer> maybe = Maybe.create(emitter -> {
            if (i > 10) {
                emitter.onComplete();
            } else {
                emitter.onSuccess(i++);
            }
        });

        RxUtils.repeatUntilComplete(maybe)
                .doOnNext(System.out::println)
                .doOnComplete(() -> System.out.println("on complete"))
                .flatMapCompletable(o -> Completable.complete().doOnComplete(() -> System.out.println("completable completes")))
                .subscribe();
    }
}