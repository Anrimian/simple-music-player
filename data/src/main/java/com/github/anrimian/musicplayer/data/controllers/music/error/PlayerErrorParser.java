package com.github.anrimian.musicplayer.data.controllers.music.error;

import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;

public interface PlayerErrorParser {

    ErrorType getErrorType(Throwable throwable);
}
