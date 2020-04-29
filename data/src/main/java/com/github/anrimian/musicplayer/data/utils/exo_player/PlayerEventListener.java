package com.github.anrimian.musicplayer.data.utils.exo_player;

import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;

public class PlayerEventListener implements Player.EventListener {

    private final Runnable onEnded;
    private final Callback<ExoPlaybackException> errorCallback;

    public PlayerEventListener(Runnable onEnded, Callback<ExoPlaybackException> errorCallback) {
        this.onEnded = onEnded;
        this.errorCallback = errorCallback;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        //            case Player.STATE_BUFFERING: {
        //                break;
        //            }
        //            case Player.STATE_IDLE: {
        //                break;
        //            }
        //            case Player.STATE_READY: {
        //                break;
        //            }
        if (playbackState == Player.STATE_ENDED) {
            onEnded.run();
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        errorCallback.call(error);
    }
}
