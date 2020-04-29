package com.github.anrimian.musicplayer.domain.models.player;

/**
 * Created on 02.11.2017.
 */

public enum PlayerState {
    IDLE,
    PLAY,
    PAUSE,
    STOP,
    LOADING,
    PAUSED_EXTERNALLY {
        @Override
        public PlayerState toBaseState() {
            return PAUSE;
        }
    },
    PAUSED_PREPARE_ERROR {
        @Override
        public PlayerState toBaseState() {
            return PAUSE;
        }
    };

    public PlayerState toBaseState() {
        return this;
    }
}
