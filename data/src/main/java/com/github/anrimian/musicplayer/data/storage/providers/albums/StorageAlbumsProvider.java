package com.github.anrimian.musicplayer.data.storage.providers.albums;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.Albums;

import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;

import java.io.FileNotFoundException;
import java.io.InputStream;

import io.reactivex.Observable;

public class StorageAlbumsProvider {

    private final ContentResolver contentResolver;

    public StorageAlbumsProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public Observable<LongSparseArray<StorageAlbum>> getAlbumsObservable() {
        return RxContentObserver.getObservable(contentResolver, Albums.EXTERNAL_CONTENT_URI)
                .map(o -> getAlbums());
    }

    public LongSparseArray<StorageAlbum> getAlbums() {
        try(Cursor cursor = contentResolver.query(Albums.EXTERNAL_CONTENT_URI,
                new String[] {
                        Albums._ID,
                        Albums.ALBUM,
//                        Albums.ALBUM_ID,
//                        Albums.ALBUM_KEY,
                        Albums.FIRST_YEAR,
                        Albums.LAST_YEAR,
                        Albums.ARTIST,
//                        Albums.ARTIST_ID
                },
                null,
                null,
                null)) {
            if (cursor == null) {
                return new LongSparseArray<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            LongSparseArray<StorageAlbum> artists = new LongSparseArray<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                StorageAlbum item = getAlbumFromCursor(cursorWrapper);
                if (item != null) {
                    artists.put(item.getId(), item);
                }
            }
            return artists;
        }
    }

    public InputStream getAlbumCoverStream(String name) throws FileNotFoundException {
        long id = getAlbumIdByName(name);
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, id);
        return contentResolver.openInputStream(uri);
    }

    public void updateAlbumName(String oldName, String artist, String name) {
        ContentValues cv = new ContentValues();
        cv.put(Albums.ALBUM, name);
        contentResolver.update(Albums.EXTERNAL_CONTENT_URI,
                cv,
                Albums.ALBUM + " = ? AND " + Albums.ARTIST + " = ?",
                new String[] { oldName, artist });
    }

    public void updateAlbumArtist(String albumName, String oldArtist, String newArtistName) {
        ContentValues cv = new ContentValues();
        cv.put(Albums.ARTIST, newArtistName);
        contentResolver.update(Albums.EXTERNAL_CONTENT_URI,
                cv,
                Albums.ALBUM + " = ? AND " + Albums.ARTIST + " = ?",
                new String[] { albumName, oldArtist });
    }

    private long getAlbumIdByName(String name) {
        try (Cursor cursor = contentResolver.query(Albums.EXTERNAL_CONTENT_URI,
                new String[] {
                        Albums._ID,
                },
                Albums.ALBUM + " = ? ",
                new String[] { name },
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                CursorWrapper cursorWrapper = new CursorWrapper(cursor);
                return cursorWrapper.getLong(Albums._ID);
            }
        }
        return 0;
    }

    @Nullable
    private StorageAlbum getAlbumFromCursor(CursorWrapper cursorWrapper) {
        String name = cursorWrapper.getString(Albums.ALBUM);
        if (name == null) {
            return null;
        }
        String artist = cursorWrapper.getString(Albums.ARTIST);
        if (artist != null && artist.equals("<unknown>")) {
            artist = null;
        }

        return new StorageAlbum(
                cursorWrapper.getLong(Albums._ID),
                name,
                artist,
                cursorWrapper.getInt(Albums.FIRST_YEAR),
                cursorWrapper.getInt(Albums.LAST_YEAR)
        );
    }

}
