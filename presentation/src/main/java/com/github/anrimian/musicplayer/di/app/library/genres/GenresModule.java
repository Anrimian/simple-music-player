package com.github.anrimian.musicplayer.di.app.library.genres;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryGenresInteractor;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.genres.list.GenresListPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class GenresModule {

    @Provides
    @Nonnull
    LibraryGenresInteractor libraryGenresInteractor(EditorRepository editorRepository,
                                                    LibraryRepository repository,
                                                    SettingsRepository settingsRepository) {
        return new LibraryGenresInteractor(editorRepository, repository, settingsRepository);
    }

    @Provides
    @Nonnull
    GenresListPresenter genreListPresenter(LibraryGenresInteractor interactor,
                                           ErrorParser errorParser,
                                           @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new GenresListPresenter(interactor, errorParser, uiScheduler);
    }
}
