package com.github.anrimian.musicplayer.data.database;

import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static org.junit.Assert.assertEquals;

public class MigrationsTest {

    private static final String TEST_DB_NAME = "music_player_database";

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);

    private Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private Context context = instrumentation.getContext();

    @Rule
    public MigrationTestHelper testHelper =
            new MigrationTestHelper(
                    instrumentation,
                    AppDatabase.class.getCanonicalName(),
                    new FrameworkSQLiteOpenHelperFactory());

    @Test
    public void testMigrationFrom5To6() throws IOException {
        SupportSQLiteDatabase db = testHelper.createDatabase(TEST_DB_NAME, 5);

        ContentValues cv = new ContentValues();
        cv.put("storageId", 1L);
        cv.put("title", "titleHH");
        cv.put("filePath", "test/music/filename34.mp3");
        cv.put("duration", 13);
        cv.put("size", 100);
        cv.put("dateAdded", 0L);
        cv.put("dateModified", 0L);
        long id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv);

        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                6,
                false,
                Migrations.MIGRATION_5_6);

        Cursor c = db.query("SELECT fileName FROM compositions WHERE id = " + id);
        c.moveToFirst();
        assertEquals("filename34.mp3", c.getString(c.getColumnIndex("fileName")));
    }

    @Test
    public void testMigrationFrom4To5() throws IOException {
        SupportSQLiteDatabase db = testHelper.createDatabase(TEST_DB_NAME, 4);

        ContentValues cv = new ContentValues();
        cv.put("storageId", 1L);
        cv.put("artistId", (Long) null);
        cv.put("title", "titleHH");
        cv.put("filePath", "filePath");
        cv.put("duration", 13);
        cv.put("size", 100);
        cv.put("dateAdded", 0L);
        cv.put("dateModified", 0L);
        long id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv);

        ContentValues cvQueueItem = new ContentValues();
        cvQueueItem.put("audioId", id);
        cvQueueItem.put("position", 0);
        cvQueueItem.put("shuffledPosition", 0);
        long queueId = db.insert("play_queue", SQLiteDatabase.CONFLICT_ABORT, cvQueueItem);

        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                5,
                false,
                Migrations.MIGRATION_4_5);

        Cursor c = db.query("SELECT title FROM compositions WHERE id = " + id);
        c.moveToFirst();
        assertEquals("titleHH", c.getString(c.getColumnIndex("title")));

        Cursor cursorQueue = db.query("SELECT audioId FROM play_queue WHERE id = " + queueId);
        cursorQueue.moveToFirst();
        assertEquals(id, cursorQueue.getLong(cursorQueue.getColumnIndex("audioId")));
    }

    @Test
    public void testMigrationFrom3To4() throws IOException {
        SupportSQLiteDatabase db = testHelper.createDatabase(TEST_DB_NAME, 3);

        ContentValues cv = new ContentValues();
        cv.put("storageId", 1L);
        cv.put("artist", "artist");
        cv.put("title", "title");
        cv.put("album", "album");
        cv.put("filePath", "filePath");
        cv.put("duration", "duration");
        cv.put("size", "size");
        cv.put("dateAdded", 0L);
        cv.put("dateModified", 0L);
        long id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv);

        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                4,
                false,
                Migrations.getMigration3_4(context));

        Cursor c = db.query("SELECT name FROM artists WHERE id = (SELECT artistId FROM compositions WHERE id = " + id + ")");
        c.moveToFirst();
        assertEquals("artist", c.getString(c.getColumnIndex("name")));
    }

    @Test
    public void testMigrationFrom2To3() throws Exception {
        SupportSQLiteDatabase db = testHelper.createDatabase(TEST_DB_NAME, 2);

        //add duplicate indexes for test
        ContentValues cv = new ContentValues();
        cv.put("storageId", 1L);
        cv.put("artist", "artist");
        cv.put("title", "title");
        cv.put("album", "album");
        cv.put("filePath", "filePath");
        cv.put("duration", "duration");
        cv.put("size", "size");
        cv.put("dateAdded", 0L);
        cv.put("dateModified", 0L);
        long id = db.insert("compositions", SQLiteDatabase.CONFLICT_ABORT, cv);
        ContentValues cvQueueItem = new ContentValues();
        cvQueueItem.put("audioId", id);
        cvQueueItem.put("position", 0);
        cvQueueItem.put("shuffledPosition", 0);
        db.insert("play_queue", SQLiteDatabase.CONFLICT_ABORT, cvQueueItem);
        db.insert("play_queue", SQLiteDatabase.CONFLICT_ABORT, cvQueueItem);

        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                3,
                false,
                Migrations.MIGRATION_2_3);
    }

    @Test
    public void testMigrationFrom1To2() throws Exception {
        testHelper.createDatabase(TEST_DB_NAME, 1);
        testHelper.runMigrationsAndValidate(TEST_DB_NAME,
                2,
                false,
                Migrations.getMigration1_2(context));
    }
}