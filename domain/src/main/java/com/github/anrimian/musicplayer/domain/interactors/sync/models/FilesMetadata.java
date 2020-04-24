package com.github.anrimian.musicplayer.domain.interactors.sync.models;

import java.util.Date;

public class FilesMetadata {

    private int version;
    private Date modifyTime;

    public FilesMetadata(int version, Date modifyTime) {
        this.version = version;
        this.modifyTime = modifyTime;
    }

    public int getVersion() {
        return version;
    }

    public Date getModifyTime() {
        return modifyTime;
    }
}
