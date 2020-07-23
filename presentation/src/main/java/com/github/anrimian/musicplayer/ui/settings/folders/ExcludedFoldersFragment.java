package com.github.anrimian.musicplayer.ui.settings.folders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentExcludedFoldersBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.settings.folders.view.ExcludedFolderAdapter;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public class ExcludedFoldersFragment extends MvpAppCompatFragment implements ExcludedFoldersView, FragmentLayerListener {

    @InjectPresenter
    ExcludedFoldersPresenter presenter;

    private FragmentExcludedFoldersBinding viewBinding;
    private CoordinatorLayout clContainer;
    private RecyclerView recyclerView;

    private ExcludedFolderAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    @ProvidePresenter
    ExcludedFoldersPresenter providePresenter() {
        return Components.getLibraryComponent().excludedFoldersPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentExcludedFoldersBinding.inflate(inflater, container, false);
        clContainer = viewBinding.clContainer;
        recyclerView = viewBinding.recyclerView;
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.excluded_folders);
        toolbar.setSubtitle(null);
        toolbar.setTitleClickListener(null);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.hideAll();

        adapter = new ExcludedFolderAdapter(recyclerView, presenter::onDeleteFolderClicked);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        SlidrPanel.simpleSwipeBack(clContainer, this, toolbar::onStackFragmentSlided);
    }

    @Override
    public void onFragmentMovedOnTop() {
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.clearOptionsMenu();
    }

    @Override
    public void showListState() {
        progressViewWrapper.hideAll();
    }

    @Override
    public void showEmptyListState() {
        progressViewWrapper.showMessage(R.string.no_excluded_folders);
    }

    @Override
    public void showErrorState(ErrorCommand errorCommand) {
        progressViewWrapper.showMessage(errorCommand.getMessage(), false);
    }

    @Override
    public void showExcludedFoldersList(List<IgnoredFolder> folders) {
        adapter.submitList(folders);
    }

    @Override
    public void showRemovedFolderMessage(IgnoredFolder folder) {
        MessagesUtils.makeSnackbar(clContainer, R.string.ignored_folder_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.cancel, presenter::onRestoreRemovedFolderClicked)
                .show();
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clContainer, errorCommand.getMessage(), Snackbar.LENGTH_SHORT).show();
    }
}
