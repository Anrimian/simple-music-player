package com.github.anrimian.musicplayer.data.storage.source;

import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceEditor;
import com.github.anrimian.musicplayer.data.utils.files.ResourceFile;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class CompositionSourceEditorTest {

    @Rule
    public ResourceFile res = new ResourceFile("/Кот Леопольд - Неприятность эту мы переживем.mp3");

    private StorageMusicProvider musicProvider = mock(StorageMusicProvider.class);

    private CompositionSourceEditor sourceEditor = new CompositionSourceEditor(musicProvider);

    @Test
    public void testEditor() throws IOException {
        String filePath = res.getFile().getPath();
        System.out.println("title: " + sourceEditor.getCompositionTitle(filePath).blockingGet());
        System.out.println("author: " + sourceEditor.getCompositionAuthor(filePath).blockingGet());
        System.out.println("album: " + sourceEditor.getCompositionAlbum(filePath).blockingGet());
        System.out.println("album artist: " + sourceEditor.getCompositionAlbumArtist(filePath).blockingGet());
        System.out.println("genre: " + sourceEditor.getCompositionGenre(filePath).blockingGet());
    }

    @Test
    public void changeTitleTest() throws IOException {
        String filePath = res.getFile().getPath();
        System.out.println("title: " + sourceEditor.getCompositionTitle(filePath).blockingGet());

        String testTitle = "Test title";
        sourceEditor.setCompositionTitle(filePath, testTitle).subscribe();
        String newTitle = sourceEditor.getCompositionTitle(filePath).blockingGet();
        System.out.println("new title: " + sourceEditor.getCompositionTitle(filePath).blockingGet());
        assertEquals(testTitle, newTitle);
    }

    @Test
    public void changeAlbumTest() throws IOException {
        String filePath = res.getFile().getPath();
        System.out.println("album: " + sourceEditor.getCompositionAlbum(filePath).blockingGet());

        String testAlbum = "Test album";
        sourceEditor.setCompositionAlbum(filePath, testAlbum).subscribe();
        String newTitle = sourceEditor.getCompositionAlbum(filePath).blockingGet();
        System.out.println("new album: " + sourceEditor.getCompositionAlbum(filePath).blockingGet());
        assertEquals(testAlbum, newTitle);
    }

    @Test
    public void addGenreTest() throws IOException {
        String filePath = res.getFile().getPath();
        String genres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("genres: " + genres);

        String testGenre1 = "Test genre1";
        String testGenre2 = "Test genre2";
        sourceEditor.addCompositionGenre(filePath, testGenre1).subscribe();
        sourceEditor.addCompositionGenre(filePath, testGenre2).subscribe();
        String newGenres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("new genres: " + newGenres);
        assertEquals(testGenre1 + "\u0000" + testGenre2, newGenres);
    }

    @Test
    public void removeGenreTest() throws IOException {
        String filePath = res.getFile().getPath();
        String genres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("genres: " + genres);

        String testGenre1 = "Test genre1";
        String testGenre2 = "Test genre2";
        sourceEditor.addCompositionGenre(filePath, testGenre1).subscribe();
        sourceEditor.addCompositionGenre(filePath, testGenre2).subscribe();
        sourceEditor.removeCompositionGenre(filePath, testGenre1).subscribe();
        String newGenres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("new genres: " + newGenres);
        assertEquals(testGenre2, newGenres);
    }

    @Test
    public void removeLastTest() throws IOException {
        String filePath = res.getFile().getPath();
        String genres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("genres: " + genres);

        String testGenre1 = "Test genre1";
        sourceEditor.addCompositionGenre(filePath, testGenre1).subscribe();
        sourceEditor.removeCompositionGenre(filePath, testGenre1).subscribe();
        String newGenres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("new genres: " + newGenres);
        assertEquals("", newGenres);
    }

    @Test
    public void changeGenreTest() throws IOException {
        String filePath = res.getFile().getPath();
        String genres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("genres: " + genres);

        String testGenre1 = "Test genre1";
        String testGenre2 = "Test genre2";
        String testGenre3 = "Test genre3";
        sourceEditor.addCompositionGenre(filePath, testGenre1).subscribe();
        sourceEditor.addCompositionGenre(filePath, testGenre2).subscribe();
        sourceEditor.changeCompositionGenre(filePath, testGenre1, testGenre3).subscribe();
        String newGenres = sourceEditor.getCompositionGenre(filePath).blockingGet();
        System.out.println("new genres: " + newGenres);
        assertEquals(testGenre3 + "\u0000" + testGenre2, newGenres);
    }

    @Test
    public void changeAlbumArtistTest() throws IOException {
        String filePath = res.getFile().getPath();
        System.out.println("album artist: " + sourceEditor.getCompositionAlbumArtist(filePath).blockingGet());

        String testName = "Test album artist";
        sourceEditor.setCompositionAlbumArtist(filePath, testName).subscribe();
        String newArtist = sourceEditor.getCompositionAlbumArtist(filePath).blockingGet();
        System.out.println("new album artist: " + newArtist);
        assertEquals(testName, newArtist);
    }
}