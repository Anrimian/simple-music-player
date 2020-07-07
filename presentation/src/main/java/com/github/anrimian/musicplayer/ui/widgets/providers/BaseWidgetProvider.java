package com.github.anrimian.musicplayer.ui.widgets.providers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.LayoutRes;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.ui.main.MainActivity;
import com.github.anrimian.musicplayer.ui.widgets.WidgetActionsReceiver;
import com.github.anrimian.musicplayer.ui.widgets.WidgetDataHolder;
import com.github.anrimian.musicplayer.utils.Permissions;

import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_AUTHOR_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_NAME_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_UPDATE_TIME_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.OPEN_PLAY_QUEUE_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.QUEUE_SIZE_ARG;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;
import static com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater.ACTION_UPDATE_COMPOSITION;
import static com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater.ACTION_UPDATE_QUEUE;
import static com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater.WIDGET_ACTION;


public abstract class BaseWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        String compositionName;
        String compositionAuthor;
        long compositionId;
        long compositionUpdateTime;
        if (ACTION_UPDATE_COMPOSITION.equals(intent.getStringExtra(WIDGET_ACTION))) {
            compositionName = intent.getStringExtra(COMPOSITION_NAME_ARG);
            compositionAuthor = intent.getStringExtra(COMPOSITION_AUTHOR_ARG);
            compositionId = intent.getLongExtra(COMPOSITION_ID_ARG, 0);
            compositionUpdateTime = intent.getLongExtra(COMPOSITION_UPDATE_TIME_ARG, 0);
        } else {
            compositionName = WidgetDataHolder.getCompositionName(context);
            compositionAuthor = WidgetDataHolder.getCompositionAuthor(context);
            compositionId = WidgetDataHolder.getCompositionId(context);
            compositionUpdateTime = WidgetDataHolder.getCompositionUpdateTime(context);
        }

        int queueSize;
        if (ACTION_UPDATE_QUEUE.equals(intent.getStringExtra(WIDGET_ACTION))) {
            queueSize = intent.getIntExtra(QUEUE_SIZE_ARG, 0);
        } else {
            queueSize = WidgetDataHolder.getCurrentQueueSize(context);
        }

        AppComponent appComponent = Components.getAppComponent();
        boolean play = false;
        boolean randomPlayModeEnabled = false;
        int repeatMode = RepeatMode.NONE;
        if (Permissions.hasFilePermission(context)) {
            LibraryPlayerInteractor musicPlayerInteractor = appComponent.musicPlayerInteractor();
            play = musicPlayerInteractor.getPlayerState() == PlayerState.PLAY;
            randomPlayModeEnabled = musicPlayerInteractor.isRandomPlayingEnabled();
            repeatMode = musicPlayerInteractor.getRepeatMode();
        }

        boolean showCovers = appComponent.displaySettingsInteractor().isCoversEnabled();

        boolean enabled = true;
        if (isEmpty(compositionName)) {
            compositionName = context.getString(R.string.no_current_composition);
            compositionAuthor = null;
            enabled = false;
        }

        int[] ids = appWidgetManager.getAppWidgetIds(thisAppWidget);
        for (int widgetId : ids) {
            updateWidget(context,
                    appWidgetManager,
                    widgetId,
                    play,
                    compositionName,
                    compositionAuthor,
                    compositionId,
                    compositionUpdateTime,
                    queueSize,
                    enabled,
                    showCovers,
                    randomPlayModeEnabled,
                    repeatMode);
        }
    }

    protected void applyViewLogic(RemoteViews widgetView,
                                  AppWidgetManager appWidgetManager,
                                  int widgetId,
                                  Context context,
                                  boolean play,
                                  String compositionName,
                                  String compositionAuthor,
                                  long compositionId,
                                  long compositionUpdateTime,
                                  int queueSize,
                                  boolean enabled,
                                  boolean showCovers,
                                  boolean randomPlayModeEnabled,
                                  int repeatMode) {
        widgetView.setBoolean(R.id.iv_skip_to_previous, "setEnabled", enabled);
        widgetView.setBoolean(R.id.iv_play_pause, "setEnabled", enabled);
        widgetView.setBoolean(R.id.iv_skip_to_next, "setEnabled", enabled && queueSize > 1);
        widgetView.setTextViewText(R.id.tv_composition, compositionName);
        widgetView.setTextViewText(R.id.tv_composition_author, compositionAuthor);

        widgetView.setImageViewResource(R.id.iv_play_pause, play? R.drawable.ic_pause: R.drawable.ic_play);

        int requestCode = play? PAUSE : PLAY;
        Intent intentPlayPause = new Intent(context, WidgetActionsReceiver.class);
        intentPlayPause.putExtra(REQUEST_CODE, requestCode);
        PendingIntent pIntentPlayPause = PendingIntent.getBroadcast(context,
                requestCode,
                intentPlayPause,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.iv_play_pause, pIntentPlayPause);

        Intent intentSkipToPrevious = new Intent(context, WidgetActionsReceiver.class);
        intentSkipToPrevious.putExtra(REQUEST_CODE, SKIP_TO_PREVIOUS);
        PendingIntent pIntentSkipToPrevious = PendingIntent.getBroadcast(context,
                SKIP_TO_PREVIOUS,
                intentSkipToPrevious,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.iv_skip_to_previous, pIntentSkipToPrevious);

        Intent intentSkipToNext = new Intent(context, WidgetActionsReceiver.class);
        intentSkipToNext.putExtra(REQUEST_CODE, SKIP_TO_NEXT);
        PendingIntent pIntentSkipToNext = PendingIntent.getBroadcast(context,
                SKIP_TO_NEXT,
                intentSkipToNext,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.iv_skip_to_next, pIntentSkipToNext);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(OPEN_PLAY_QUEUE_ARG, enabled);
        PendingIntent pIntent = PendingIntent.getActivity(context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.widget_view, pIntent);
    }

    private void updateWidget(Context context,
                              AppWidgetManager appWidgetManager,
                              int widgetId,
                              boolean play,
                              String compositionName,
                              String compositionAuthor,
                              long compositionId,
                              long compositionUpdateTime,
                              int queueSize,
                              boolean enabled,
                              boolean showCovers,
                              boolean randomPlayModeEnabled,
                              int repeatMode) {
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), getRemoteViewId());

        applyViewLogic(widgetView,
                appWidgetManager,
                widgetId,
                context,
                play,
                compositionName,
                compositionAuthor,
                compositionId,
                compositionUpdateTime,
                queueSize,
                enabled,
                showCovers,
                randomPlayModeEnabled,
                repeatMode);

        appWidgetManager.updateAppWidget(widgetId, widgetView);
    }

    @LayoutRes
    protected abstract int getRemoteViewId();
}
