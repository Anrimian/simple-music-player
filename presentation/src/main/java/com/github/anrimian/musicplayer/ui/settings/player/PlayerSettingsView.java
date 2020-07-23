package com.github.anrimian.musicplayer.ui.settings.player;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface PlayerSettingsView extends MvpView {

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showDecreaseVolumeOnAudioFocusLossEnabled(boolean checked);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showSelectedEqualizerType(int type);
}
