package com.github.anrimian.musicplayer.domain.interactors.sync.repositories;

import com.github.anrimian.musicplayer.domain.interactors.sync.models.FileKey;
import com.github.anrimian.musicplayer.domain.interactors.sync.models.RemoteFilesMetadata;

import java.util.List;

public interface RemoteRepository {

    RemoteFilesMetadata getMetadata();

    List<FileKey> getRealFileList();
}
