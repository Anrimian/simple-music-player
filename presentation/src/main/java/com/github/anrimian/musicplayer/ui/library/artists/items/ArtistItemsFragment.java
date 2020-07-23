package com.github.anrimian.musicplayer.ui.library.artists.items;

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
import com.github.anrimian.musicplayer.databinding.FragmentBaseFabListBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.utils.functions.BooleanConditionRunner;
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.dialogs.composition.CompositionActionDialogFragment;
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.library.albums.items.AlbumItemsFragment;
import com.github.anrimian.musicplayer.ui.library.artists.items.adapter.ArtistAlbumsPresenter;
import com.github.anrimian.musicplayer.ui.library.artists.items.adapter.ArtistItemsAdapter;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsFragment;
import com.github.anrimian.musicplayer.ui.library.common.compositions.BaseLibraryCompositionsPresenter;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment;
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
import com.r0adkll.slidr.model.SlidrInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.Constants.Arguments.ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.POSITION_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.ARTIST_NAME_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.COMPOSITION_ACTION_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.PROGRESS_DIALOG_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;

public class ArtistItemsFragment extends BaseLibraryCompositionsFragment implements
        ArtistItemsView, FragmentLayerListener, BackButtonListener {

    @InjectPresenter
    ArtistItemsPresenter presenter;

    private RecyclerView recyclerView;
    private CoordinatorLayout clListContainer;
    private View fab;

    private AdvancedToolbar toolbar;
    private ArtistItemsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    private final BooleanConditionRunner showNoCompositionsRunner = new BooleanConditionRunner(2,
            () -> progressViewWrapper.showMessage(R.string.no_compositions));

    private final ArtistAlbumsPresenter artistAlbumsPresenter = new ArtistAlbumsPresenter();

    private DialogFragmentRunner<CompositionActionDialogFragment> compositionActionDialogRunner;
    private DialogFragmentRunner<ChoosePlayListDialogFragment> choosePlayListDialogRunner;
    private DialogFragmentRunner<InputTextDialogFragment> editArtistNameDialogRunner;
    private DialogFragmentDelayRunner progressDialogRunner;

    private SlidrInterface slidrInterface;

    public static ArtistItemsFragment newInstance(long artistId) {
        Bundle args = new Bundle();
        args.putLong(ID_ARG, artistId);
        ArtistItemsFragment fragment = new ArtistItemsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @ProvidePresenter
    ArtistItemsPresenter providePresenter() {
        return Components.artistItemsComponent(getAlbumId()).artistItemsPresenter();
    }

    @Override
    protected BaseLibraryCompositionsPresenter<ArtistItemsView> getBasePresenter() {
        return presenter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentBaseFabListBinding viewBinding = FragmentBaseFabListBinding.inflate(inflater, container, false);
        recyclerView = viewBinding.recyclerView;
        clListContainer = viewBinding.listContainer;
        fab = viewBinding.fab;
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = requireActivity().findViewById(R.id.toolbar);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.onTryAgainClick(presenter::onTryAgainLoadCompositionsClicked);
        progressViewWrapper.hideAll();

        RecyclerViewUtils.attachFastScroller(recyclerView, true);

        adapter = new ArtistItemsAdapter(recyclerView,
                presenter.getSelectedCompositions(),
                presenter::onCompositionClicked,
                presenter::onCompositionLongClick,
                presenter::onCompositionIconClicked,
                this::onAlbumClicked,
                this::onAlbumsScrolled);
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        fab.setOnClickListener(v -> presenter.onPlayAllButtonClicked());

        slidrInterface = SlidrPanel.simpleSwipeBack(clListContainer,
                this,
                toolbar::onStackFragmentSlided);

        FragmentManager fm = getChildFragmentManager();

        choosePlayListDialogRunner = new DialogFragmentRunner<>(fm,
                SELECT_PLAYLIST_TAG,
                f -> f.setOnCompleteListener(presenter::onPlayListToAddingSelected));

        compositionActionDialogRunner = new DialogFragmentRunner<>(fm,
                COMPOSITION_ACTION_TAG,
                f -> f.setOnTripleCompleteListener(this::onCompositionActionSelected));

        editArtistNameDialogRunner = new DialogFragmentRunner<>(fm,
                ARTIST_NAME_TAG,
                fragment -> fragment.setComplexCompleteListener((name, extra) -> {
                    presenter.onNewArtistNameEntered(name, extra.getLong(ID_ARG));
                })
        );

        progressDialogRunner = new DialogFragmentDelayRunner(fm, PROGRESS_DIALOG_TAG);
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
        toolbar.setupOptionsMenu(R.menu.artist_menu, this::onOptionsItemClicked);
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
    public void showArtistInfo(Artist artist) {
        toolbar.setTitle(artist.getName());
        toolbar.setSubtitle(FormatUtils.formatArtistAdditionalInfo(requireContext(),
                artist,
                R.drawable.ic_description_text_circle_inverse
        ));
    }

    @Override
    public void showEmptyList() {
        fab.setVisibility(View.GONE);
        progressViewWrapper.hideAll();
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
    public void updateList(List<Composition> compositions) {
        List<Object> list = new ArrayList<>();
        list.add(artistAlbumsPresenter);
        list.addAll(compositions);
        adapter.submitList(list);

        showNoCompositionsRunner.setCondition(compositions.isEmpty());
        artistAlbumsPresenter.setCompositionsTitleVisible(!compositions.isEmpty());
    }

    @Override
    public void showArtistAlbums(List<Album> albums) {
        artistAlbumsPresenter.submitAlbums(albums);
        showNoCompositionsRunner.setCondition(albums.isEmpty());
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
    public void closeScreen() {
        FragmentNavigation.from(requireFragmentManager()).goBack();
    }

    @Override
    public void showRenameArtistDialog(Artist artist) {
        Bundle bundle = new Bundle();
        bundle.putLong(ID_ARG, artist.getId());
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(R.string.change_name,
                R.string.change,
                R.string.cancel,
                R.string.name,
                artist.getName())
                .canBeEmpty(false)
                .extra(bundle)
                .build();
        editArtistNameDialogRunner.show(fragment);
    }

    @Override
    public void showRenameProgress() {
        ProgressDialogFragment fragment = ProgressDialogFragment.newInstance(R.string.rename_progress);
        progressDialogRunner.show(fragment);
    }

    @Override
    public void hideRenameProgress() {
        progressDialogRunner.cancel();
    }

    //scroll horizontally then scroll to bottom issue
    private void onAlbumsScrolled(boolean onStart) {
        if (onStart) {
            slidrInterface.unlock();
        } else {
            slidrInterface.lock();
        }
    }

    private long getAlbumId() {
        return Objects.requireNonNull(getArguments()).getLong(ID_ARG);
    }

    private void onAlbumClicked(Album album) {
        FragmentNavigation.from(requireFragmentManager())
                .addNewFragment(AlbumItemsFragment.newInstance(album.getId()));
    }

    private void onOptionsItemClicked(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_rename: {
                presenter.onRenameArtistClicked();
                break;
            }
        }
    }
}
