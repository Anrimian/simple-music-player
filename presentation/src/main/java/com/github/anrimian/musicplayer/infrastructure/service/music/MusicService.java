package com.github.anrimian.musicplayer.infrastructure.service.music;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.media.session.MediaButtonReceiver;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting;
import com.github.anrimian.musicplayer.domain.utils.functions.Optional;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController;
import com.github.anrimian.musicplayer.ui.main.MainActivity;
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer;
import com.github.anrimian.musicplayer.utils.Permissions;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SEEK_TO;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP;
import static android.support.v4.media.session.PlaybackStateCompat.Builder;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_GROUP;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_INVALID;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE;
import static android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE;
import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_REPEAT_MODE;
import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;
import static com.github.anrimian.musicplayer.infrastructure.service.music.models.mappers.PlayerStateMapper.toMediaState;

/**
 * Created on 03.11.2017.
 */

public class MusicService extends Service {

    public static final String REQUEST_CODE = "request_code";
    public static final String START_FOREGROUND_SIGNAL = "start_foreground_signal";

    private NotificationsDisplayer notificationsDisplayer;

    @Inject
    PlayerInteractor playerInteractor;

    @Inject
    MusicServiceInteractor musicServiceInteractor;

    @Inject
    ThemeController themeController;

    @Named(UI_SCHEDULER)
    @Inject
    Scheduler uiScheduler;

    private final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
    private final Builder stateBuilder = new Builder()
            .setActions(ACTION_PLAY
                    | ACTION_STOP
                    | ACTION_PAUSE
                    | ACTION_PLAY_PAUSE
                    | ACTION_SKIP_TO_NEXT
                    | ACTION_SKIP_TO_PREVIOUS
                    | ACTION_SEEK_TO);
    //optimization
    private final ServiceState serviceState = new ServiceState();

    private final MediaSessionCallback mediaSessionCallback = new MediaSessionCallback();
    private final CompositeDisposable serviceDisposable = new CompositeDisposable();

    private MediaSessionCompat mediaSession;

