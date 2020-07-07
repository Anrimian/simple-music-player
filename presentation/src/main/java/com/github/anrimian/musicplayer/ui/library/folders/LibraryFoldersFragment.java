package com.github.anrimian.musicplayer.ui.library.folders;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.AttrRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentLibraryFoldersBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.dialogs.composition.CompositionActionDialogFragment;
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.editor.composition.CompositionEditorActivity;
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerChooserDialogFragment;
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogFragment;
import com.github.anrimian.musicplayer.ui.library.folders.adapter.MusicFileSourceAdapter;
import com.github.anrimian.musicplayer.ui.library.folders.wrappers.HeaderViewWrapper;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersFragment;
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.google.android.material.snackbar.Snackbar;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.github.anrimian.musicplayer.Constants.Arguments.ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.COMPOSITION_ACTION_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.FILE_NAME_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.NEW_FOLDER_NAME_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.ORDER_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.PROGRESS_DIALOG_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_FOR_FOLDER_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils.shareCompositions;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatLinkedFabView;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility;

/**
 * Created on 23.10.2017.
 */

public class LibraryFoldersFragment extends MvpAppCompatFragment
        implements LibraryFoldersView, BackButtonListener, FragmentLayerListener {

    @InjectPresenter
    LibraryFoldersPresenter presenter;

    private FragmentLibraryFoldersBinding viewBinding;
    private RecyclerView recyclerView;
    private CoordinatorLayout clListContainer;

    private final CompositeDisposable fragmentDisposable = new CompositeDisposable();

    private AdvancedToolbar toolbar;
    private ProgressViewWrapper progressViewWrapper;
    private MusicFileSourceAdapter adapter;

    private HeaderViewWrapper headerViewWrapper;

    private DialogFragmentRunner<InputTextDialogFragment> filenameDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> newFolderDialogFragmentRunner;
    private DialogFragmentRunner<CompositionActionDialogFragment> compositionActionDialogRunner;
    private DialogFragmentRunner<ChoosePlayListDialogFragment> choosePlaylistForFolderDialogRunner;
    private DialogFragmentDelayRunner progressDialogRunner;

    public static LibraryFoldersFragment newInstance(@Nullable Long folderId) {
        Bundle args = new Bundle();
        args.putLong(ID_ARG, folderId == null? 0 : folderId);
        LibraryFoldersFragment fragment = new LibraryFoldersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @ProvidePresenter
    LibraryFoldersPresenter providePresenter() {
        return Components.getLibraryFolderComponent(getFolderId()).storageLibraryPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentLibraryFoldersBinding.inflate(inflater, container, false);
        recyclerView = viewBinding.recyclerView;
        clListContainer = viewBinding.listContainer;
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = requireActivity().findViewById(R.id.toolbar);
        fragmentDisposable.add(toolbar.getSelectionModeObservable()
                .subscribe(this::onSelectionModeChanged));

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.onTryAgainClick(presenter::onTryAgainButtonClicked);
        progressViewWrapper.hideAll();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewUtils.attachFastScroller(recyclerView, true);

        adapter = new MusicFileSourceAdapter(recyclerView,
                presenter.getSelectedFiles(),
                presenter.getSelectedMoveFiles(),
                presenter::onCompositionClicked,
                presenter::onFolderClicked,
                presenter::onItemLongClick,
                this::onFolderMenuClicked,
                presenter::onCompositionIconClicked);
        recyclerView.setAdapter(adapter);

        headerViewWrapper = new HeaderViewWrapper(viewBinding.headerContainer);
        headerViewWrapper.setOnClickListener(v -> presenter.onBackPathButtonClicked());

        viewBinding.fab.setOnClickListener(v -> presenter.onPlayAllButtonClicked());
        viewBinding.vgFileMenu.setVisibility(INVISIBLE);
        viewBinding.vgMoveFileMenu.setVisibility(INVISIBLE);
        formatLinkedFabView(viewBinding.vgFileMenu, viewBinding.fab);
        formatLinkedFabView(viewBinding.vgMoveFileMenu, viewBinding.fab);

        viewBinding.ivCut.setOnClickListener(v -> presenter.onMoveSelectedFoldersButtonClicked());
        viewBinding.ivCopy.setOnClickListener(v -> presenter.onCopySelectedFoldersButtonClicked());

        //maybe will be moved to root fragment later
        view.findViewById(R.id.iv_close).setOnClickListener(v -> presenter.onCloseMoveMenuClicked());
        view.findViewById(R.id.iv_paste).setOnClickListener(v -> presenter.onPasteButtonClicked());
        view.findViewById(R.id.iv_paste_in_new_folder)
                .setOnClickListener(v -> presenter.onPasteInNewFolderButtonClicked());

        FragmentManager fm = getChildFragmentManager();

        SelectOrderDialogFragment orderFragment = (SelectOrderDialogFragment) fm
                .findFragmentByTag(ORDER_TAG);
        if (orderFragment != null) {
            orderFragment.setOnCompleteListener(presenter::onOrderSelected);
        }

        ChoosePlayListDialogFragment playListDialog = (ChoosePlayListDialogFragment) fm
                .findFragmentByTag(SELECT_PLAYLIST_TAG);
        if (playListDialog != null) {
            playListDialog.setOnCompleteListener(presenter::onPlayListToAddingSelected);
        }

        choosePlaylistForFolderDialogRunner = new DialogFragmentRunner<>(fm,
                SELECT_PLAYLIST_FOR_FOLDER_TAG,
                fragment -> fragment.setComplexCompleteListener((playlist, bundle) -> {
                    long folderId = bundle.getLong(ID_ARG);
                    presenter.onPlayListForFolderSelected(folderId, playlist);
                }));

        compositionActionDialogRunner = new DialogFragmentRunner<>(fm,
                COMPOSITION_ACTION_TAG,
                f -> f.setOnCompleteListener(this::onCompositionActionSelected));

        filenameDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                FILE_NAME_TAG,
                fragment -> fragment.setComplexCompleteListener((name, extra) -> {
                    presenter.onNewFolderNameEntered(extra.getLong(ID_ARG), name);
                }));

        newFolderDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                NEW_FOLDER_NAME_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewFileNameForPasteEntered));

        progressDialogRunner = new DialogFragmentDelayRunner(fm, PROGRESS_DIALOG_TAG);

        if (getFolderId() != null) {
            SlidrConfig slidrConfig = new SlidrConfig.Builder().position(SlidrPosition.LEFT).build();
            SlidrPanel.replace(viewBinding.contentContainer,
                    () -> {
                        toolbar.showSelectionMode(0);
                        FragmentNavigation.from(requireFragmentManager()).goBack();
                    },
                    slidrConfig);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentDisposable.clear();
    }

    @Override
    public void onFragmentMovedOnTop() {
        presenter.onFragmentDisplayed();

        Activity act = requireActivity();
        AdvancedToolbar toolbar = act.findViewById(R.id.toolbar);
        toolbar.setupSelectionModeMenu(R.menu.library_folders_selection_menu,
                this::onActionModeItemClicked);
        toolbar.setupOptionsMenu(R.menu.library_files_menu, this::onOptionsItemClicked);
    }

    @Override
    public void updateList(List<FileSource> list) {
        adapter.submitList(list);
    }

    @Override
    public void showFolderInfo(FolderFileSource folder) {
        headerViewWrapper.setVisible(true);
        headerViewWrapper.bind(folder);
    }

    @Override
    public void hideFolderInfo() {
        headerViewWrapper.setVisible(false);
    }

    @Override
    public void showEmptyList() {
        viewBinding.fab.setVisibility(View.GONE);
        progressViewWrapper.showMessage(R.string.compositions_on_device_not_found, false);
    }

    @Override
    public void showEmptySearchResult() {
        viewBinding.fab.setVisibility(View.GONE);
        progressViewWrapper.showMessage(R.string.compositions_and_folders_for_search_not_found, false);
    }

    @Override
    public void showList() {
        viewBinding.fab.setVisibility(VISIBLE);
        progressViewWrapper.hideAll();
    }

    @Override
    public void showLoading() {
        progressViewWrapper.showProgress();
    }

    @Override
    public void showError(ErrorCommand errorCommand) {
        progressViewWrapper.hideAll();
        progressViewWrapper.showMessage(errorCommand.getMessage(), true);
    }

    @Override
    public void goBackToParentFolderScreen() {
        FragmentNavigation.from(requireFragmentManager()).goBack();
    }

    @Override
    public boolean onBackPressed() {
        if (toolbar.isInActionMode()) {
            presenter.onSelectionModeBackPressed();
            return true;
        }
        if (toolbar.isInSearchMode()) {
            toolbar.setSearchModeEnabled(false);
            return true;
        }
        if (getFolderId() != null) {
            presenter.onBackPathButtonClicked();
            return true;
        }
        return false;
    }

    @Override
    public void showSearchMode(boolean show) {
        toolbar.setSearchModeEnabled(show);
    }

    @Override
    public void showAddingToPlayListError(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer,
                getString(R.string.add_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions) {
        String text = getAddToPlayListCompleteMessage(requireActivity(), playList, compositions);
        MessagesUtils.makeSnackbar(clListContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showSelectPlayListForFolderDialog(FolderFileSource folder) {
        Bundle bundle = new Bundle();
        bundle.putLong(ID_ARG, folder.getId());
        ChoosePlayListDialogFragment dialog = ChoosePlayListDialogFragment.newInstance(bundle);
        choosePlaylistForFolderDialogRunner.show(dialog);
    }

    @Override
    public void showSelectOrderScreen(Order folderOrder) {
        SelectOrderDialogFragment fragment = SelectOrderDialogFragment.newInstance(folderOrder,
                OrderType.ALPHABETICAL,
                OrderType.ADD_TIME,
                OrderType.DURATION,
                OrderType.SIZE);
        fragment.setOnCompleteListener(presenter::onOrderSelected);
        fragment.show(getChildFragmentManager(), ORDER_TAG);
    }

    @Override
    public void showSelectPlayListDialog() {
        ChoosePlayListDialogFragment dialog = new ChoosePlayListDialogFragment();
        dialog.setOnCompleteListener(presenter::onPlayListToAddingSelected);
        dialog.show(getChildFragmentManager(), SELECT_PLAYLIST_TAG);
    }

    @Override
    public void showConfirmDeleteDialog(List<Composition> compositionsToDelete) {
        DialogUtils.showConfirmDeleteDialog(requireContext(),
                compositionsToDelete,
                presenter::onDeleteCompositionsDialogConfirmed);
    }

    @Override
    public void showConfirmDeleteDialog(FolderFileSource folder) {
        DialogUtils.showConfirmDeleteDialog(requireContext(),
                folder,
                () -> presenter.onDeleteFolderDialogConfirmed(folder));
    }

    @Override
    public void showDeleteCompositionError(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer,
                getString(R.string.delete_composition_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showDeleteCompositionMessage(List<Composition> compositionsToDelete) {
        String text = getDeleteCompleteMessage(requireActivity(), compositionsToDelete);
        MessagesUtils.makeSnackbar(clListContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void sendCompositions(List<Composition> compositions) {
        shareCompositions(requireContext(), compositions);
    }

    @Override
    public void showReceiveCompositionsForSendError(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer,
                getString(R.string.can_not_receive_file_for_send, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void goToMusicStorageScreen(Long folderId) {
        FragmentNavigation.from(requireFragmentManager())
                .addNewFragment(LibraryFoldersFragment.newInstance(folderId));
    }

    @Override
    public void showCompositionActionDialog(Composition composition) {
        @AttrRes int statusBarColor = toolbar.isInActionMode()?
                R.attr.actionModeStatusBarColor: android.R.attr.statusBarColor;
        CompositionActionDialogFragment fragment = CompositionActionDialogFragment.newInstance(
                composition,
                R.menu.composition_actions_menu,
                statusBarColor);
        compositionActionDialogRunner.show(fragment);
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer, errorCommand.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setDisplayCoversEnabled(boolean isCoversEnabled) {
        adapter.setCoversEnabled(isCoversEnabled);
    }

    @Override
    public void showInputFolderNameDialog(FolderFileSource folder) {
        Bundle extra = new Bundle();
        extra.putLong(ID_ARG, folder.getId());
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.rename_folder,
                R.string.change,
                R.string.cancel,
                R.string.folder_name,
                folder.getName(),
                false,
                extra);
        filenameDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showInputNewFolderNameDialog() {
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.new_folder,
                R.string.create,
                R.string.cancel,
                R.string.folder_name,
                null,
                false);
        newFolderDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showSelectionMode(int count) {
        toolbar.showSelectionMode(count);
    }

    @Override
    public void onItemSelected(FileSource item, int position) {
        adapter.setItemSelected(position);
    }

    @Override
    public void onItemUnselected(FileSource item, int position) {
        adapter.setItemUnselected(position);
    }

    @Override
    public void setItemsSelected(boolean selected) {
        adapter.setItemsSelected(selected);
    }

    @Override
    public void updateMoveFilesList() {
        adapter.updateItemsToMove();
    }

    @Override
    public void showMoveFileMenu(boolean show) {
        animateVisibility(viewBinding.vgMoveFileMenu, show? VISIBLE: INVISIBLE);
    }

    @Override
    public void showCurrentComposition(CurrentComposition currentComposition) {
        adapter.showCurrentComposition(currentComposition);
    }

    @Override
    public void showAddedIgnoredFolderMessage(IgnoredFolder folder) {
        String message = getString(R.string.ignored_folder_added, folder.getRelativePath());
        MessagesUtils.makeSnackbar(clListContainer, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.cancel, presenter::onRemoveIgnoredFolderClicked)
                .show();
    }

    @Override
    public void onCompositionsAddedToPlayNext(List<Composition> compositions) {
        String message = MessagesUtils.getPlayNextMessage(requireContext(), compositions);
        MessagesUtils.makeSnackbar(clListContainer, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onCompositionsAddedToQueue(List<Composition> compositions) {
        String message = MessagesUtils.getAddedToQueueMessage(requireContext(), compositions);
        MessagesUtils.makeSnackbar(clListContainer, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void hideProgressDialog() {
        progressDialogRunner.cancel();
    }

    @Override
    public void showMoveProgress() {
        showProgressDialog(R.string.move_progress);
    }

    @Override
    public void showDeleteProgress() {
        showProgressDialog(R.string.delete_progress);
    }

    @Override
    public void showRenameProgress() {
        showProgressDialog(R.string.rename_progress);
    }

    private void showProgressDialog(@StringRes int resId) {
        progressDialogRunner.show(ProgressDialogFragment.newInstance(resId));
    }

    private void onSelectionModeChanged(boolean enabled) {
        animateVisibility(viewBinding.vgFileMenu, enabled? VISIBLE: INVISIBLE);
    }

    private void onCompositionActionSelected(Composition composition, @MenuRes int menuItemId) {
        switch (menuItemId) {
            case R.id.menu_play: {
                presenter.onPlayActionSelected(composition);
                break;
            }
            case R.id.menu_play_next: {
                presenter.onPlayNextCompositionClicked(composition);
                break;
            }
            case R.id.menu_add_to_queue: {
                presenter.onAddToQueueCompositionClicked(composition);
                break;
            }
            case R.id.menu_add_to_playlist: {
                presenter.onAddToPlayListButtonClicked(composition);
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
                presenter.onDeleteCompositionButtonClicked(composition);
                break;
            }
        }
    }

    private void onActionModeItemClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_play: {
                presenter.onPlayAllSelectedClicked();
                break;
            }
            case R.id.menu_select_all: {
                presenter.onSelectAllButtonClicked();
                break;
            }
            case R.id.menu_play_next: {
                presenter.onPlayNextSelectedSourcesClicked();
                break;
            }
            case R.id.menu_add_to_queue: {
                presenter.onAddToQueueSelectedSourcesClicked();
                break;
            }
            case R.id.menu_add_to_playlist: {
                presenter.onAddSelectedSourcesToPlayListClicked();
                break;
            }
            case R.id.menu_share: {
                presenter.onShareSelectedSourcesClicked();
                break;
            }
            case R.id.menu_delete: {
                presenter.onDeleteSelectedCompositionButtonClicked();
                break;
            }
        }
    }

    private void onFolderMenuClicked(View view, FolderFileSource folder) {
        PopupMenuWindow.showPopup(view,
                R.menu.folder_item_menu,
                item -> {
                    switch (item.getItemId()) {
                        case R.id.menu_play: {
                            presenter.onPlayFolderClicked(folder);
                            break;
                        }
                        case R.id.menu_play_next: {
                            presenter.onPlayNextFolderClicked(folder);
                            break;
                        }
                        case R.id.menu_add_to_queue: {
                            presenter.onAddToQueueFolderClicked(folder);
                            break;
                        }
                        case R.id.menu_add_to_playlist: {
                            presenter.onAddFolderToPlayListButtonClicked(folder);
                            break;
                        }
                        case R.id.menu_rename_folder: {
                            presenter.onRenameFolderClicked(folder);
                            break;
                        }
                        case R.id.menu_share: {
                            presenter.onShareFolderClicked(folder);
                            break;
                        }
                        case R.id.menu_hide: {
                            presenter.onExcludeFolderClicked(folder);
                            break;
                        }
                        case R.id.menu_delete: {
                            presenter.onDeleteFolderButtonClicked(folder);
                            break;
                        }
                    }
                });
    }

    @Nullable
    private Long getFolderId() {
        long value = requireArguments().getLong(ID_ARG);
        return value == 0? null: value;
    }

    private void onOptionsItemClicked(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_order: {
                presenter.onOrderMenuItemClicked();
                break;
            }
            case R.id.menu_excluded_folders: {
                //noinspection ConstantConditions
                FragmentNavigation.from(getParentFragment().requireFragmentManager())
                        .addNewFragment(new ExcludedFoldersFragment());
                break;
            }
            case R.id.menu_equalizer: {
                new EqualizerChooserDialogFragment().show(getChildFragmentManager(), null);
                break;
            }
            case R.id.menu_search: {
                presenter.onSearchButtonClicked();
                break;
            }
            case R.id.menu_rescan_storage: {
                Components.getAppComponent().mediaScannerRepository().rescanStorage();
                break;
            }
        }
    }

}
