package com.github.anrimian.musicplayer.data.storage.files;

import java.io.File;

public class FileManager {

    public static void deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    public static void deleteEmptyDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files == null) {
                return;
            }
            for (File child : files) {
                if (fileOrDirectory.isDirectory()) {
                    deleteEmptyDirectory(child);
                }
            }
            if (files.length == 0) {
                //noinspection ResultOfMethodCallIgnored
                fileOrDirectory.delete();
            }
        }
    }
}
