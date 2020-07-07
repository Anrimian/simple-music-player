package com.github.anrimian.musicplayer.ui.library.genres.list;

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
import com.github.anrimian.musicplayer.databinding.FragmentBaseListBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.models.order.Order;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.common.serialization.GenreSerializer;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerChooserDialogFragment;
import com.github.anrimian.musicplayer.ui.library.LibraryFragment;
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogFragment;
import com.github.anrimian.musicplayer.ui.library.genres.items.GenreItemsFragment;
import com.github.anrimian.musicplayer.ui.library.genres.list.adapter.GenresAdapter;
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

public class GenresListFragment extends LibraryFragment implements
        GenresListView, FragmentLayerListener, BackButtonListener {

    @InjectPresenter
    GenresListPresenter presenter;

    private FragmentBaseListBinding viewBinding;
    private RecyclerView recyclerView;

    private AdvancedToolbar toolbar;
    private GenresAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    private DialogFragmentRunner<MenuDialogFragment> genreMenuDialogRunner;
    private DialogFragmentRunner<InputTextDialogFragment> editGenreNameDialogRunner;
    private DialogFragmentRunner<SelectOrderDialogFragment> selectOrderDialogRunner;
    private DialogFragmentDelayRunner progressDialogRunner;

    @ProvidePresenter
    GenresListPresenter providePresenter() {
        return Components.genresComponent().genresListPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentBaseListBinding.inflate(inflater, container, false);
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

        RecyclerViewUtils.attachFastScroller(recyclerView);

        adapter = new GenresAdapter(recyclerView,
                this::goToGenreScreen,
                this::onGenreLongClick);
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        FragmentManager fm = getChildFragmentManager();
        selectOrderDialogRunner = new DialogFragmentRunner<>(fm,
                ORDER_TAG,
                f -> f.setOnCompleteListener(presenter::onOrderSelected));
        genreMenuDialogRunner = new DialogFragmentRunner<>(fm,
                ARTIST_MENU_TAG,
                fragment -> fragment.setComplexCompleteListener(this::onGenreMenuClicked)
        );
        editGenreNameDialogRunner = new DialogFragmentRunner<>(fm,
                ARTIST_NAME_TAG,
                fragment -> fragment.setComplexCompleteListener((name, extra) -> {
                    presenter.onNewGenreNameEntered(name, extra.getLong(ID_ARG));
                })
        );
        progressDialogRunner = new DialogFragmentDelayRunner(fm, PROGRESS_DIALOG_TAG);
    }

    @Override
    public void onFragmentMovedOnTop() {
        super.onFragmentMovedOnTop();
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.genres);
        toolbar.setupSearch(presenter::onSearchTextChanged, presenter.getSearchText());
        toolbar.setupOptionsMenu(R.menu.library_genres_menu, this::onOptionsItemClicked);
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
        progressViewWrapper.showMessage(R.string.no_genres_in_library);
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
    public void submitList(List<Genre> genres) {
        adapter.submitList(genres);
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

    private void onGenreLongClick(Genre genre) {
        Bundle extra = GenreSerializer.serialize(genre);
        MenuDialogFragment fragment = MenuDialogFragment.newInstance(
                R.menu.genre_menu,
                genre.getName(),
                extra
        );
        genreMenuDialogRunner.show(fragment);
    }

    private void onGenreMenuClicked(MenuItem menuItem, Bundle extra) {
        Genre genre = GenreSerializer.deserialize(extra);
        switch (menuItem.getItemId()) {
            case R.id.menu_rename: {
                showEditGenreNameDialog(genre);
                break;
            }
        }
    }

    private void showEditGenreNameDialog(Genre genre) {
        Bundle bundle = new Bundle();
        bundle.putLong(ID_ARG, genre.getId());
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(R.string.change_name,
                R.string.change,
                R.string.cancel,
                R.string.name,
                genre.getName())
                .canBeEmpty(false)
                .extra(bundle)
                .build();
        editGenreNameDialogRunner.show(fragment);
    }

    private void goToGenreScreen(Genre genre) {
        FragmentNavigation.from(requireFragmentManager())
                .addNewFragment(GenreItemsFragment.newInstance(genre.getId()));
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
