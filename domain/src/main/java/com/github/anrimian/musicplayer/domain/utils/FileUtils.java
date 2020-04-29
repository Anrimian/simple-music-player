package com.github.anrimian.musicplayer.domain.utils;

import java.io.File;

import static com.github.anrimian.musicplayer.domain.utils.TextUtils.isEmpty;

public class FileUtils {

    public static String getParentDirPath(String path) {
        int lastSeparatorIndex = path.lastIndexOf("/");
        if (lastSeparatorIndex != -1) {
            return path.substring(0, lastSeparatorIndex);
        }
        return path;
    }

    public static String getFileName(String path) {
        String displayPath = path;
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            displayPath = path.substring(++lastSlashIndex);
        }
        return displayPath;
    }

    public static String formatFileName(String filePath) {
        return formatFileName(filePath, false);
    }

    public static String formatFileName(String filePath, boolean showExtension) {
        if (isEmpty(filePath)) {
            return "";
        }
        String fileName = getFileName(filePath);
        if (!showExtension) {
            int cropIndex = fileName.lastIndexOf('.');
            if (cropIndex != -1) {
                return fileName.substring(0, cropIndex);
            }
        }
        return fileName;
    }

    public static String getNewPath(String fullPath, String newFileName) {
        String fileName = FileUtils.formatFileName(fullPath);
        return TextUtils.replaceLast(fullPath, fileName, newFileName);
    }

    public static String getChangedFilePath(String fullPath, String newFileName) {
        String newPath = getNewPath(fullPath, newFileName);
        return getUniqueFilePath(newPath, newFileName);
    }

    public static String getChangedFilePath(String filePath, String oldPath, String newPath) {
        String fileName = FileUtils.formatFileName(filePath);
        String path = filePath.replaceFirst(oldPath, newPath);
        return getUniqueFilePath(path, fileName);
    }

    public static String safeReplacePath(String filePath, String oldPath, String newPath) {
        String fileName = FileUtils.formatFileName(filePath);
        String path = filePath.replace(oldPath, newPath);
        return getUniqueFilePath(path, fileName);
    }

    private static String getUniqueFilePath(String filePath, String fileName) {
        File file = new File(filePath);
        int filesCount = 0;
        String newFileName;
        if (file.exists()) {
            while (file.exists()) {
                filesCount++;

                newFileName = fileName + "(" + filesCount + ")";//hmm, check on new name like name(1)?
                String newPath = filePath.replace(fileName, newFileName);

                file = new File(newPath);
            }
        }
        return file.getPath();
    }
}
