package com.github.anrimian.musicplayer.di.app.editor.album;

import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.editor.album.AlbumEditorPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class AlbumEditorModule {

    private final long albumId;

    public AlbumEditorModule(long albumId) {
        this.albumId = albumId;
    }

    @Provides
    @Nonnull
    AlbumEditorPresenter compositionEditorPresenter(EditorInteractor interactor,
                                                    @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                                    ErrorParser errorParser) {
        return new AlbumEditorPresenter(albumId, interactor, uiScheduler, errorParser);
    }

}
