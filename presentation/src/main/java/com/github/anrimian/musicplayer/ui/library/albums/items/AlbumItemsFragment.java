package com.github.anrimian.musicplayer.ui.library.albums.items;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.dialogs.composition.CompositionActionDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.editor.album.AlbumEditorActivity;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsFragment;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter;
import com.github.anrimian.musicplayer.ui.library.compositions.adapter.CompositionsAdapter;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.Constants.Arguments.ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.POSITION_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.COMPOSITION_ACTION_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;

public class AlbumItemsFragment extends BaseLibraryCompositionsFragment implements
        AlbumItemsView, FragmentLayerListener, BackButtonListener {

    @InjectPresenter
    AlbumItemsPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.list_container)
    CoordinatorLayout clListContainer;

    @BindView(R.id.fab)
    View fab;

    private AdvancedToolbar toolbar;
    private CompositionsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    private DialogFragmentRunner<CompositionActionDialogFragment> compositionActionDialogRunner;
    private DialogFragmentRunner<ChoosePlayListDialogFragment> choosePlayListDialogRunner;

    public static AlbumItemsFragment newInstance(long albumId) {
        Bundle args = new Bundle();
        args.putLong(ID_ARG, albumId);
        AlbumItemsFragment fragment = new AlbumItemsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @ProvidePresenter
    AlbumItemsPresenter providePresenter() {
        return Components.albumItemsComponent(getAlbumId()).albumItemsPresenter();
    }

    @Override
    protected BaseLibraryCompositionsPresenter getBasePresenter() {
        return presenter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base_fab_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        toolbar = requireActivity().findViewById(R.id.toolbar);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.onTryAgainClick(presenter::onTryAgainLoadCompositionsClicked);
        progressViewWrapper.hideAll();

        RecyclerViewUtils.attachFastScroller(recyclerView, true);

        adapter = new CompositionsAdapter(recyclerView,
                presenter.getSelectedCompositions(),
                presenter::onCompositionClicked,
                presenter::onCompositionLongClick,
                presenter::onCompositionIconClicked);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> presenter.onPlayAllButtonClicked());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        SlidrPanel.simpleSwipeBack(clListContainer, this, toolbar::onStackFragmentSlided);

        FragmentManager fm = getChildFragmentManager();

        choosePlayListDialogRunner = new DialogFragmentRunner<>(fm,
                SELECT_PLAYLIST_TAG,
                f -> f.setOnCompleteListener(presenter::onPlayListToAddingSelected));

        compositionActionDialogRunner = new DialogFragmentRunner<>(fm,
                COMPOSITION_ACTION_TAG,
                f -> f.setOnTripleCompleteListener(this::onCompositionActionSelected));
    }

    @Override
    public void onFragmentMovedOnTop() {
//        super.onFragmentMovedOnTop();
        presenter.onFragmentMovedToTop();
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setupSearch(null, null);
        toolbar.setTitleClickListener(null);
        toolbar.setupSelectionModeMenu(R.menu.library_compositions_selection_menu,
                this::onActionModeItemClicked);
        toolbar.setupOptionsMenu(R.menu.album_menu, this::onOptionsItemClicked);
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
        return false;
    }

    @Override
    public void showAlbumInfo(Album album) {
        toolbar.setTitle(album.getName());
        toolbar.setSubtitle(FormatUtils.formatAlbumAdditionalInfo(getContext(),
                album,
                R.drawable.ic_description_text_circle_inverse
        ));
    }

    @Override
    public void showEmptyList() {
        fab.setVisibility(View.GONE);
        progressViewWrapper.showMessage(R.string.no_compositions);
    }

    @Override
    public void showEmptySearchResult() {
        fab.setVisibility(View.GONE);
        progressViewWrapper.showMessage(R.string.compositions_for_search_not_found);
    }

    @Override
    public void showList() {
        fab.setVisibility(View.VISIBLE);
        progressViewWrapper.hideAll();
    }

    @Override
    public void showLoading() {
        progressViewWrapper.showProgress();
    }

    @Override
    public void showLoadingError(ErrorCommand errorCommand) {
        progressViewWrapper.showMessage(errorCommand.getMessage(), true);
    }

    @Override
    public void updateList(List<Composition> genres) {
        adapter.submitList(genres);
    }
    @Override
    public void onCompositionSelected(Composition composition, int position) {
        adapter.setItemSelected(position);
    }

    @Override
    public void onCompositionUnselected(Composition composition, int position) {
        adapter.setItemUnselected(position);
    }

    @Override
    public void setItemsSelected(boolean selected) {
        adapter.setItemsSelected(selected);
    }

    @Override
    public void showSelectionMode(int count) {
        toolbar.showSelectionMode(count);
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
    public void showSelectPlayListDialog() {
        ChoosePlayListDialogFragment dialog = toolbar.isInActionMode()?
                ChoosePlayListDialogFragment.newInstance(R.attr.actionModeStatusBarColor)
                : new ChoosePlayListDialogFragment();
        choosePlayListDialogRunner.show(dialog);
    }

    @Override
    public void showConfirmDeleteDialog(List<Composition> compositionsToDelete) {
        DialogUtils.showConfirmDeleteDialog(requireContext(),
                compositionsToDelete,
                presenter::onDeleteCompositionsDialogConfirmed);
    }

    @Override
    public void showDeleteCompositionError(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer,
                getString(R.string.add_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showDeleteCompositionMessage(List<Composition> compositionsToDelete) {
        String text = getDeleteCompleteMessage(requireActivity(), compositionsToDelete);
        MessagesUtils.makeSnackbar(clListContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void shareCompositions(Collection<Composition> selectedCompositions) {
        DialogUtils.shareCompositions(requireContext(), selectedCompositions);
    }

    @Override
    public void showCurrentComposition(CurrentComposition currentComposition) {
        adapter.showCurrentComposition(currentComposition);
    }

    @Override
    public void setDisplayCoversEnabled(boolean isCoversEnabled) {
        adapter.setCoversEnabled(isCoversEnabled);
    }

    @Override
    public void showCompositionActionDialog(Composition composition, int position) {
        Bundle extra = new Bundle();
        extra.putInt(POSITION_ARG, position);

        @AttrRes int statusBarColor = toolbar.isInActionMode()?
                R.attr.actionModeStatusBarColor: android.R.attr.statusBarColor;
        CompositionActionDialogFragment fragment = CompositionActionDialogFragment.newInstance(
                composition,
                R.menu.composition_actions_menu,
                statusBarColor,
                extra);
        compositionActionDialogRunner.show(fragment);
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer, errorCommand.getMessage(), Snackbar.LENGTH_SHORT).show();
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
    public void showEditAlbumScreen(Album album) {
        startActivity(AlbumEditorActivity.newIntent(requireContext(), album.getId()));
    }

    @Override
    public void closeScreen() {
        FragmentNavigation.from(requireFragmentManager()).goBack();
    }

    private long getAlbumId() {
        return Objects.requireNonNull(getArguments()).getLong(ID_ARG);
    }

    private void onOptionsItemClicked(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit: {
                presenter.onEditAlbumClicked();
                break;
            }
        }
    }
}
