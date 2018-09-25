package com.github.anrimian.musicplayer.data.storage.files;

import java.io.File;

public class FileManager {

    public void deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }


    public void deleteEmptyDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                if (fileOrDirectory.isDirectory()) {
                    deleteEmptyDirectory(child);
                }
            }
            if (fileOrDirectory.listFiles().length == 0) {
                //noinspection ResultOfMethodCallIgnored
                fileOrDirectory.delete();
            }
        }
    }
}