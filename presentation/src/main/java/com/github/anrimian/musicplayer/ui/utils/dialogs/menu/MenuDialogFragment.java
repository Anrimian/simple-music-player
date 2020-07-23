package com.github.anrimian.musicplayer.ui.utils.dialogs.menu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.DialogMenuBinding;
import com.github.anrimian.musicplayer.domain.utils.functions.BiCallback;
import com.github.anrimian.musicplayer.ui.utils.OnCompleteListener;

import static com.github.anrimian.musicplayer.Constants.Arguments.EXTRA_DATA_ARG;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.createMenu;

public class MenuDialogFragment extends DialogFragment {

    private static final String MENU_ARG = "menu_arg";
    private static final String TITLE_ARG = "title_arg";

    @Nullable
    private OnCompleteListener<MenuItem> onCompleteListener;

    @Nullable
    private BiCallback<MenuItem, Bundle> complexCompleteListener;

    public static MenuDialogFragment newInstance(@MenuRes int menuRes, String title) {
        return newInstance(menuRes, title, null);
    }

    public static MenuDialogFragment newInstance(@MenuRes int menuRes,
                                                 String title,
                                                 Bundle extra) {
        Bundle args = new Bundle();
        args.putInt(MENU_ARG, menuRes);
        args.putString(TITLE_ARG, title);
        args.putBundle(EXTRA_DATA_ARG, extra);
        MenuDialogFragment fragment = new MenuDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DialogMenuBinding binding = DialogMenuBinding.inflate(LayoutInflater.from(requireContext()));
        View view = binding.getRoot();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        MenuAdapter menuAdapter = new MenuAdapter(getMenu(), R.layout.item_dialog_menu);
        menuAdapter.setOnItemClickListener(this::onMenuItemClicked);
        binding.recyclerView.setAdapter(menuAdapter);

        return new AlertDialog.Builder(getActivity())
                .setTitle(getTitle())
                .setView(view)
                .create();
    }

    public void setOnCompleteListener(@Nullable OnCompleteListener<MenuItem> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void setComplexCompleteListener(@Nullable BiCallback<MenuItem, Bundle> complexCompleteListener) {
        this.complexCompleteListener = complexCompleteListener;
    }

    private void onMenuItemClicked(MenuItem menuItem) {
        if (onCompleteListener != null) {
            onCompleteListener.onComplete(menuItem);
        }
        if (complexCompleteListener != null) {
            complexCompleteListener.call(menuItem, requireArguments().getBundle(EXTRA_DATA_ARG));
        }
        dismissAllowingStateLoss();
    }

    private Menu getMenu() {
        return createMenu(requireContext(), requireArguments().getInt(MENU_ARG));
    }

    private String getTitle() {
        return requireArguments().getString(TITLE_ARG);
    }
}
