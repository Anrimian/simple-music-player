package com.github.anrimian.musicplayer.data.storage.files;

import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.FileExistsException;
import com.github.anrimian.musicplayer.data.storage.providers.music.FilePathComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.mapListNotNull;

public class StorageFilesDataSource {

    private final StorageMusicProvider storageMusicProvider;

    public StorageFilesDataSource(StorageMusicProvider storageMusicProvider) {
        this.storageMusicProvider = storageMusicProvider;
    }

    public String renameCompositionsFolder(List<Composition> compositions,
                                           String oldPath,
                                           String newName,
                                           List<FilePathComposition> updatedCompositions) {
//        Log.d("KEK2", "changeFolderName, oldPath: " + oldPath);
//        Log.d("KEK2", "changeFolderName, newPath: " + newPath);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            String newName = FileUtils.formatFileName(newPath);
//            File oldFile = new File(oldPath);
//            DocumentFile documentFile = DocumentFile.fromFile(oldFile);
//            documentFile.renameTo(newName);
//            storageMusicProvider.scanMedia(oldPath);
//            storageMusicProvider.scanMedia(newPath);
//        } else {

        //seems working for android <10, implement for scoped storage
        String newPath = FileUtils.getChangedFilePath(oldPath, newName);
        for (Composition composition: compositions) {
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                String oldFilePath = getCompositionFilePath(storageId);
                String newFilePath = FileUtils.safeReplacePath(oldFilePath, oldPath, newPath);

                updatedCompositions.add(new FilePathComposition(
                        composition.getId(),
                        composition.getStorageId(),
                        newFilePath)
                );
            }
        }
        renameFile(oldPath, newPath);

        storageMusicProvider.updateCompositionsFilePath(updatedCompositions);
        return FileUtils.getFileName(newPath);
//        }
    }

    public String renameCompositionFile(FullComposition composition, String fileName) {
        Long storageId = composition.getStorageId();
        if (storageId == null) {
            return null;
        }

        String oldPath = getCompositionFilePath(storageId);
        String newPath = FileUtils.getChangedFilePath(oldPath, fileName);
        renameFile(oldPath, newPath);

        storageMusicProvider.updateCompositionFilePath(storageId, newPath);
        storageMusicProvider.updateCompositionFileName(storageId, fileName);

        return newPath;
    }

    public List<FilePathComposition> moveCompositionsToFolder(List<Composition> compositions,
                                                              String fromPath,
                                                              String toPath) {
//        Log.d("KEK2", "moveCompositionsToFolder, fromPath: " + fromPath);
//        Log.d("KEK2", "moveCompositionsToFolder, toPath: " + toPath);

        List<FilePathComposition> updatedCompositions = new LinkedList<>();
        for (Composition composition: compositions) {
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                String oldPath = getCompositionFilePath(storageId);
                String newPath = FileUtils.safeReplacePath(oldPath, fromPath, toPath);

//            Log.d("KEK2", "rename file, oldPath: " + oldPath);
//            Log.d("KEK2", "rename file, newPath: " + newPath);

                moveFile(oldPath, newPath);

                updatedCompositions.add(new FilePathComposition(composition.getId(),
                        composition.getStorageId(),
                        newPath)
                );
            }
        }
        storageMusicProvider.updateCompositionsFilePath(updatedCompositions);
        return updatedCompositions;
    }

    public String moveCompositionsToNewFolder(List<Composition> compositions,
                                              String fromPath,
                                              String toParentPath,
                                              String directoryName,
                                              List<FilePathComposition> updatedCompositions) {
        String newFolderPath = toParentPath + '/' + directoryName;
        createDirectory(newFolderPath);

        for (Composition composition: compositions) {
            Long storageId = composition.getStorageId();
            if (storageId != null) {
                String oldPath = getCompositionFilePath(storageId);
                String newPath = FileUtils.getChangedFilePath(oldPath, fromPath, newFolderPath);

                moveFile(oldPath, newPath);

                updatedCompositions.add(new FilePathComposition(composition.getId(),
                        composition.getStorageId(),
                        newPath)
                );
            }
        }
        storageMusicProvider.updateCompositionsFilePath(updatedCompositions);
        return FileUtils.getFileName(newFolderPath);
    }

    public void deleteCompositionFiles(List<Composition> compositions) {
        for (Composition composition: compositions) {
            deleteFile(composition);
        }
        storageMusicProvider.deleteCompositions(mapListNotNull(
                compositions,
                Composition::getStorageId)
        );
    }

    public void deleteCompositionFile(Composition composition) {
        deleteFile(composition);
        Long storageId = composition.getStorageId();
        if (storageId != null) {
            storageMusicProvider.deleteComposition(storageId);
        }
    }

    private void deleteFile(Composition composition) {
        Long storageId = composition.getStorageId();
        if (storageId == null) {
            return;
        }
        String filePath = storageMusicProvider.getCompositionFilePath(storageId);
        if (filePath == null) {
            return;
        }
        File parentDirectory = new File(filePath).getParentFile();
        FileManager.deleteFile(filePath);
        if (parentDirectory != null) {
            FileManager.deleteEmptyDirectory(parentDirectory);
        }
    }

    @Nonnull
    private String getCompositionFilePath(long storageId) {
        String filePath = storageMusicProvider.getCompositionFilePath(storageId);
        if (filePath == null) {
            throw new RuntimeException("composition path not found in system media store");
        }
        return filePath;
    }

    private void createDirectory(String path) {
        File file = new File(path);
        if (file.exists()) {
            throw new FileExistsException();
        }
        if (!file.mkdir()) {
            throw new RuntimeException("file not created, path: " + path);
        }
    }

    private static void moveFile(String oldPath,
                                 String newPath) {
        String destDirectoryPath = FileUtils.getParentDirPath(newPath);
        File destDirectory = new File(destDirectoryPath);
        if (!destDirectory.exists()) {
            boolean created = destDirectory.mkdirs();
            if (!created) {
                throw new RuntimeException("parent directory wasn't created");
            }
        }
        renameFile(oldPath, newPath);

        String oldDirectoryPath = FileUtils.getParentDirPath(oldPath);
        File oldDirectory = new File(oldDirectoryPath);
        String[] files = oldDirectory.list();
        if (oldDirectory.isDirectory() && files != null && files.length == 0) {
            //not necessary to check
            //noinspection ResultOfMethodCallIgnored
            oldDirectory.delete();
        }
    }

    private static void renameFile(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        if (!oldFile.exists()) {
            throw new RuntimeException("target file not exists");
        }
        File newFile = new File(newPath);
        boolean renamed = oldFile.renameTo(newFile);
        if (!renamed) {
            throw new RuntimeException("file wasn't renamed");
        }
    }

}
