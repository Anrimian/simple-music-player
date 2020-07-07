package com.github.anrimian.musicplayer.ui.library.artists.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentLibraryArtistsBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.common.serialization.ArtistSerializer;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerChooserDialogFragment;
import com.github.anrimian.musicplayer.ui.library.LibraryFragment;
import com.github.anrimian.musicplayer.ui.library.artists.items.ArtistItemsFragment;
import com.github.anrimian.musicplayer.ui.library.artists.list.adapter.ArtistsAdapter;
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.Constants.Arguments.ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.ARTIST_MENU_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.ARTIST_NAME_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.ORDER_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.PROGRESS_DIALOG_TAG;

public class ArtistsListFragment extends LibraryFragment implements
        ArtistsListView, FragmentLayerListener, BackButtonListener {

    @InjectPresenter
    ArtistsListPresenter presenter;

    private RecyclerView recyclerView;
    private CoordinatorLayout clListContainer;

    private AdvancedToolbar toolbar;
    private ArtistsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    private DialogFragmentRunner<MenuDialogFragment> artistMenuDialogRunner;
    private DialogFragmentRunner<InputTextDialogFragment> editArtistNameDialogRunner;
    private DialogFragmentRunner<SelectOrderDialogFragment> selectOrderDialogRunner;
    private DialogFragmentDelayRunner progressDialogRunner;

    @ProvidePresenter
    ArtistsListPresenter providePresenter() {
        return Components.artistsComponent().artistsListPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentLibraryArtistsBinding binding = FragmentLibraryArtistsBinding.inflate(inflater, container, false);
        recyclerView = binding.recyclerView;
        clListContainer = binding.listContainer;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = requireActivity().findViewById(R.id.toolbar);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.onTryAgainClick(presenter::onTryAgainLoadCompositionsClicked);
        progressViewWrapper.hideAll();

        adapter = new ArtistsAdapter(recyclerView,
                this::goToArtistScreen,
                this::onArtistLongClick);
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewUtils.attachFastScroller(recyclerView);

        FragmentManager fm = getChildFragmentManager();
        artistMenuDialogRunner = new DialogFragmentRunner<>(fm,
                ARTIST_MENU_TAG,
                fragment -> fragment.setComplexCompleteListener(this::onArtistMenuClicked)
        );
        editArtistNameDialogRunner = new DialogFragmentRunner<>(fm,
                ARTIST_NAME_TAG,
                fragment -> fragment.setComplexCompleteListener((name, extra) -> {
                    presenter.onNewArtistNameEntered(name, extra.getLong(ID_ARG));
                })
        );
        selectOrderDialogRunner = new DialogFragmentRunner<>(fm,
                ORDER_TAG,
                f -> f.setOnCompleteListener(presenter::onOrderSelected));

        progressDialogRunner = new DialogFragmentDelayRunner(fm, PROGRESS_DIALOG_TAG);
    }

    @Override
    public void onFragmentMovedOnTop() {
        super.onFragmentMovedOnTop();
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.artists);
        toolbar.setupSearch(presenter::onSearchTextChanged, presenter.getSearchText());
        toolbar.setupOptionsMenu(R.menu.library_artists_menu, this::onOptionsItemClicked);
    }

    @Override
    public boolean onBackPressed() {
        if (toolbar.isInSearchMode()) {
            toolbar.setSearchModeEnabled(false);
            return true;
        }
        return false;
    }

    @Override
    public void showEmptyList() {
        progressViewWrapper.showMessage(R.string.no_artists_in_library);
    }

    @Override
    public void showEmptySearchResult() {
        progressViewWrapper.showMessage(R.string.compositions_for_search_not_found);
    }

    @Override
    public void showList() {
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
    public void submitList(List<Artist> artists) {
        adapter.submitList(artists);
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

    @Override
    public void showSelectOrderScreen(Order order) {
        SelectOrderDialogFragment fragment = SelectOrderDialogFragment.newInstance(order,
                OrderType.ALPHABETICAL,
                OrderType.COMPOSITION_COUNT);
        selectOrderDialogRunner.show(fragment);
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer, errorCommand.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    private void onArtistMenuClicked(MenuItem menuItem, Bundle extra) {
        Artist artist = ArtistSerializer.deserialize(extra);
        switch (menuItem.getItemId()) {
            case R.id.menu_rename: {
                showEditArtistNameDialog(artist);
                break;
            }
        }
    }

    private void showEditArtistNameDialog(Artist artist) {
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

    private void goToArtistScreen(Artist artist) {
        FragmentNavigation.from(requireFragmentManager())
                .addNewFragment(ArtistItemsFragment.newInstance(artist.getId()));
    }

    private void onArtistLongClick(Artist artist) {
        Bundle extra = ArtistSerializer.serialize(artist);
        MenuDialogFragment fragment = MenuDialogFragment.newInstance(
                R.menu.artist_menu,
                artist.getName(),
                extra
        );
        artistMenuDialogRunner.show(fragment);
    }

    private void onOptionsItemClicked(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_order: {
                presenter.onOrderMenuItemClicked();
                break;
            }
            case R.id.menu_search: {
                toolbar.setSearchModeEnabled(true);
                break;
            }
            case R.id.menu_equalizer: {
                new EqualizerChooserDialogFragment().show(getChildFragmentManager(), null);
                break;
            }
            case R.id.menu_rescan_storage: {
                Components.getAppComponent().mediaScannerRepository().rescanStorage();
                break;
            }
        }
    }
}
