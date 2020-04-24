package com.github.anrimian.musicplayer.domain.interactors.sync.repositories;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteRepositoryType;

import java.util.List;

public interface SyncSettingsRepository {

    List<RemoteRepositoryType> getEnabledRemoteRepositories();

}
