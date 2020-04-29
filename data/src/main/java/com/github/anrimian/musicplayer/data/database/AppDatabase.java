package com.github.anrimian.musicplayer.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.github.anrimian.musicplayer.data.database.converters.DateConverter;
import com.github.anrimian.musicplayer.data.database.converters.EnumConverter;
import com.github.anrimian.musicplayer.data.database.dao.albums.AlbumsDao;
import com.github.anrimian.musicplayer.data.database.dao.artist.ArtistsDao;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.dao.folders.FoldersDao;
import com.github.anrimian.musicplayer.data.database.dao.genre.GenreDao;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListDao;
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDao;
import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.IgnoredFolderEntity;
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntity;
import com.github.anrimian.musicplayer.data.database.entities.genres.GenreEntryEntity;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryEntity;

/**
 * Created on 18.11.2017.
 */

@Database(entities = {
        PlayQueueEntity.class,
        CompositionEntity.class,
        PlayListEntity.class,
        PlayListEntryEntity.class,
        ArtistEntity.class,
        AlbumEntity.class,
        GenreEntity.class,
        GenreEntryEntity.class,
        IgnoredFolderEntity.class,
        FolderEntity.class
}, version = 6)
@TypeConverters({
        DateConverter.class,
        EnumConverter.class
})
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlayQueueDao playQueueDao();
    public abstract CompositionsDao compositionsDao();
    public abstract PlayListDao playListDao();
    public abstract ArtistsDao artistsDao();
    public abstract AlbumsDao albumsDao();
    public abstract GenreDao genreDao();
    public abstract FoldersDao foldersDao();
}
