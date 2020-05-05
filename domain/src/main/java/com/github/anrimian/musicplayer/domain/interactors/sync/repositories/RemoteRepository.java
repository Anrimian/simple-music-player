package com.github.anrimian.musicplayer.domain.interactors.sync.repositories;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileKey;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;

import java.util.Set;

public interface RemoteRepository {

    RemoteFilesMetadata getMetadata();

    Set<FileKey> getRealFileList();
}
