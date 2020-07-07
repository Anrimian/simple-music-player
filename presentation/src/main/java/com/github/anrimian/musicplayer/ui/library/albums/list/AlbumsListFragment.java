package com.github.anrimian.musicplayer.ui.library.albums.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentLibraryAlbumsBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.common.serialization.AlbumSerializer;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.editor.album.AlbumEditorActivity;
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerChooserDialogFragment;
import com.github.anrimian.musicplayer.ui.library.LibraryFragment;
import com.github.anrimian.musicplayer.ui.library.albums.items.AlbumItemsFragment;
import com.github.anrimian.musicplayer.ui.library.albums.list.adapter.AlbumsAdapter;
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.Constants.Tags.ALBUM_MENU_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.ORDER_TAG;

public class AlbumsListFragment extends LibraryFragment implements
        AlbumsListView, FragmentLayerListener, BackButtonListener {

    @InjectPresenter
    AlbumsListPresenter presenter;

    private FragmentLibraryAlbumsBinding viewBinding;
    private RecyclerView recyclerView;

    private AdvancedToolbar toolbar;
    private AlbumsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    private DialogFragmentRunner<MenuDialogFragment> albumMenuDialogRunner;
    private DialogFragmentRunner<SelectOrderDialogFragment> selectOrderDialogRunner;

    @ProvidePresenter
    AlbumsListPresenter providePresenter() {
        return Components.albumsComponent().albumsListPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentLibraryAlbumsBinding.inflate(inflater, container, false);
        recyclerView = viewBinding.recyclerView;
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = requireActivity().findViewById(R.id.toolbar);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.onTryAgainClick(presenter::onTryAgainLoadCompositionsClicked);
        progressViewWrapper.hideAll();

        adapter = new AlbumsAdapter(recyclerView, this::goToAlbumScreen, this::onAlbumLongClick);
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewUtils.attachFastScroller(recyclerView);

        FragmentManager fm = getChildFragmentManager();
        albumMenuDialogRunner = new DialogFragmentRunner<>(fm,
                ALBUM_MENU_TAG,
                fragment -> fragment.setComplexCompleteListener(this::onAlbumMenuClicked)
        );
        selectOrderDialogRunner = new DialogFragmentRunner<>(fm,
                ORDER_TAG,
                f -> f.setOnCompleteListener(presenter::onOrderSelected));
    }

    @Override
    public void onFragmentMovedOnTop() {
        super.onFragmentMovedOnTop();
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.albums);
        toolbar.setupSearch(presenter::onSearchTextChanged, presenter.getSearchText());
        toolbar.setupOptionsMenu(R.menu.library_albums_menu, this::onOptionsItemClicked);
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
        progressViewWrapper.showMessage(R.string.no_albums_in_library);
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
    public void submitList(List<Album> albums) {
        adapter.submitList(albums);
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(viewBinding.listContainer, errorCommand.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showSelectOrderScreen(Order order) {
        SelectOrderDialogFragment fragment = SelectOrderDialogFragment.newInstance(order,
                OrderType.ALPHABETICAL,
                OrderType.COMPOSITION_COUNT);
        selectOrderDialogRunner.show(fragment);
    }

    private void onAlbumMenuClicked(MenuItem menuItem, Bundle extra) {
        Album album = AlbumSerializer.deserialize(extra);
        switch (menuItem.getItemId()) {
            case R.id.menu_edit: {
                startActivity(AlbumEditorActivity.newIntent(requireContext(), album.getId()));
                break;
            }
        }
    }

    private void goToAlbumScreen(Album album) {
        FragmentNavigation.from(requireFragmentManager())
                .addNewFragment(AlbumItemsFragment.newInstance(album.getId()));
    }

    private void onAlbumLongClick(Album album) {
        Bundle extra = AlbumSerializer.serialize(album);
        MenuDialogFragment fragment = MenuDialogFragment.newInstance(
                R.menu.album_menu,
                album.getName(),
                extra
        );
        albumMenuDialogRunner.show(fragment);
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
