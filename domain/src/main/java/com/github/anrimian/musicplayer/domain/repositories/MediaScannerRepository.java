package com.github.anrimian.musicplayer.domain.repositories;

import io.reactivex.Completable;

public interface MediaScannerRepository {
    void runStorageObserver();

    void rescanStorage();

    Completable runStorageScanner();
}
