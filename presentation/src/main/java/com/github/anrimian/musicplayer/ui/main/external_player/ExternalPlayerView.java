package com.github.anrimian.musicplayer.ui.main.external_player;

import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import javax.annotation.Nullable;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface ExternalPlayerView extends MvpView {

    String PLAYER_STATE = "player_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void showStopState();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PLAYER_STATE)
    void showPlayState();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void displayComposition(UriCompositionSource source);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showTrackState(long currentPosition, long duration);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showRepeatMode(int mode);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showPlayErrorEvent(@Nullable ErrorType errorType);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showKeepPlayerInBackground(boolean externalPlayerKeepInBackground);
}
