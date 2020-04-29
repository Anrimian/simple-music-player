package com.github.anrimian.musicplayer.ui.widgets;

import android.content.Context;
import android.content.Intent;

import com.github.anrimian.musicplayer.domain.interactors.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_AUTHOR_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_NAME_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.PLAY_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.QUEUE_SIZE_ARG;
import static com.github.anrimian.musicplayer.domain.Constants.TRIGGER;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;

public class WidgetUpdater {

    public static final String WIDGET_ACTION = "widget_action";
    public static final String ACTION_UPDATE_COMPOSITION = "action_update_composition";
    public static final String ACTION_UPDATE_QUEUE = "action_update_queue";

    private final Context context;
    private final MusicPlayerInteractor musicPlayerInteractor;
    private final DisplaySettingsInteractor displaySettingsInteractor;

    private final CompositeDisposable updateDisposable = new CompositeDisposable();

    public WidgetUpdater(Context context,
                         MusicPlayerInteractor musicPlayerInteractor,
                         DisplaySettingsInteractor displaySettingsInteractor) {
        this.context = context;
        this.musicPlayerInteractor = musicPlayerInteractor;
        this.displaySettingsInteractor = displaySettingsInteractor;
    }

    public void start() {
        if (updateDisposable.size() > 0) {
            return;
        }
        updateDisposable.add(musicPlayerInteractor
                .getCurrentQueueItemObservable()
                .subscribe(this::onCurrentCompositionReceived));

        updateDisposable.add(musicPlayerInteractor
                .getPlayQueueObservable()
                .subscribe(this::onPlayQueueReceived));

        updateDisposable.add(musicPlayerInteractor
                .getPlayerStateObservable()
                .subscribe(this::onPlayStateReceived));

        updateDisposable.add(displaySettingsInteractor
                .getCoversEnabledObservable()
                .subscribe(this::onDisplaySettingsChanged));

        updateDisposable.add(Observable.combineLatest(musicPlayerInteractor.getRepeatModeObservable(),
                musicPlayerInteractor.getRandomPlayingObservable(),
                (o1, o2) -> TRIGGER)
                .subscribe(o -> updateWidgets()));
    }

    private void onDisplaySettingsChanged(boolean isCoversEnabled) {
        updateWidgets();
    }

    private void onPlayQueueReceived(List<PlayQueueItem> playQueueItems) {
        WidgetDataHolder.setCurrentQueueSize(context, playQueueItems.size());

        updateWidgets(intent -> {
            intent.putExtra(WIDGET_ACTION, ACTION_UPDATE_QUEUE);
            intent.putExtra(QUEUE_SIZE_ARG, playQueueItems.size());
        });
    }

    private void onCurrentCompositionReceived(PlayQueueEvent playQueueEvent) {
        String compositionName = null;
        String compositionAuthor = null;
        long compositionId = 0;
        PlayQueueItem item = playQueueEvent.getPlayQueueItem();
        if (item != null) {
            compositionName = formatCompositionName(item.getComposition());
            compositionAuthor = formatCompositionAuthor(item.getComposition(), context).toString();
            compositionId = item.getComposition().getId();
        }
        updateComposition(compositionName, compositionAuthor, compositionId);
    }

    private void updateComposition(String compositionName,
                                   String compositionAuthor,
                                   long compositionId) {
        WidgetDataHolder.setCompositionName(context, compositionName);
        WidgetDataHolder.setCompositionAuthor(context, compositionAuthor);
        WidgetDataHolder.setCompositionId(context, compositionId);

        updateWidgets(intent -> {
            intent.putExtra(WIDGET_ACTION, ACTION_UPDATE_COMPOSITION);
            intent.putExtra(COMPOSITION_NAME_ARG, compositionName);
            intent.putExtra(COMPOSITION_AUTHOR_ARG, compositionAuthor);
            intent.putExtra(COMPOSITION_ID_ARG, compositionId);
        });
    }

    private void onPlayStateReceived(PlayerState playerState) {
        updateWidgets(intent -> intent.putExtra(PLAY_ARG, playerState == PlayerState.PLAY));
    }

    private void updateWidgets() {
        updateWidgets(null);
    }

    private void updateWidgets(Callback<Intent> intentCallback) {
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
        intent.setPackage(context.getPackageName());
        if (intentCallback != null) {
            intentCallback.call(intent);
        }
        context.sendBroadcast(intent);
    }
}
