package com.github.anrimian.musicplayer.di.app.play_list;

import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.PlayListPresenter;
import com.github.anrimian.musicplayer.ui.playlist_screens.rename.RenamePlayListPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class PlayListModule {

    private final long playListId;

    public PlayListModule(long playListId) {
        this.playListId = playListId;
    }

    @Provides
    @Nonnull
    PlayListPresenter playListsPresenter(LibraryPlayerInteractor musicPlayerInteractor,
                                         PlayListsInteractor playListsInteractor,
                                         DisplaySettingsInteractor displaySettingsInteractor,
                                         @Named(UI_SCHEDULER) Scheduler uiSchedule,
                                         ErrorParser errorParser) {
        return new PlayListPresenter(playListId,
                musicPlayerInteractor,
                playListsInteractor,
                displaySettingsInteractor,
                errorParser,
                uiSchedule);
    }

    @Provides
    @Nonnull
    RenamePlayListPresenter changePlayListPresenter(PlayListsInteractor playListsInteractor,
                                                    @Named(UI_SCHEDULER) Scheduler uiSchedule,
                                                    ErrorParser errorParser) {
        return new RenamePlayListPresenter(playListId,
                playListsInteractor,
                uiSchedule,
                errorParser);
    }
}
