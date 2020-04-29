package com.github.anrimian.musicplayer.di.app.editor.composition;

import com.github.anrimian.musicplayer.domain.interactors.editor.EditorInteractor;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.editor.composition.CompositionEditorPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class CompositionEditorModule {

    private final long compositionId;

    public CompositionEditorModule(long compositionId) {
        this.compositionId = compositionId;
    }

    @Provides
    @Nonnull
    CompositionEditorPresenter compositionEditorPresenter(EditorInteractor interactor,
                                                          @Named(UI_SCHEDULER) Scheduler uiScheduler,
                                                          ErrorParser errorParser) {
        return new CompositionEditorPresenter(compositionId, interactor, uiScheduler, errorParser);
    }
}
