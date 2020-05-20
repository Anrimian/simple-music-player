package com.github.anrimian.musicplayer.data.utils.db;

import android.database.Cursor;

import androidx.annotation.Nullable;

public class CursorWrapper {

    private final Cursor cursor;

    public CursorWrapper(Cursor cursor) {
        this.cursor = cursor;
    }

    @Nullable
    public String getString(String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex < 0 || cursor.isNull(columnIndex)) {
            return null;
        } else {
            return cursor.getString(columnIndex);
        }
    }

    public long getLong(String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex < 0 || cursor.isNull(columnIndex)) {
            return 0;
        } else {
            return cursor.getLong(columnIndex);
        }
    }

    public int getInt(String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex < 0 || cursor.isNull(columnIndex)) {
            return 0;
        } else {
            return cursor.getInt(columnIndex);
        }
    }

    public boolean getBoolean(String columnName) {
        return getInt(columnName) == 1;
    }
}
