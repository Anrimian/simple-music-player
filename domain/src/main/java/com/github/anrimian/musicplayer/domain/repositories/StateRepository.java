package com.github.anrimian.musicplayer.domain.repositories;

public interface StateRepository {

    String getRootFolderPath();

    void setRootFolderPath(String path);
}
