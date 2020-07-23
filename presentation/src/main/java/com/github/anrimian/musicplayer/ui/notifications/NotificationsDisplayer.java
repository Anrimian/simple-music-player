package com.github.anrimian.musicplayer.ui.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting;
import com.github.anrimian.musicplayer.infrastructure.service.music.CompositionSourceModelHelper;
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService;
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader;
import com.github.anrimian.musicplayer.ui.main.MainActivity;
import com.github.anrimian.musicplayer.ui.notifications.builder.AppNotificationBuilder;

import javax.annotation.Nonnull;

import static androidx.core.content.ContextCompat.getColor;
import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_REPEAT_MODE;
import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.Constants.Arguments.OPEN_PLAY_QUEUE_ARG;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getRepeatModeIcon;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getRepeatModeText;


/**
 * Created on 05.11.2017.
 */

public class NotificationsDisplayer {

    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    private static final int ERROR_NOTIFICATION_ID = 2;

    public static final String FOREGROUND_CHANNEL_ID = "0";
    private static final String ERROR_CHANNEL_ID = "1";

    private final Context context;
    private final NotificationManager notificationManager;
    private final AppNotificationBuilder notificationBuilder;
    private final CoverImageLoader coverImageLoader;

    private Bitmap currentNotificationBitmap;
    private Runnable cancellationRunnable;

