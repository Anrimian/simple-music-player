package com.github.anrimian.musicplayer.domain.interactors.sync.repositories;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FilesMetadata;

public interface RemoteRepository {

    FilesMetadata getMetadata();
}
