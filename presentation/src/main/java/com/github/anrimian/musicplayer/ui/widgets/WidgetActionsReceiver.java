package com.github.anrimian.musicplayer.ui.widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.utils.Permissions;

import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_REPEAT_MODE;
import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_SHUFFLE_NODE;
import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.infrastructure.service.music.MusicService.REQUEST_CODE;


public class WidgetActionsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AppComponent appComponent = Components.getAppComponent();
        if (!Permissions.hasFilePermission(context)) {
            appComponent.notificationDisplayer().showErrorNotification(R.string.no_file_permission);
            return;
        }

        int action = intent.getIntExtra(REQUEST_CODE, 0);

        if (action == 0) {
            return;
        }

        LibraryPlayerInteractor interactor = appComponent.musicPlayerInteractor();
        switch (action) {
            case SKIP_TO_PREVIOUS: {
                interactor.skipToPrevious();
                break;
            }
            case SKIP_TO_NEXT: {
                interactor.skipToNext();
                break;
            }
            case PAUSE:
            case PLAY: {
                interactor.playOrPause();
                break;
            }
            case CHANGE_REPEAT_MODE: {
                interactor.changeRepeatMode();
                break;
            }
            case CHANGE_SHUFFLE_NODE: {
                interactor.setRandomPlayingEnabled(!interactor.isRandomPlayingEnabled());
            }
        }
    }
}
