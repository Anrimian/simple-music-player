package com.github.anrimian.musicplayer.data.storage.providers.playlists;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.Playlists;

import androidx.collection.LongSparseArray;

import com.github.anrimian.musicplayer.data.models.exceptions.PlayListNotCreatedException;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.data.utils.rx.content_observer.RxContentObserver;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.rx.FastDebounceFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Observable;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Audio.Playlists.Members.AUDIO_ID;
import static android.provider.MediaStore.Audio.Playlists.Members.getContentUri;
import static android.text.TextUtils.isEmpty;
import static java.util.Collections.emptyList;

public class StoragePlayListsProvider {

    private final ContentResolver contentResolver;

    public StoragePlayListsProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public Observable<LongSparseArray<StoragePlayList>> getPlayListsObservable() {
        return RxContentObserver.getObservable(contentResolver, Playlists.EXTERNAL_CONTENT_URI)
                .map(o -> getPlayLists());
    }

    public LongSparseArray<StoragePlayList> getPlayLists() {
        try(Cursor cursor = contentResolver.query(Playlists.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null)) {
            if (cursor == null) {
                return new LongSparseArray<>();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);

            LongSparseArray<StoragePlayList> map = new LongSparseArray<>();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                StoragePlayList playList = getPlayListFromCursor(cursorWrapper);
                if (playList != null) {
                    map.put(playList.getStorageId(), playList);
                }
            }
            return map;
        }
    }

    public Long createPlayList(String name, Date dateAdded, Date dateModified) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Playlists.NAME, name);
        contentValues.put(Playlists.DATE_ADDED, dateAdded.getTime() / 1000L);
        contentValues.put(Playlists.DATE_MODIFIED, dateModified.getTime() / 1000L);
        Uri uri = contentResolver.insert(Playlists.EXTERNAL_CONTENT_URI, contentValues);
        if (uri == null || isEmpty(uri.getLastPathSegment())) {
            return null;
        }
        long id = Long.parseLong(uri.getLastPathSegment());
        StoragePlayList playList = findPlayList(id);
        if (playList == null) {
            return null;
        }
        return playList.getStorageId();
    }

    public StoragePlayList createPlayList(String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Playlists.NAME, name);
        contentValues.put(Playlists.DATE_MODIFIED, System.currentTimeMillis() / 1000L);
        Uri uri = contentResolver.insert(Playlists.EXTERNAL_CONTENT_URI, contentValues);
        if (uri == null || isEmpty(uri.getLastPathSegment())) {
            throw new PlayListNotCreatedException();
        }
        long id = Long.parseLong(uri.getLastPathSegment());
        StoragePlayList playList = findPlayList(id);
        if (playList == null) {
            throw new PlayListNotCreatedException();
        }
        return playList;
    }

    public void deletePlayList(long id) {
        contentResolver.delete(Playlists.EXTERNAL_CONTENT_URI,
                Playlists._ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    public Observable<List<StoragePlayListItem>> getPlayListEntriesObservable(long playListId) {
        return RxContentObserver.getObservable(contentResolver, getContentUri("external", playListId))
                .debounce(new FastDebounceFilter<>())
                .map(o -> getPlayListItems(playListId));
    }

    public List<StoragePlayListItem> getPlayListItems(long playListId) {
        try(Cursor cursor = contentResolver.query(
                getContentUri("external", playListId),
                new String[] {AUDIO_ID, _ID},
                null,
                null,
                Playlists.Members.PLAY_ORDER)) {
            if (cursor == null) {
                return emptyList();
            }
            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            List<StoragePlayListItem> items = new ArrayList<>(cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);

                StoragePlayListItem item = getPlayListItemFromCursor(cursorWrapper);
                items.add(item);
            }
            return items;
        }
    }

    public void addCompositionToPlayList(long compositionId, long playListId, int position) {
        ContentValues values = new ContentValues();
        values.put(Playlists.Members.PLAY_ORDER, position);
        values.put(Playlists.Members.AUDIO_ID, compositionId);
        values.put(Playlists.Members.PLAYLIST_ID, playListId);

        contentResolver.insert(getContentUri("external", playListId), values);
        updateModifyTime(playListId);
    }

    public void addCompositionsToPlayList(List<Composition> compositions, long playListId, int startPosition) {
        int position = startPosition;
        ContentValues[] valuesList = new ContentValues[compositions.size()];
        for (int i = 0; i < compositions.size(); i++) {
            Long compositionId = compositions.get(i).getStorageId();
            if (compositionId == null) {
                continue;
            }
            ContentValues values = new ContentValues();
            values.put(Playlists.Members.PLAY_ORDER, position);
            values.put(Playlists.Members.AUDIO_ID, compositionId);
            values.put(Playlists.Members.PLAYLIST_ID, playListId);
            valuesList[i] = values;
            position++;
        }

        try {
            contentResolver.bulkInsert(
                    getContentUri("external", playListId),
                    valuesList
            );
            updateModifyTime(playListId);
        } catch (SecurityException ignored) {}
    }

    public void deleteItemFromPlayList(long itemId, long playListId) {
        contentResolver.delete(
                getContentUri("external", playListId),
                Playlists.Members._ID + " = ?",
                new String[] { String.valueOf(itemId) }
        );
    }

    public void moveItemInPlayList(long playListId, int from, int to) {
        Playlists.Members.moveItem(contentResolver, playListId, from, to);
    }

    public void updatePlayListName(long playListId, String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Playlists.NAME, name);
        contentValues.put(Playlists.DATE_MODIFIED, System.currentTimeMillis() / 1000L);
        contentResolver.update(Playlists.EXTERNAL_CONTENT_URI,
                contentValues,
                Playlists._ID + " = ?",
                new String[] { String.valueOf(playListId) });
    }

    private void updateModifyTime(long playListId) {
        ContentValues playListValues = new ContentValues();
        playListValues.put(Playlists.DATE_MODIFIED, System.currentTimeMillis() / 1000L);
        contentResolver.update(Playlists.EXTERNAL_CONTENT_URI,
                playListValues,
                Playlists._ID + " = ?",
                new String[] { String.valueOf(playListId) });
    }

    @Nullable
    private StoragePlayList findPlayList(long id) {
        try(Cursor cursor = contentResolver.query(
                Playlists.EXTERNAL_CONTENT_URI,
                null,
                Playlists._ID + " = ?",
                new String[] { String.valueOf(id) },
                Playlists.DATE_ADDED + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                CursorWrapper cursorWrapper = new CursorWrapper(cursor);
                return getPlayListFromCursor(cursorWrapper);
            }
            return null;
        }
    }

    private StoragePlayListItem getPlayListItemFromCursor(CursorWrapper cursorWrapper) {
        long itemId = cursorWrapper.getLong(Playlists.Members._ID);
        long audioId = cursorWrapper.getLong(Playlists.Members.AUDIO_ID);
        return new StoragePlayListItem(itemId, audioId);
    }

    @Nullable
    private StoragePlayList getPlayListFromCursor(CursorWrapper cursorWrapper) {
        long id = cursorWrapper.getLong(Playlists._ID);
        String name = cursorWrapper.getString(Playlists.NAME);
        if (name == null) {
            return null;
        }
        long dateAdded = cursorWrapper.getLong(Playlists.DATE_ADDED);
        long dateModified = cursorWrapper.getLong(Playlists.DATE_MODIFIED);

        return new StoragePlayList(id,
                name,
                new Date(dateAdded * 1000L),
                new Date(dateModified * 1000L));
    }
}
