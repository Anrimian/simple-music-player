package com.github.anrimian.musicplayer.data.database.dao.genre;

import android.content.Context;

import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntity;
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntryEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static utils.TestDataProvider.composition;

public class GenresDaoWrapperTest {

    private AppDatabase db;
    private CompositionsDao compositionsDao;
    private GenreDao genreDao;

    private GenresDaoWrapper daoWrapper;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        compositionsDao = db.compositionsDao();
        genreDao = db.genreDao();

        daoWrapper = new GenresDaoWrapper(db, genreDao, compositionsDao);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void updateGenreToUnknownName() {
        long genreId = genreDao.insert(new GenreEntity(null, "test genre"));
        long compositionId = compositionsDao.insert(composition(null, null, "test title"));
        genreDao.insertGenreEntry(new GenreEntryEntity(compositionId, genreId, null));

        daoWrapper.updateCompositionGenre(compositionId, "test genre2");

        Long newGenreId = genreDao.findGenre("test genre2");
        assertNull(genreDao.findGenre("test genre"));
        assertNotNull(newGenreId);

        assertEquals(newGenreId, genreDao.getGenresByCompositionId(compositionId)[0]);
    }

    @Test
    public void updateGenreToKnownName() {
        long genreId = genreDao.insert(new GenreEntity(null, "test genre"));
        Long secondGenreId = genreDao.insert(new GenreEntity(null, "test genre2"));
        long compositionId = compositionsDao.insert(composition(null, null, "test title"));
        genreDao.insertGenreEntry(new GenreEntryEntity(compositionId, genreId, null));

        daoWrapper.updateCompositionGenre(compositionId, "test genre2");

        Long newGenreId = genreDao.findGenre("test genre2");
        assertNull(genreDao.findGenre("test genre"));
        assertNotNull(newGenreId);

        assertEquals(secondGenreId, genreDao.getGenresByCompositionId(compositionId)[0]);
    }

    @Test
    public void updateGenreWithMultipleEntries() {
        long genreId = genreDao.insert(new GenreEntity(null, "test genre"));
        long compositionId = compositionsDao.insert(composition(null, null, "test title"));
        genreDao.insertGenreEntry(new GenreEntryEntity(compositionId, genreId, null));
        long secondCompositionId = compositionsDao.insert(composition(null, null, "test title2"));
        genreDao.insertGenreEntry(new GenreEntryEntity(secondCompositionId, genreId, null));

        daoWrapper.updateCompositionGenre(compositionId, "test genre2");

        Long newGenreId = genreDao.findGenre("test genre2");
        assertNotNull(genreDao.findGenre("test genre"));
        assertNotNull(newGenreId);

        assertEquals(newGenreId, genreDao.getGenresByCompositionId(compositionId)[0]);
    }

    @Test
    public void testUpdateCompositionWithMultipleGenres() {
        long genreId = genreDao.insert(new GenreEntity(null, "test genre"));
        long secondGenreId = genreDao.insert(new GenreEntity(null, "test genre2"));
        long compositionId = compositionsDao.insert(composition(null, null, "test title"));
        genreDao.insertGenreEntry(new GenreEntryEntity(compositionId, genreId, null));
        genreDao.insertGenreEntry(new GenreEntryEntity(compositionId, secondGenreId, null));

        daoWrapper.updateCompositionGenre(compositionId, "test genre3");

        Long newGenreId = genreDao.findGenre("test genre3");
        assertNull(genreDao.findGenre("test genre"));
        assertNull(genreDao.findGenre("test genre2"));
        assertNotNull(newGenreId);

        assertEquals(newGenreId, genreDao.getGenresByCompositionId(compositionId)[0]);
    }
}