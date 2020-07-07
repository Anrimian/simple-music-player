package com.github.anrimian.musicplayer.ui.editor.composition;

import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface CompositionEditorView extends MvpView {

    String DISPLAY_COMPOSITION_STATE = "display_composition_state";
    String CHANGE_COVER_STATE = "change_cover_state";

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = DISPLAY_COMPOSITION_STATE)
    void showCompositionLoadingError(ErrorCommand errorCommand);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = DISPLAY_COMPOSITION_STATE)
    void showComposition(FullComposition composition);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CHANGE_COVER_STATE)
    void showChangeCoverProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = CHANGE_COVER_STATE)
    void hideChangeCoverProgress();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showCheckTagsErrorMessage(ErrorCommand errorCommand);

    @StateStrategyType(SkipStrategy.class)
    void showEnterAuthorDialog(FullComposition composition, String[] hints);

    @StateStrategyType(SkipStrategy.class)
    void showEnterTitleDialog(FullComposition composition);

    @StateStrategyType(SkipStrategy.class)
    void showEnterFileNameDialog(FullComposition composition);

    @StateStrategyType(SkipStrategy.class)
    void copyFileNameText(String filePath);

    @StateStrategyType(SkipStrategy.class)
    void showEnterAlbumDialog(FullComposition composition, String[] hints);

    @StateStrategyType(SkipStrategy.class)
    void showEnterAlbumArtistDialog(FullComposition composition, String[] hints);

    @StateStrategyType(SkipStrategy.class)
    void showEnterLyricsDialog(FullComposition composition);

    @StateStrategyType(SkipStrategy.class)
    void showAddGenreDialog(String[] genres);

    @StateStrategyType(SkipStrategy.class)
    void showEditGenreDialog(ShortGenre shortGenre, String[] genres);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showGenres(List<ShortGenre> shortGenres);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showRemovedGenreMessage(ShortGenre genre);

    @StateStrategyType(SkipStrategy.class)
    void showCoverActionsDialog();

    @StateStrategyType(SkipStrategy.class)
    void showSelectImageFromGalleryScreen();
}
