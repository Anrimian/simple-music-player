package com.github.anrimian.musicplayer.ui.library.folders;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.ui.utils.moxy.ListStateStrategy;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

/**
 * Created on 23.10.2017.
 */

public interface LibraryFoldersView extends MvpView {

    String LIST_STATE = "list_state";
    String FOLDER_STATE = "back_path_button_state";
    String PROGRESS_DIALOG_STATE = "progress_dialog_state";

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptyList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showEmptySearchResult();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showList();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showLoading();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = LIST_STATE)
    void showError(ErrorCommand errorCommand);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = FOLDER_STATE)
    void showFolderInfo(FolderFileSource folder);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = FOLDER_STATE)
    void hideFolderInfo();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PROGRESS_DIALOG_STATE)
    void hideProgressDialog();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PROGRESS_DIALOG_STATE)
    void showMoveProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PROGRESS_DIALOG_STATE)
    void showDeleteProgress();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = PROGRESS_DIALOG_STATE)
    void showRenameProgress();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void goBackToParentFolderScreen();

    @StateStrategyType(ListStateStrategy.class)
    void updateList(List<FileSource> update);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectOrderScreen(Order folderOrder);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectPlayListDialog();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAddingToPlayListError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showSelectPlayListForFolderDialog(FolderFileSource folder);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showConfirmDeleteDialog(List<Composition> compositionsToDelete);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeleteCompositionError(ErrorCommand errorCommand);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showDeleteCompositionMessage(List<Composition> compositionsToDelete);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showConfirmDeleteDialog(FolderFileSource folder);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showSearchMode(boolean show);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void sendCompositions(List<Composition> compositions);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showReceiveCompositionsForSendError(ErrorCommand errorCommand);

    @StateStrategyType(SkipStrategy.class)
    void goToMusicStorageScreen(Long folderId);

    @StateStrategyType(SkipStrategy.class)
    void showCompositionActionDialog(Composition composition);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand errorCommand);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void setDisplayCoversEnabled(boolean isCoversEnabled);

    @StateStrategyType(SkipStrategy.class)
    void showInputFolderNameDialog(FolderFileSource folder);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showSelectionMode(int count);

    @StateStrategyType(SkipStrategy.class)
    void onItemSelected(FileSource item, int position);

    @StateStrategyType(SkipStrategy.class)
    void onItemUnselected(FileSource item, int position);

    @StateStrategyType(SkipStrategy.class)
    void setItemsSelected(boolean selected);

    @StateStrategyType(SkipStrategy.class)
    void updateMoveFilesList();

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showMoveFileMenu(boolean show);

    @StateStrategyType(SkipStrategy.class)
    void showInputNewFolderNameDialog();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showAddedIgnoredFolderMessage(IgnoredFolder folder);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onCompositionsAddedToPlayNext(List<Composition> compositions);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void onCompositionsAddedToQueue(List<Composition> compositions);

    @StateStrategyType(AddToEndSingleStrategy.class)
    void showCurrentComposition(CurrentComposition currentComposition);
}
