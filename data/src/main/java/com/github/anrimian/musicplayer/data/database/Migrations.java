package com.github.anrimian.musicplayer.data.database;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.core.content.ContextCompat;
import androidx.room.migration.Migration;
import androidx.room.util.CursorUtil;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.github.anrimian.musicplayer.data.database.converters.EnumConverter;
import com.github.anrimian.musicplayer.data.database.mappers.CompositionCorruptionDetector;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageFullComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageMusicProvider;
import com.github.anrimian.musicplayer.domain.utils.FileUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@SuppressLint("RestrictedApi")
class Migrations {

    static Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE compositions ADD COLUMN lyrics TEXT");
        }
    };

    static Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE compositions ADD COLUMN fileName TEXT");

            //migrate file name
            Cursor c = database.query("SELECT id, filePath FROM compositions");
            while (c.moveToNext()) {
                long id = c.getLong(CursorUtil.getColumnIndex(c,"id"));
                String filePath = c.getString(CursorUtil.getColumnIndex(c,"filePath"));

                String fileName = FileUtils.formatFileName(filePath, true);

                ContentValues cv = new ContentValues();
                cv.put("fileName", fileName);

                database.update("compositions",
                        SQLiteDatabase.CONFLICT_REPLACE,
                        cv,
                        "id = ?",
                        new String[]{ String.valueOf(id) }
                );
            }
        }
    };

    static Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `ignored_folders` (`relativePath` TEXT NOT NULL, `addDate` INTEGER, PRIMARY KEY(`relativePath`))");

            database.execSQL("CREATE TABLE IF NOT EXISTS `folders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `parentId` INTEGER, `name` TEXT, FOREIGN KEY(`parentId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("CREATE  INDEX `index_folders_parentId` ON `folders` (`parentId`)");

            //migrate compositions
            database.execSQL("CREATE TABLE IF NOT EXISTS `compositions_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `albumId` INTEGER, `folderId` INTEGER, `storageId` INTEGER, `title` TEXT, `filePath` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `corruptionType` TEXT, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`folderId`) REFERENCES `folders`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");

            //don't migrate folders, they will update automatically on next storage rescan
            Cursor c = database.query("SELECT * FROM compositions");
            while (c.moveToNext()) {
                ContentValues cv = new ContentValues();

                cv.put("id", getLong(c, "id"));
                cv.put("artistId", getLong(c, "artistId"));
                cv.put("albumId", getLong(c, "albumId"));
                cv.put("storageId", getLong(c, "storageId"));
                cv.put("title", c.getString(c.getColumnIndex("title")));
                cv.put("filePath", c.getString(c.getColumnIndex("filePath")));
                cv.put("duration", getLong(c, "duration"));
                cv.put("size", getLong(c, "size"));
                cv.put("dateAdded", getLong(c, "dateAdded"));
                cv.put("dateModified", getLong(c, "dateModified"));
                cv.put("corruptionType", c.getString(c.getColumnIndex("corruptionType")));
                database.insert("compositions_temp", SQLiteDatabase.CONFLICT_REPLACE, cv);
            }

            database.execSQL("DROP TABLE `compositions`");
            database.execSQL("ALTER TABLE `compositions_temp` RENAME TO `compositions`");

            database.execSQL("CREATE  INDEX `index_compositions_folderId` ON compositions (`folderId`)");
            database.execSQL("CREATE  INDEX `index_compositions_artistId` ON compositions (`artistId`)");
            database.execSQL("CREATE  INDEX `index_compositions_albumId` ON compositions (`albumId`)");
        }
    };

    @SuppressLint("RestrictedApi")
    private static Long getLong(Cursor c, String columnName) {
        int columnIndex = CursorUtil.getColumnIndex(c, columnName);
        if (columnIndex < 0 || c.isNull(columnIndex)) {
            return null;
        } else {
            return c.getLong(columnIndex);
        }
    }

    static Migration getMigration3_4(Context context) {
        return new Migration(3, 4) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS artists (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)");
                database.execSQL("CREATE UNIQUE INDEX `index_artists_name` ON artists (`name`)");

                database.execSQL("CREATE TABLE IF NOT EXISTS albums (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `name` TEXT, `firstYear` INTEGER NOT NULL, `lastYear` INTEGER NOT NULL, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");
                database.execSQL("CREATE  INDEX `index_albums_artistId` ON albums (`artistId`)");
                database.execSQL("CREATE UNIQUE INDEX `index_albums_artistId_name` ON albums (`artistId`, `name`)");

                //compositions
                database.execSQL("CREATE TABLE IF NOT EXISTS compositions_temp (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `artistId` INTEGER, `albumId` INTEGER, `storageId` INTEGER, `title` TEXT, `filePath` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `corruptionType` TEXT, FOREIGN KEY(`artistId`) REFERENCES `artists`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`albumId`) REFERENCES `albums`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");

                StorageAlbumsProvider storageAlbumsProvider = new StorageAlbumsProvider(context);
                StorageMusicProvider provider = new StorageMusicProvider(context, storageAlbumsProvider);
                LongSparseArray<StorageFullComposition> storageCompositions;
                if (hasFilePermission(context)) {
                    storageCompositions = provider.getCompositions();
                } else {
                    storageCompositions = new LongSparseArray<>();
                }

                Map<String, Long> artistCache = new HashMap<>();
                Map<String, Long> albumsCache = new HashMap<>();
                Cursor c = database.query("SELECT * FROM compositions");
                while (c.moveToNext()) {
                    ContentValues cv = new ContentValues();

                    //artists
                    String artist = c.getString(c.getColumnIndex("artist"));
                    Long artistId = insertArtist(artist, database, artistCache);
                    cv.put("artistId", artistId);

                    long storageId = c.getLong(c.getColumnIndex("storageId"));

                    //albums
                    Long albumId = null;
                    if (storageId != 0) {
                        StorageFullComposition composition = storageCompositions.get(storageId);
                        if (composition != null) {
                            StorageAlbum album = composition.getStorageAlbum();
                            if (album != null) {
                                Long albumArtistId = insertArtist(album.getArtist(), database, artistCache);
                                albumId = insertAlbum(album, albumArtistId, database, albumsCache);
                            }
                        }
                    }
                    cv.put("albumId", albumId);

                    cv.put("id", c.getLong(c.getColumnIndex("id")));
                    cv.put("storageId", storageId);
                    cv.put("title", c.getString(c.getColumnIndex("title")));
                    cv.put("filePath", c.getString(c.getColumnIndex("filePath")));
                    cv.put("duration", c.getLong(c.getColumnIndex("duration")));
                    cv.put("size", c.getLong(c.getColumnIndex("size")));
                    cv.put("dateAdded", c.getLong(c.getColumnIndex("dateAdded")));
                    cv.put("dateModified", c.getLong(c.getColumnIndex("dateModified")));
                    cv.put("corruptionType", c.getString(c.getColumnIndex("corruptionType")));
                    database.insert("compositions_temp", SQLiteDatabase.CONFLICT_REPLACE, cv);
                }

                database.execSQL("DROP TABLE compositions");
                database.execSQL("ALTER TABLE compositions_temp RENAME TO compositions");
                database.execSQL("CREATE  INDEX `index_compositions_artistId` ON compositions (`artistId`)");
                database.execSQL("CREATE  INDEX `index_compositions_albumId` ON compositions (`albumId`)");

                database.execSQL("CREATE TABLE IF NOT EXISTS genres (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageId` INTEGER, `name` TEXT)");
                database.execSQL("CREATE UNIQUE INDEX `index_genres_name` ON genres (`name`)");
                database.execSQL("CREATE TABLE IF NOT EXISTS genre_entries (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `audioId` INTEGER NOT NULL, `genreId` INTEGER NOT NULL, `storageId` INTEGER, FOREIGN KEY(`audioId`) REFERENCES `compositions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`genreId`) REFERENCES `genres`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
                database.execSQL("CREATE  INDEX `index_genre_entries_audioId` ON genre_entries (`audioId`)");
                database.execSQL("CREATE  INDEX `index_genre_entries_genreId` ON genre_entries (`genreId`)");
            }
        };
    }

    @Nullable
    private static Long insertArtist(String artist,
                                     SupportSQLiteDatabase database,
                                     Map<String, Long> artistCache) {
        Long artistId = null;
        if (artist != null) {
            artistId = artistCache.get(artist);
            if (artistId == null) {
                ContentValues cvArt = new ContentValues();
                cvArt.put("name", artist);
                artistId = database.insert("artists", SQLiteDatabase.CONFLICT_REPLACE, cvArt);
                artistCache.put(artist, artistId);
            }
        }
        return artistId;
    }

    private static Long insertAlbum(StorageAlbum album,
                                    Long albumArtistId,
                                    SupportSQLiteDatabase database,
                                    Map<String, Long> albumsCache) {
        String albumName = album.getAlbum();

        Long albumId = albumsCache.get(albumName);
        if (albumId != null) {
            return albumId;
        }

        ContentValues cvAlb = new ContentValues();
        cvAlb.put("artistId", albumArtistId);
        cvAlb.put("name", album.getAlbum());
        cvAlb.put("firstYear", album.getFirstYear());
        cvAlb.put("lastYear", album.getLastYear());

        albumId = database.insert("albums", SQLiteDatabase.CONFLICT_REPLACE, cvAlb);

        albumsCache.put(albumName, albumId);
        return albumId;
    }

    static Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //copy values with verified index
            Cursor c = database.query("SELECT id FROM play_queue ORDER BY position");
            LongSparseArray<Integer> positionMap = new LongSparseArray<>();
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                positionMap.put(c.getLong(c.getColumnIndex("id")), i);
            }

            c = database.query("SELECT id, audioId FROM play_queue ORDER BY shuffledPosition");
            LinkedList<ContentValues> cvList = new LinkedList<>();
            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                ContentValues cv = new ContentValues();
                long id = c.getLong(c.getColumnIndex("id"));
                cv.put("id", id);
                cv.put("audioId", c.getLong(c.getColumnIndex( "audioId")));
                cv.put("position", positionMap.get(id));
                cv.put("shuffledPosition", i);
                cvList.add(cv);
            }

            database.execSQL("DELETE FROM play_queue");
            for (ContentValues cv: cvList) {
                database.insert("play_queue", SQLiteDatabase.CONFLICT_REPLACE, cv);
            }

            database.execSQL("CREATE UNIQUE INDEX `index_play_queue_position` ON `play_queue` (`position`)");
            database.execSQL("CREATE UNIQUE INDEX `index_play_queue_shuffledPosition` ON `play_queue` (`shuffledPosition`)");
        }
    };

    static Migration getMigration1_2(Context context) {
        return new Migration(1, 2) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `compositions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageId` INTEGER, `artist` TEXT, `title` TEXT, `album` TEXT, `filePath` TEXT, `duration` INTEGER NOT NULL, `size` INTEGER NOT NULL, `dateAdded` INTEGER, `dateModified` INTEGER, `corruptionType` TEXT)");
                StorageAlbumsProvider albumsProvider = new StorageAlbumsProvider(context);
                StorageMusicProvider provider = new StorageMusicProvider(context, albumsProvider);

                EnumConverter enumConverter = new EnumConverter();
                LongSparseArray<StorageFullComposition> map = provider.getCompositions();
                for(int i = 0, size = map.size(); i < size; i++) {
                    StorageFullComposition composition = map.valueAt(i);
                    ContentValues cv = new ContentValues();
                    cv.put("storageId", composition.getId());
                    cv.put("artist", composition.getArtist());
                    cv.put("title", composition.getTitle());
                    StorageAlbum storageAlbum = composition.getStorageAlbum();
                    if (storageAlbum != null) {
                        cv.put("album", storageAlbum.getAlbum());
                    }
                    cv.put("filePath", composition.getFilePath());
                    cv.put("duration", composition.getDuration());
                    cv.put("size", composition.getSize());
                    cv.put("dateAdded", composition.getDateAdded().getTime());
                    cv.put("dateModified", composition.getDateModified().getTime());
                    cv.put("corruptionType", enumConverter.toName(CompositionCorruptionDetector.getCorruptionType(composition)));
                    database.insert("compositions", SQLiteDatabase.CONFLICT_REPLACE, cv);
                }

                //playlists
                database.execSQL("CREATE TABLE IF NOT EXISTS `play_lists` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageId` INTEGER, `name` TEXT, `dateAdded` INTEGER, `dateModified` INTEGER)");
                database.execSQL("CREATE UNIQUE INDEX `index_play_lists_name` ON `play_lists` (`name`)");

                //play lists entries
                database.execSQL("CREATE TABLE IF NOT EXISTS `play_lists_entries` (`itemId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `storageItemId` INTEGER, `audioId` INTEGER NOT NULL, `playListId` INTEGER NOT NULL, `orderPosition` INTEGER NOT NULL, FOREIGN KEY(`audioId`) REFERENCES `compositions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`playListId`) REFERENCES `play_lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
                database.execSQL("CREATE INDEX `index_play_lists_entries_audioId` ON `play_lists_entries` (`audioId`)");
                database.execSQL("CREATE INDEX `index_play_lists_entries_playListId` ON `play_lists_entries` (`playListId`)");

                //play queue
                database.execSQL("CREATE TABLE IF NOT EXISTS `play_queue_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `audioId` INTEGER NOT NULL, `position` INTEGER NOT NULL, `shuffledPosition` INTEGER NOT NULL, FOREIGN KEY(`audioId`) REFERENCES `compositions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
                Cursor c = database.query("SELECT id, (SELECT id FROM compositions WHERE storageId = audioId), position, shuffledPosition FROM play_queue");
                while (c.moveToNext()) {
                    ContentValues cv = new ContentValues();
                    cv.put("id", getLong(c, "id"));
                    Long audioId = getLong(c, "audioId");
                    if (audioId == null || audioId < 1) {
                        continue;
                    }
                    cv.put("audioId", audioId);
                    cv.put("position", getLong(c, "position"));
                    cv.put("shuffledPosition", getLong(c, "shuffledPosition"));
                    database.insert("play_queue_new", SQLiteDatabase.CONFLICT_REPLACE, cv);
                }
                database.execSQL("INSERT INTO `play_queue_new` (id, audioId, position, shuffledPosition) " +
                        "SELECT id, (SELECT id FROM compositions WHERE storageId = audioId), position, shuffledPosition " +
                        "FROM play_queue");//select and replace old audio id with new?
                database.execSQL("DROP TABLE play_queue");
                database.execSQL("ALTER TABLE play_queue_new RENAME TO play_queue");

                database.execSQL("CREATE  INDEX `index_play_queue_audioId` ON `play_queue` (`audioId`)");
            }
        };
    }

    private static boolean hasFilePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }
}
