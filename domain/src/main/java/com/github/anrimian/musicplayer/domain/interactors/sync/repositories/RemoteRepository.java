package com.github.anrimian.musicplayer.domain.interactors.sync.repositories;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;

public interface RemoteRepository {

    RemoteFilesMetadata getMetadata();

}