    public NotificationsDisplayer(Context context,
                                  AppNotificationBuilder notificationBuilder,
                                  CoverImageLoader coverImageLoader) {
        this.context = context;
        this.notificationBuilder = notificationBuilder;
        this.coverImageLoader = coverImageLoader;

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID,
                    getString(R.string.foreground_channel_description),
                    NotificationManager.IMPORTANCE_LOW);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);

            NotificationChannel errorChannel = new NotificationChannel(ERROR_CHANNEL_ID,
                    getString(R.string.error_channel_description),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(errorChannel);
        }
    }

    public void showErrorNotification(@StringRes int errorMessageId) {
        notificationManager.notify(ERROR_NOTIFICATION_ID, getErrorNotification(errorMessageId));
    }

    public void startForegroundErrorNotification(Service service,
                                                 @StringRes int errorMessageId) {
        Notification notification = getErrorNotification(errorMessageId);
        service.startForeground(ERROR_NOTIFICATION_ID, notification);
    }

    public Notification getErrorNotification(@StringRes int errorMessageId) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(context, ERROR_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.playing_error))
                .setContentText(context.getString(errorMessageId))
                .setColor(getColor(context, R.color.default_notification_color))
                .setSmallIcon(R.drawable.ic_music_box)
                .setVibrate(new long[]{100L, 100L})
                .setContentIntent(pIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
    }

    public void removeErrorNotification() {
        notificationManager.cancel(ERROR_NOTIFICATION_ID);
    }

    public void startForegroundNotification(Service service,
                                            boolean play,
                                            @Nullable CompositionSource source,
                                            MediaSessionCompat mediaSession,
                                            int repeatMode,
                                            @Nullable MusicNotificationSetting notificationSetting,
                                            boolean reloadCover) {
        Notification notification = getDefaultMusicNotification(play,
                source,
                mediaSession,
                repeatMode,
                notificationSetting)
                .build();
        service.startForeground(FOREGROUND_NOTIFICATION_ID, notification);

        if (reloadCover) {
            showMusicNotificationWithCover(play, source, mediaSession, repeatMode, notificationSetting);
        }
    }

    public void updateForegroundNotification(boolean play,
                                             @Nullable CompositionSource source,
                                             MediaSessionCompat mediaSession,
                                             int repeatMode,
                                             MusicNotificationSetting notificationSetting,
                                             boolean reloadCover) {
        if (!isNotificationVisible(notificationManager, FOREGROUND_NOTIFICATION_ID)) {
            return;
        }

        Notification notification = getDefaultMusicNotification(play,
                source,
                mediaSession,
                repeatMode,
                notificationSetting)
                .build();
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);

        if (reloadCover) {
            showMusicNotificationWithCover(play, source, mediaSession, repeatMode, notificationSetting);
        }
    }

    public void cancelCoverLoadingForForegroundNotification() {
        if (cancellationRunnable != null) {
            cancellationRunnable.run();
        }
    }

    private void showMusicNotificationWithCover(boolean play,
                                                @Nullable CompositionSource source,
                                                MediaSessionCompat mediaSession,
                                                int repeatMode,
                                                MusicNotificationSetting notificationSetting) {
        cancelCoverLoadingForForegroundNotification();

        if (source == null) {
            return;
        }

        boolean showCovers = false;
        if (notificationSetting != null) {
            showCovers = notificationSetting.isShowCovers();
        }
        if (!showCovers) {
            return;
        }

        cancellationRunnable = CompositionSourceModelHelper.getCompositionSourceCover(
                source,
                bitmap -> {
                    NotificationCompat.Builder builder = getDefaultMusicNotification(play,
                            source,
                            mediaSession,
                            repeatMode,
                            notificationSetting);

                    builder.setLargeIcon(bitmap);
                    currentNotificationBitmap = bitmap;
                    notificationManager.notify(FOREGROUND_NOTIFICATION_ID, builder.build());
                },
                () -> currentNotificationBitmap,
                coverImageLoader);
    }

    private NotificationCompat.Builder getDefaultMusicNotification(boolean play,
                                                                   @Nullable CompositionSource source,
                                                                   MediaSessionCompat mediaSession,
                                                                   int repeatMode,
                                                                   @Nullable MusicNotificationSetting notificationSetting) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(OPEN_PLAY_QUEUE_ARG, true);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        boolean coloredNotification = false;
        boolean showCovers = false;
        if (notificationSetting != null) {
            coloredNotification = notificationSetting.isColoredNotification();
            showCovers = notificationSetting.isShowCovers();
        }

        NotificationCompat.Builder builder = notificationBuilder.buildMusicNotification(context)
                .setColorized(coloredNotification)
                .setSmallIcon(R.drawable.ic_music_box)
                .setContentIntent(pIntent)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (source != null) {
            formatCompositionSource(source, builder);
            setActionsToNotification(play, source, mediaSession, repeatMode, builder);
        }

        if (showCovers) {
            Bitmap bitmap = currentNotificationBitmap;
            if (bitmap == null || bitmap.isRecycled()) {
                bitmap = coverImageLoader.getDefaultNotificationBitmap();
            }
            builder.setLargeIcon(bitmap);
        }

        return builder;
    }

    private void setActionsToNotification(boolean play,
                                          @Nonnull CompositionSource source,
                                          MediaSessionCompat mediaSession,
                                          int repeatMode,
                                          NotificationCompat.Builder builder) {
        int requestCode = play? PAUSE : PLAY;
        Intent intentPlayPause = new Intent(context, MusicService.class);
        intentPlayPause.putExtra(REQUEST_CODE, requestCode);
        PendingIntent pIntentPlayPause = PendingIntent.getService(context,
                requestCode,
                intentPlayPause,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(
                play? R.drawable.ic_pause: R.drawable.ic_play,
                getString(play? R.string.pause: R.string.play),
                pIntentPlayPause);

        androidx.media.app.NotificationCompat.MediaStyle style = new androidx.media.app.NotificationCompat.MediaStyle();
        style.setMediaSession(mediaSession.getSessionToken());

        if (source instanceof LibraryCompositionSource) {
            Intent intentSkipToPrevious = new Intent(context, MusicService.class);
            intentSkipToPrevious.putExtra(REQUEST_CODE, SKIP_TO_PREVIOUS);
            PendingIntent pIntentSkipToPrevious = PendingIntent.getService(context,
                    SKIP_TO_PREVIOUS,
                    intentSkipToPrevious,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Intent intentSkipToNext = new Intent(context, MusicService.class);
            intentSkipToNext.putExtra(REQUEST_CODE, SKIP_TO_NEXT);
            PendingIntent pIntentSkipToNext = PendingIntent.getService(context,
                    SKIP_TO_NEXT,
                    intentSkipToNext,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            style.setShowActionsInCompactView(0, 1, 2);

            builder.addAction(R.drawable.ic_skip_previous, getString(R.string.previous_track), pIntentSkipToPrevious)
                    .addAction(playPauseAction)
                    .addAction(R.drawable.ic_skip_next, getString(R.string.next_track), pIntentSkipToNext);
        }
        if (source instanceof UriCompositionSource) {
            Intent intentChangeRepeatMode = new Intent(context, MusicService.class);
            intentChangeRepeatMode.putExtra(REQUEST_CODE, CHANGE_REPEAT_MODE);

            PendingIntent pIntentChangeRepeatMode = PendingIntent.getService(context,
                    CHANGE_REPEAT_MODE,
                    intentChangeRepeatMode,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Action changeRepeatModeAction = new NotificationCompat.Action(
                    getRepeatModeIcon(repeatMode),
                    getString(getRepeatModeText(repeatMode)),
                    pIntentChangeRepeatMode);


            style.setShowActionsInCompactView(0, 1);

            builder.addAction(changeRepeatModeAction)
                    .addAction(playPauseAction);
        }

        builder.setStyle(style);
    }

    private void formatCompositionSource(@Nonnull CompositionSource source,
                                         NotificationCompat.Builder builder) {
        if (source instanceof LibraryCompositionSource) {
            Composition composition = ((LibraryCompositionSource) source).getComposition();
            builder.setContentTitle(formatCompositionName(composition))
                    .setContentText(formatCompositionAuthor(composition, context));
        }
        if (source instanceof UriCompositionSource) {
            UriCompositionSource uriSource = (UriCompositionSource) source;
            builder.setContentTitle(formatCompositionName(uriSource.getTitle(), uriSource.getDisplayName()))
                    .setContentText(formatAuthor(uriSource.getArtist(), context));
        }
    }

    private boolean isNotificationVisible(NotificationManager notificationManager,
                                          int notificationId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                if (notification.getId() == notificationId) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }
}
