package com.github.anrimian.musicplayer.data.database.converters;

import androidx.room.TypeConverter;

import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;

import javax.annotation.Nullable;

public class EnumConverter {

    @TypeConverter
    public CorruptionType toEnum(@Nullable String name) {
        if (name == null) {
            return null;
        }
        return CorruptionType.valueOf(name);
    }

    @TypeConverter
    public String toName(@Nullable CorruptionType value) {
        if (value == null) {
            return null;
        }
        return value.name();
    }

    @TypeConverter
    public OrderType toOrderType(@Nullable String name) {
        if (name == null) {
            return null;
        }
        return OrderType.valueOf(name);
    }

    @TypeConverter
    public String toName(@Nullable OrderType value) {
        if (value == null) {
            return null;
        }
        return value.name();
    }
}
