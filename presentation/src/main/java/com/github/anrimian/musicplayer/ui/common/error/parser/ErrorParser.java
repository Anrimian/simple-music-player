package com.github.anrimian.musicplayer.ui.common.error.parser;

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

/**
 * Created on 29.10.2017.
 */

public interface ErrorParser {

    ErrorCommand parseError(Throwable throwable);

    void logError(Throwable throwable);
}
