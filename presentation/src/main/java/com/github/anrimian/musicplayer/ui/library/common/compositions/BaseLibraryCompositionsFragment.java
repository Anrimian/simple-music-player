package com.github.anrimian.musicplayer.ui.library.common.compositions;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.MenuRes;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils;
import com.github.anrimian.musicplayer.ui.editor.composition.CompositionEditorActivity;
import com.github.anrimian.musicplayer.ui.library.LibraryFragment;

import static com.github.anrimian.musicplayer.Constants.Arguments.POSITION_ARG;

public abstract class BaseLibraryCompositionsFragment extends LibraryFragment {

    protected abstract BaseLibraryCompositionsPresenter getBasePresenter();

    protected void onCompositionActionSelected(Composition composition,
                                             @MenuRes int menuItemId,
                                             Bundle extra) {
        switch (menuItemId) {
            case R.id.menu_play: {
                getBasePresenter().onPlayActionSelected(extra.getInt(POSITION_ARG));
                break;
            }
            case R.id.menu_play_next: {
                getBasePresenter().onPlayNextCompositionClicked(composition);
                break;
            }
            case R.id.menu_add_to_queue: {
                getBasePresenter().onAddToQueueCompositionClicked(composition);
                break;
            }
            case R.id.menu_add_to_playlist: {
                getBasePresenter().onAddToPlayListButtonClicked(composition);
                break;
            }
            case R.id.menu_edit: {
                startActivity(CompositionEditorActivity.newIntent(requireContext(), composition.getId()));
                break;
            }
            case R.id.menu_share: {
                DialogUtils.shareComposition(requireContext(), composition);
                break;
            }
            case R.id.menu_delete: {
                getBasePresenter().onDeleteCompositionButtonClicked(composition);
                break;
            }
        }
    }

    protected void onActionModeItemClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_play: {
                getBasePresenter().onPlayAllSelectedClicked();
                break;
            }
            case R.id.menu_select_all: {
                getBasePresenter().onSelectAllButtonClicked();
                break;
            }
            case R.id.menu_play_next: {
                getBasePresenter().onPlayNextSelectedCompositionsClicked();
                break;
            }
            case R.id.menu_add_to_queue: {
                getBasePresenter().onAddToQueueSelectedCompositionsClicked();
                break;
            }
            case R.id.menu_add_to_playlist: {
                getBasePresenter().onAddSelectedCompositionToPlayListClicked();
                break;
            }
            case R.id.menu_share: {
                getBasePresenter().onShareSelectedCompositionsClicked();
                break;
            }
            case R.id.menu_delete: {
                getBasePresenter().onDeleteSelectedCompositionButtonClicked();
                break;
            }
        }
    }
}
