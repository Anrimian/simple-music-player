package com.github.anrimian.musicplayer.data.storage.providers.genres;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Genres;

import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

public class StorageGenresProvider {

    private final ContentResolver contentResolver;

    public StorageGenresProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public Observable<Map<String, StorageGenre>> getGenresObservable() {
        return RxContentObserver.getObservable(contentResolver, Genres.EXTERNAL_CONTENT_URI)
                .map(o -> getGenres());
    }

    public Map<String, StorageGenre> getGenres() {
        try(Cursor cursor = contentResolver.query(Genres.EXTERNAL_CONTENT_URI,
                new String[] {
                        Genres._ID,
                        Genres.NAME
                },
                null,
                null,
                null)) {
            if (cursor == null) {
                return new HashMap<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            Map<String, StorageGenre> genres = new HashMap<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                StorageGenre item = getGenreFromCursor(cursorWrapper);
                if (item != null) {
                    genres.put(item.getName(), item);
                }
            }
            return genres;
        }
    }

    public Observable<LongSparseArray<StorageGenreItem>> getGenreItemsObservable(long genreId) {
        return RxContentObserver.getObservable(contentResolver, Genres.Members.getContentUri("external", genreId))
                .map(o -> getGenreItems(genreId));
    }

    public LongSparseArray<StorageGenreItem> getGenreItems(long genreId) {
        try(Cursor cursor = contentResolver.query(
                Genres.Members.getContentUri("external", genreId),
                new String[] {
                        Genres.Members._ID,
                        Genres.Members.AUDIO_ID
                },
                null,
                null,
                null)) {
            if (cursor == null) {
                return new LongSparseArray<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            LongSparseArray<StorageGenreItem> artists = new LongSparseArray<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                StorageGenreItem item = getGenreItemFromCursor(cursorWrapper);
                artists.put(item.getId(), item);
            }
            return artists;
        }
    }

    public void updateGenreName(String oldName, String newName) {
        ContentValues cv = new ContentValues();
        cv.put(Genres.NAME, newName);
        contentResolver.update(Genres.EXTERNAL_CONTENT_URI,
                cv,
                Genres.NAME + " = ?",
                new String[] { oldName });
    }

    private StorageGenreItem getGenreItemFromCursor(CursorWrapper cursorWrapper) {
        return new StorageGenreItem(
                cursorWrapper.getLong(Genres.Members._ID),
                cursorWrapper.getLong(Genres.Members.AUDIO_ID)
        );
    }

    @Nullable
    private StorageGenre getGenreFromCursor(CursorWrapper cursorWrapper) {
        String name = cursorWrapper.getString(Genres.NAME);
        if (name == null) {
            return null;
        }
        return new StorageGenre(
                cursorWrapper.getLong(Genres._ID),
                name
        );
    }
}
