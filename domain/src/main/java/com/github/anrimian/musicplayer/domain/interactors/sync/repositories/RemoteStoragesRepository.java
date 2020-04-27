package com.github.anrimian.musicplayer.domain.interactors.sync.repositories;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryState;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;

public interface RemoteStoragesRepository {

    RemoteRepository getRemoteRepository(RemoteRepositoryType type);

    void setEnabledState(RemoteRepositoryType type, RemoteRepositoryState state);

}
