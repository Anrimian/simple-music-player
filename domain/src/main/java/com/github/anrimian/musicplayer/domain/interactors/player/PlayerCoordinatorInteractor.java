package com.github.anrimian.musicplayer.domain.interactors.player;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.events.PlayerEvent;

import java.util.HashMap;

import io.reactivex.Observable;

public class PlayerCoordinatorInteractor {

    private final PlayerInteractor playerInteractor;

    private PlayerType activePlayerType = PlayerType.LIBRARY;
    private HashMap<PlayerType, CompositionSource> preparedSourcesMap = new HashMap<>();

    public PlayerCoordinatorInteractor(PlayerInteractor playerInteractor) {
        this.playerInteractor = playerInteractor;
    }

    public void startPlaying(CompositionSource compositionSource, PlayerType playerType) {
        applyPlayerType(playerType);
        playerInteractor.startPlaying(compositionSource);
    }

    public void play(PlayerType playerType) {
        applyPlayerType(playerType);
        playerInteractor.play();
    }

    public void setInLoadingState(PlayerType playerType) {
        if (playerType == activePlayerType) {
            playerInteractor.setInLoadingState();
        }
    }

    public void updateSource(CompositionSource source, PlayerType playerType) {
        CompositionSource currentSource = preparedSourcesMap.get(playerType);
        if (source.equals(currentSource)) {
            preparedSourcesMap.put(playerType, source);
        }
        if (playerType == activePlayerType) {
            playerInteractor.updateSource(source);
        }
    }

    public void playOrPause(PlayerType playerType) {
        applyPlayerType(playerType);
        playerInteractor.playOrPause();
    }

    public void stop(PlayerType playerType) {
        if (playerType == activePlayerType) {
            playerInteractor.stop();
        }
    }

    public void pause(PlayerType playerType) {
        if (playerType == activePlayerType) {
            playerInteractor.pause();
        }
    }

    public void prepareToPlay(CompositionSource compositionSource, PlayerType playerType) {
        preparedSourcesMap.put(playerType, compositionSource);
        if (playerType == activePlayerType) {
            playerInteractor.prepareToPlay(compositionSource);
        }
    }

    public void onSeekStarted(PlayerType playerType) {
        if (activePlayerType == playerType) {
            playerInteractor.onSeekStarted();
        }
    }

    public boolean onSeekFinished(long position, PlayerType playerType) {
        if (activePlayerType == playerType) {
            playerInteractor.onSeekFinished(position);
            return true;
        } else {
            CompositionSource source = preparedSourcesMap.get(playerType);
            if (source != null) {
                applyPositionChange(source, position);
            }
            return false;
        }
    }

    public long getActualTrackPosition(PlayerType playerType) {
        return isPlayerTypeActive(playerType)? playerInteractor.getTrackPosition(): -1;
    }

    public Observable<PlayerEvent> getPlayerEventsObservable(PlayerType playerType) {
        return playerInteractor.getPlayerEventsObservable()
                .filter(o -> isPlayerTypeActive(playerType));
    }

    public Observable<Long> getTrackPositionObservable(PlayerType playerType) {
        return playerInteractor.getTrackPositionObservable()
                .filter(o -> isPlayerTypeActive(playerType));
    }

    public Observable<PlayerState> getPlayerStateObservable(PlayerType playerType) {
        return playerInteractor.getPlayerStateObservable()
                .map(state -> isPlayerTypeActive(playerType)? state : PlayerState.PAUSE);
    }

    public boolean isPlayerTypeActive(PlayerType playerType) {
        return activePlayerType == playerType;
    }

    public PlayerState getPlayerState(PlayerType playerType) {
        return isPlayerTypeActive(playerType)? playerInteractor.getPlayerState() : PlayerState.PAUSE;
    }

    private void applyPlayerType(PlayerType playerType) {
        if (activePlayerType != playerType) {
            playerInteractor.pause();
            CompositionSource source = preparedSourcesMap.get(playerType);
            if (source != null) {
                playerInteractor.prepareToPlay(source);
            }
            CompositionSource oldSource = preparedSourcesMap.get(activePlayerType);
            if (oldSource != null) {
                applyPositionChange(oldSource, playerInteractor.getTrackPosition());
            }
            activePlayerType = playerType;
        }
    }

    private void applyPositionChange(CompositionSource source, long position) {
        if (source instanceof LibraryCompositionSource) {
            ((LibraryCompositionSource) source).setTrackPosition(position);
        }
    }
}
