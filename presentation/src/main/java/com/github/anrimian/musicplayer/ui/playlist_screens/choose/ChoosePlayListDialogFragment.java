package com.github.anrimian.musicplayer.ui.playlist_screens.choose;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.DialogSelectPlayListBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.utils.functions.BiCallback;
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter.PlayListsAdapter;
import com.github.anrimian.musicplayer.ui.playlist_screens.rename.RenamePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.OnCompleteListener;
import com.github.anrimian.musicplayer.ui.utils.ViewUtils;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.BoundValuesDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.DelegateManager;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MotionLayoutDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.StatusBarColorDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.TextColorDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.TextSizeDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.VisibilityDelegate;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import moxy.MvpBottomSheetDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static android.view.View.INVISIBLE;
import static com.github.anrimian.musicplayer.Constants.Arguments.EXTRA_DATA_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.STATUS_BAR_COLOR_ATTR_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.PLAY_LIST_MENU;
import static com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils.setupBottomSheetDialogMaxWidth;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getContentView;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getFloat;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getStatusBarHeight;
import static com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils.attachDynamicShadow;

public class ChoosePlayListDialogFragment extends MvpBottomSheetDialogFragment
        implements ChoosePlayListView {

    @InjectPresenter
    ChoosePlayListPresenter presenter;

    private DialogSelectPlayListBinding viewBinding;
    private RecyclerView recyclerView;

    @Nullable
    private OnCompleteListener<PlayList> onCompleteListener;

    @Nullable
    private BiCallback<PlayList, Bundle> complexCompleteListener;

    private PlayListsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;
    private SlideDelegate slideDelegate;

    public static ChoosePlayListDialogFragment newInstance(@AttrRes int statusBarColorAttr) {
        return newInstance(statusBarColorAttr, null);
    }

    public static ChoosePlayListDialogFragment newInstance(Bundle extra) {
        return newInstance(0, extra);
    }

    public static ChoosePlayListDialogFragment newInstance(@AttrRes int statusBarColorAttr,
                                                           Bundle extra) {
        Bundle args = new Bundle();
        args.putInt(STATUS_BAR_COLOR_ATTR_ARG, statusBarColorAttr);
        args.putBundle(EXTRA_DATA_ARG, extra);
        ChoosePlayListDialogFragment fragment = new ChoosePlayListDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @ProvidePresenter
    ChoosePlayListPresenter providePresenter() {
        return Components.getAppComponent().choosePlayListPresenter();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        viewBinding = DialogSelectPlayListBinding.inflate(LayoutInflater.from(getContext()));
        recyclerView = viewBinding.recyclerView;
        View view = viewBinding.getRoot();
        dialog.setContentView(view);

        DisplayMetrics displayMetrics = requireActivity().getResources().getDisplayMetrics();

        int height = displayMetrics.heightPixels;

        float heightPercent = getFloat(getResources(), R.dimen.choose_playlist_dialog_height);
        int minHeight = (int) (height * heightPercent);
        view.setMinimumHeight(minHeight);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.hideAll();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PlayListsAdapter(
                recyclerView,
                this::onPlayListSelected,
                presenter::onPlayListLongClick
        );
        recyclerView.setAdapter(adapter);

        attachDynamicShadow(recyclerView, viewBinding.titleShadow);

        BottomSheetBehavior bottomSheetBehavior = ViewUtils.findBottomSheetBehavior(dialog);
        bottomSheetBehavior.setPeekHeight(minHeight);
        bottomSheetBehavior.addBottomSheetCallback(new SimpleBottomSheetCallback(newState -> {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss();
            }
        }, presenter::onBottomSheetSlided));

        slideDelegate = buildSlideDelegate();

        viewBinding.ivClose.setOnClickListener(v -> dismiss());
        viewBinding.ivClose.setVisibility(INVISIBLE);//start state
        viewBinding.ivCreatePlaylist.setOnClickListener(v -> onCreatePlayListButtonClicked());

        MenuDialogFragment fragment = (MenuDialogFragment) getChildFragmentManager()
                .findFragmentByTag(PLAY_LIST_MENU);
        if (fragment != null) {
            fragment.setOnCompleteListener(this::onPlayListMenuItemSelected);
        }

        AndroidUtils.setDialogNavigationBarColorAttr(dialog, R.attr.dialogBackground);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupBottomSheetDialogMaxWidth(this);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        slideDelegate.onSlide(0f);
    }

    @Override
    public void showBottomSheetSlided(float slideOffset) {
        recyclerView.post(() -> {
            View contentView = getContentView(getActivity());
            if (contentView == null) {
                return;
            }
            float usableSlideOffset = slideOffset;
            int activityHeight = contentView.getHeight() - getStatusBarHeight(requireContext());
            int viewHeight = viewBinding.listContainer.getHeight();
            if (activityHeight > viewHeight) {
                usableSlideOffset = 0;
            }
            slideDelegate.onSlide(usableSlideOffset);
        });
    }

    @Override
    public void showEmptyList() {
        progressViewWrapper.hideAll();
        progressViewWrapper.showMessage(R.string.play_lists_on_device_not_found, false);
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
    public void updateList(List<PlayList> list) {
        adapter.submitList(list);
    }

    @Override
    public void showPlayListMenu(PlayList playList) {
        MenuDialogFragment fragment = MenuDialogFragment.newInstance(R.menu.play_list_menu, playList.getName());
        fragment.setOnCompleteListener(this::onPlayListMenuItemSelected);
        fragment.show(getChildFragmentManager(), PLAY_LIST_MENU);
    }

    @Override
    public void showConfirmDeletePlayListDialog(PlayList playList) {
        DialogUtils.showConfirmDeleteDialog(requireContext(),
                playList,
                presenter::onDeletePlayListDialogConfirmed);
    }

    @Override
    public void showEditPlayListNameDialog(PlayList playList) {
        RenamePlayListDialogFragment fragment =
                RenamePlayListDialogFragment.newInstance(playList.getId());
        fragment.show(getChildFragmentManager(), null);
    }

    @Override
    public void showPlayListDeleteSuccess(PlayList playList) {
        MessagesUtils.makeSnackbar(viewBinding.listContainer,
                getString(R.string.play_list_deleted, playList.getName()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showDeletePlayListError(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(viewBinding.listContainer,
                getString(R.string.play_list_delete_error, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    private void onPlayListMenuItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_change_play_list_name: {
                presenter.onChangePlayListNameButtonClicked();
                break;
            }
            case R.id.menu_delete_play_list: {
                presenter.onDeletePlayListButtonClicked();
                break;
            }
        }
    }

    public void setOnCompleteListener(@Nullable OnCompleteListener<PlayList> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void setComplexCompleteListener(@Nullable BiCallback<PlayList, Bundle> complexCompleteListener) {
        this.complexCompleteListener = complexCompleteListener;
    }

    private void onPlayListSelected(PlayList playList) {
        if (onCompleteListener != null) {
            onCompleteListener.onComplete(playList);
        }
        if (complexCompleteListener != null) {
            complexCompleteListener.call(playList, requireArguments().getBundle(EXTRA_DATA_ARG));
        }
        dismissAllowingStateLoss();
    }

    private void onCreatePlayListButtonClicked() {
        CreatePlayListDialogFragment fragment = new CreatePlayListDialogFragment();
        fragment.show(getChildFragmentManager(), null);
    }

    private SlideDelegate buildSlideDelegate() {
        SlideDelegate boundDelegate = new DelegateManager()
                .addDelegate(
                        new BoundValuesDelegate(0.85f, 1f,
                                new VisibilityDelegate(viewBinding.ivClose)
                        )
                )
                .addDelegate(
                        new BoundValuesDelegate(0.7f, 1f,
                                new DelegateManager()
                                        .addDelegate(new MotionLayoutDelegate(viewBinding.motionLayout))
                                        .addDelegate(new TextSizeDelegate(viewBinding.tvTitle,
                                                R.dimen.sheet_dialog_title_collapsed_size,
                                                R.dimen.sheet_dialog_title_expanded_size))
                                        .addDelegate(new TextColorDelegate(viewBinding.tvTitle,
                                                android.R.attr.textColorSecondary,
                                                android.R.attr.textColorPrimary)
                                        )
                        )
                );
        return new DelegateManager()
                .addDelegate(new BoundValuesDelegate(0.008f, 0.95f, boundDelegate))
                .addDelegate(new BoundValuesDelegate(0.85f, 1f,
                                new StatusBarColorDelegate(requireActivity().getWindow(),
                                        getColorFromAttr(getContext(), getStatusBarColorAttr()),
                                        getColorFromAttr(requireContext(), R.attr.colorPrimaryDarkSecondary))
                        )
                );
    }

    @AttrRes
    private int getStatusBarColorAttr() {
        Bundle args = getArguments();
        if (args != null) {
            int colorAttr = args.getInt(STATUS_BAR_COLOR_ATTR_ARG);
            if (colorAttr != 0) {
                return colorAttr;
            }
        }
        return android.R.attr.statusBarColor;
    }
}
