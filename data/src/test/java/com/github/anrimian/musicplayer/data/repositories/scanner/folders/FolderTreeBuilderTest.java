package com.github.anrimian.musicplayer.data.repositories.scanner.folders;

import org.junit.Test;

import io.reactivex.Observable;

import static org.junit.Assert.assertNotNull;

public class FolderTreeBuilderTest {

    private final FolderTreeBuilder<String, String> folderTreeBuilder = new FolderTreeBuilder<>(
            s -> s,
            s -> s
    );

    @Test
    public void createFileTreeTest() {
        FolderNode<String> root = folderTreeBuilder.createFileTree(Observable.fromArray(
                "music",
                "music/new",
                ""
        ));
        assert root.getFiles().contains("");

        FolderNode<String> musicFolder = root.getFolder("music");
        assertNotNull(musicFolder);
        assert musicFolder.getFiles().contains("music");

        FolderNode<String> newFolder = musicFolder.getFolder("new");
        assertNotNull(newFolder);
        assert newFolder.getFiles().contains("music/new");
    }
}