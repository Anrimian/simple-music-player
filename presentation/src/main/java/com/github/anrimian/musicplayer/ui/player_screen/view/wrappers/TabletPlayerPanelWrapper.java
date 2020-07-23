package com.github.anrimian.musicplayer.ui.player_screen.view.wrappers;

import android.view.View;

import com.github.anrimian.musicplayer.domain.utils.functions.Callback;



public class TabletPlayerPanelWrapper implements PlayerPanelWrapper {

    public TabletPlayerPanelWrapper(View view, Callback<Boolean> bottomSheetStateListener) {
        bottomSheetStateListener.call(false);
    }

    @Override
    public boolean isBottomPanelExpanded() {
        return false;
    }

    @Override
    public void collapseBottomPanelSmoothly() {

    }

    @Override
    public void collapseBottomPanel() {

    }

    @Override
    public void expandBottomPanel() {

    }

    @Override
    public void openPlayQueue() {

    }
}
