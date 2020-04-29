package com.github.anrimian.musicplayer.data.storage.providers.artist;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Artists;

import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.Observable;

public class StorageArtistsProvider {

    private final ContentResolver contentResolver;

    public StorageArtistsProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public Observable<Map<String, StorageArtist>> getArtistsObservable() {
        return RxContentObserver.getObservable(contentResolver, Artists.EXTERNAL_CONTENT_URI)
                .map(o -> getArtists());
    }

    public Map<String, StorageArtist> getArtists() {
        try(Cursor cursor = contentResolver.query(Artists.EXTERNAL_CONTENT_URI,
                new String[] {
                        Artists._ID,
                        Artists.ARTIST,
//                        Artists.ARTIST_KEY,
//                        MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
//                        MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
                },
                null,
                null,
                null)) {
            if (cursor == null) {
                return new HashMap<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            Map<String, StorageArtist> artists = new HashMap<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                StorageArtist artist = getArtistFromCursor(cursorWrapper);
                if (artist != null) {
                    artists.put(artist.getName(), artist);
                }
            }
            return artists;
        }
    }

    public void updateArtistName(String oldName, String name) {
        try(Cursor cursor = contentResolver.query(
                Artists.EXTERNAL_CONTENT_URI,
                null,
                Artists.ARTIST + " = ?",
                new String[] { oldName },
                null)) {
            if (cursor == null || cursor.getCount() == 0) {
                return;
            }

            cursor.moveToPosition(0);
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);

            ContentValues cv = new ContentValues();
            long id = cursorWrapper.getLong(Artists._ID);
            cv.put(Artists._ID, id);
            cv.put(Artists.ARTIST, name);
            cv.put(Artists.ARTIST_KEY, cursorWrapper.getString(Artists.ARTIST_KEY));
            cv.put(Artists.NUMBER_OF_ALBUMS, cursorWrapper.getInt(Artists.NUMBER_OF_ALBUMS));
            cv.put(Artists.NUMBER_OF_TRACKS, cursorWrapper.getInt(Artists.NUMBER_OF_TRACKS));

//            contentResolver.delete(Artists.EXTERNAL_CONTENT_URI,
//                    Artists._ID + " = ?",
//                    new String[] { String.valueOf(id) });
            contentResolver.insert(Artists.EXTERNAL_CONTENT_URI, cv);
        }

/*        ContentValues cv = new ContentValues();
        cv.put(Artists.ARTIST, name);
        contentResolver.update(Artists.EXTERNAL_CONTENT_URI,
                cv,
                Artists.ARTIST + " = ?",
                new String[] { oldName });*/
    }


    @Nullable
    private StorageArtist getArtistFromCursor(CursorWrapper cursorWrapper) {
        String artistName = cursorWrapper.getString(Artists.ARTIST);
        if (artistName == null || artistName.equals("<unknown>")) {
            return null;
        }
        return new StorageArtist(
                cursorWrapper.getLong(Artists._ID),
                artistName
        );
    }

}
