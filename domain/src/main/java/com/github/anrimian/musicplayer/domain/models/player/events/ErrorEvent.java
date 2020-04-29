package com.github.anrimian.musicplayer.domain.models.player.events;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;

import javax.annotation.Nonnull;

public class ErrorEvent implements PlayerEvent {

    private final ErrorType errorType;
    private final Composition composition;

    public ErrorEvent(@Nonnull ErrorType errorType, @Nonnull Composition composition) {
        this.errorType = errorType;
        this.composition = composition;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ErrorEvent that = (ErrorEvent) o;

        return errorType == that.errorType;
    }

    @Override
    public int hashCode() {
        return errorType.hashCode();
    }

    public Composition getComposition() {
        return composition;
    }

    @Override
    public String toString() {
        return "ErrorEvent{" +
                "errorType=" + errorType +
                ", composition=" + composition +
                '}';
    }
}
