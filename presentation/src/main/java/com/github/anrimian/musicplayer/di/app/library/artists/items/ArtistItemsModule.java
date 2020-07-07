package com.github.anrimian.musicplayer.di.app.library.artists.items;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryArtistsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor;
import com.github.anrimian.musicplayer.domain.interactors.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.interactors.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.artists.items.ArtistItemsPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class ArtistItemsModule {

    private long id;

    public ArtistItemsModule(long id) {
        this.id = id;
    }

    @Provides
    @Nonnull
    ArtistItemsPresenter itemsPresenter(LibraryArtistsInteractor interactor,
                                        PlayListsInteractor playListsInteractor,
                                        LibraryPlayerInteractor playerInteractor,
                                        DisplaySettingsInteractor displaySettingsInteractor,
                                        ErrorParser errorParser,
                                        @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new ArtistItemsPresenter(id,
                interactor,
                playListsInteractor,
                playerInteractor,
                displaySettingsInteractor,
                errorParser,
                uiScheduler);
    }
}
