package com.github.anrimian.musicplayer.domain.interactors.sync.models;

public class FileMetadata {

    private final String name;
    private final String path;

    public FileMetadata(String name, String path) {
        this.name = name;
        this.path = path;
    }
}