    private PlayerState playerState = PlayerState.PLAY;
    @Nullable
    private CompositionSource currentSource;
    private long trackPosition;
    private int repeatMode = RepeatMode.NONE;
    private MusicNotificationSetting notificationSetting;
    private AppTheme currentAppTheme;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this, getClass().getSimpleName());

        AppComponent appComponent = Components.getAppComponent();
        notificationsDisplayer = appComponent.notificationDisplayer();

        if (!Permissions.hasFilePermission(this)) {
            notificationsDisplayer.startForegroundErrorNotification(this, R.string.no_file_permission);
            stopForeground(true);
            stopSelf();
            return;
        }
        appComponent.inject(this);

        mediaSession.setCallback(mediaSessionCallback);

        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pActivityIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
        mediaSession.setSessionActivity(pActivityIntent);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, this, MediaButtonReceiver.class);
        PendingIntent pMediaButtonIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mediaSession.setMediaButtonReceiver(pMediaButtonIntent);

        //reduce chance to show first notification without info
        currentSource = playerInteractor.getCurrentSource();
        notificationSetting = musicServiceInteractor.getNotificationSettings();

        //we must start foreground in onCreate, strange ANR otherwise
        notificationsDisplayer.startForegroundNotification(this,
                playerState == PlayerState.PLAY,
                currentSource,
                mediaSession,
                repeatMode,
                notificationSetting,
                true);
        //update state that depends on current item and settings to keep it actual
        if (currentSource != null) {
            updateMediaSessionState();
            updateMediaSessionMetadata();
            updateMediaSessionAlbumArt();
        }

        subscribeOnServiceState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int requestCode = intent.getIntExtra(REQUEST_CODE, -1);
        int startForegroundSignal = intent.getIntExtra(START_FOREGROUND_SIGNAL, -1);
        if (startForegroundSignal != -1) {
            notificationsDisplayer.startForegroundNotification(this,
                    playerState == PlayerState.PLAY,
                    currentSource,
                    mediaSession,
                    repeatMode,
                    notificationSetting,
                    false);
        }
        if (requestCode != -1) {
            handleNotificationAction(requestCode);
        } else {
            KeyEvent keyEvent = MediaButtonReceiver.handleIntent(mediaSession, intent);
            if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                handleMediaButtonAction(keyEvent);
            }
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
        serviceDisposable.dispose();
    }

    private void handleMediaButtonAction(@Nonnull KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY) {
            playerInteractor.play();
        }
    }

    private void handleNotificationAction(int requestCode) {
        switch (requestCode) {
            case PLAY: {
                playerInteractor.play();
                break;
            }
            case PAUSE: {
                playerInteractor.pause();
                break;
            }
            case SKIP_TO_NEXT: {
                musicServiceInteractor.skipToNext();
                break;
            }
            case SKIP_TO_PREVIOUS: {
                musicServiceInteractor.skipToPrevious();
                break;
            }
            case CHANGE_REPEAT_MODE: {
                musicServiceInteractor.changeRepeatMode();
                break;
            }
        }
    }

    private void subscribeOnServiceState() {
        serviceDisposable.add(Observable.combineLatest(playerInteractor.getPlayerStateObservable(),
                playerInteractor.getCurrentSourceObservable(),
                playerInteractor.getTrackPositionObservable(),
                musicServiceInteractor.getRepeatModeObservable(),
                musicServiceInteractor.getNotificationSettingObservable(),
                themeController.getAppThemeObservable(),
                serviceState::set)
                .observeOn(uiScheduler)
                .subscribe(this::onServiceStateReceived));
    }

    private void onServiceStateReceived(ServiceState serviceState) {
        CompositionSource newCompositionSource = serviceState.compositionSource.getValue();
        PlayerState newPlayerState = serviceState.playerState;
        long newTrackPosition = serviceState.trackPosition;

        if (newCompositionSource == null || newPlayerState == PlayerState.STOP) {
            currentSource = null;
            trackPosition = 0;
            notificationsDisplayer.cancelCoverLoadingForForegroundNotification();
            mediaSession.setActive(false);
            stopForeground(true);
            stopSelf();
            return;
        }
        if (!mediaSession.isActive()) {
            mediaSession.setActive(true);
        }

        boolean updateNotification = false;
        boolean updateMediaSessionState = false;
        boolean updateMediaSessionMetadata = false;
        boolean updateMediaSessionAlbumArt = false;

        if (this.playerState != serviceState.playerState) {
            this.playerState = serviceState.playerState;
            updateNotification = true;
            updateMediaSessionState = true;

            onPlayerStateChanged(playerState);
        }

        if (!newCompositionSource.equals(currentSource) || !CompositionSourceModelHelper.areSourcesTheSame(currentSource, newCompositionSource)) {
            if (!newCompositionSource.equals(currentSource)) {
                newTrackPosition = CompositionSourceModelHelper.getTrackPosition(newCompositionSource);
                updateMediaSessionAlbumArt = true;
            } else if (!CompositionSourceModelHelper.areSourcesTheSame(currentSource, newCompositionSource)) {
                updateMediaSessionAlbumArt = true;
            }
            this.currentSource = newCompositionSource;
            updateNotification = true;
            updateMediaSessionMetadata = true;
        }

        if (this.trackPosition != newTrackPosition) {
            this.trackPosition = newTrackPosition;
            updateMediaSessionState = true;
        }

        if (this.repeatMode != serviceState.repeatMode) {
            this.repeatMode = serviceState.repeatMode;
            updateMediaSessionState = true;
        }

        MusicNotificationSetting newSettings = serviceState.settings;
        if (!newSettings.equals(this.notificationSetting)) {
            if (notificationSetting.isCoversOnLockScreen() != newSettings.isCoversOnLockScreen()) {
                updateMediaSessionAlbumArt = true;
            }
            if (notificationSetting.isShowCovers() != newSettings.isShowCovers()
                    || notificationSetting.isColoredNotification() != newSettings.isColoredNotification()) {
                updateNotification = true;
            }
            this.notificationSetting = newSettings;
        }
        if (serviceState.appTheme != currentAppTheme) {
            currentAppTheme = serviceState.appTheme;
            updateNotification = true;
        }

        //seekbar values on cover settings change
        if (updateNotification) {
            updateForegroundNotification(updateMediaSessionAlbumArt);
        }
        if (updateMediaSessionState) {
            updateMediaSessionState();
        }
        if (updateMediaSessionMetadata) {
            updateMediaSessionMetadata();
        }
        if (updateMediaSessionAlbumArt) {
            updateMediaSessionAlbumArt();
        }
    }

    private void updateMediaSessionAlbumArt() {
        assert currentSource != null;
        CompositionSourceModelHelper.updateMediaSessionAlbumArt(currentSource,
                metadataBuilder,
                mediaSession,
                notificationSetting.isCoversOnLockScreen());
    }

    private void updateMediaSessionMetadata() {
        assert currentSource != null;
        CompositionSourceModelHelper.updateMediaSessionMetadata(currentSource,
                metadataBuilder,
                mediaSession,
                this);
    }

    private void updateMediaSessionState() {
        stateBuilder.setState(toMediaState(playerState), trackPosition, 1);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void onPlayerStateChanged(PlayerState playerState) {
        switch (playerState) {
            case PAUSE: {
                stopForeground(false);
                break;
            }
        }
    }

    private void updateForegroundNotification(boolean reloadCover) {
        notificationsDisplayer.updateForegroundNotification(
                playerState == PlayerState.PLAY,
                currentSource,
                mediaSession,
                repeatMode,
                notificationSetting,
                reloadCover);
    }

    private static class ServiceState {
        PlayerState playerState;
        Optional<CompositionSource> compositionSource;
        long trackPosition;
        int repeatMode;
        MusicNotificationSetting settings;
        AppTheme appTheme;

        private ServiceState set(PlayerState playerState,
                                 Optional<CompositionSource> compositionSource,
                                 long trackPosition,
                                 int repeatMode,
                                 MusicNotificationSetting settings,
                                 AppTheme appTheme) {
            this.playerState = playerState;
            this.compositionSource = compositionSource;
            this.trackPosition = trackPosition;
            this.repeatMode = repeatMode;
            this.settings = settings;
            this.appTheme = appTheme;
            return this;
        }
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            playerInteractor.playOrPause();
        }

        @Override
        public void onPause() {
            playerInteractor.playOrPause();
        }

        @Override
        public void onStop() {
            playerInteractor.stop();
        }

        @Override
        public void onSkipToNext() {
            musicServiceInteractor.skipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            musicServiceInteractor.skipToPrevious();
        }

        @Override
        public void onSeekTo(long pos) {
            playerInteractor.onSeekFinished(pos);
        }

        //next - test it

        @Override
        public void onSetRepeatMode(int repeatMode) {
            int appRepeatMode;
            switch (repeatMode) {
                case REPEAT_MODE_INVALID:
                case REPEAT_MODE_NONE: {
                    appRepeatMode = RepeatMode.NONE;
                    break;
                }
                case REPEAT_MODE_GROUP:
                case REPEAT_MODE_ALL: {
                    appRepeatMode = RepeatMode.REPEAT_PLAY_LIST;
                    break;
                }
                case REPEAT_MODE_ONE: {
                    appRepeatMode = RepeatMode.REPEAT_COMPOSITION;
                    break;
                }
                default: {
                    appRepeatMode = RepeatMode.NONE;
                }
            }
            musicServiceInteractor.setRepeatMode(appRepeatMode);
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            musicServiceInteractor.setRandomPlayingEnabled(shuffleMode != SHUFFLE_MODE_NONE);
        }

        //next - not implemented

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onPrepare() {
            super.onPrepare();
        }

        @Override
        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
            super.onPrepareFromMediaId(mediaId, extras);
        }

        @Override
        public void onPrepareFromSearch(String query, Bundle extras) {
            super.onPrepareFromSearch(query, extras);
        }

        @Override
        public void onPrepareFromUri(Uri uri, Bundle extras) {
            super.onPrepareFromUri(uri, extras);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            super.onPlayFromSearch(query, extras);
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
        }

        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);
        }

        @Override
        public void onFastForward() {
            super.onFastForward();
        }

        @Override
        public void onRewind() {
            super.onRewind();
        }

        @Override
        public void onSetRating(RatingCompat rating) {
            super.onSetRating(rating);
        }

        @Override
        public void onSetRating(RatingCompat rating, Bundle extras) {
            super.onSetRating(rating, extras);
        }

        @Override
        public void onSetCaptioningEnabled(boolean enabled) {
            super.onSetCaptioningEnabled(enabled);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            super.onAddQueueItem(description);
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description, int index) {
            super.onAddQueueItem(description, index);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            super.onRemoveQueueItem(description);
        }
    }
}
