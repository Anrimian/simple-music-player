package com.github.anrimian.musicplayer.ui.playlist_screens.create;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.DialogCommonInputBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.OnCompleteListener;

import moxy.MvpAppCompatDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.setSoftInputVisible;

public class CreatePlayListDialogFragment extends MvpAppCompatDialogFragment
        implements CreatePlayListView {

    @InjectPresenter
    CreatePlayListPresenter presenter;

    private EditText etPlayListName;
    private TextView tvError;
    private TextView tvProgress;
    private ProgressBar progressBar;

    private Button btnCreate;

    @Nullable
    private OnCompleteListener<PlayList> onCompleteListener;

    @ProvidePresenter
    CreatePlayListPresenter providePresenter() {
        return Components.getAppComponent().createPlayListsPresenter();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DialogCommonInputBinding viewBinding = DialogCommonInputBinding.inflate(
                LayoutInflater.from(requireActivity())
        );
        etPlayListName = viewBinding.etPlaylistName;
        tvError = viewBinding.tvError;
        tvProgress = viewBinding.tvProgress;
        progressBar = viewBinding.progressBar;

        View view = viewBinding.getRoot();

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.create_playlist)
                .setPositiveButton(R.string.create, null)
                .setNegativeButton(R.string.cancel, (dialog1, which) -> {})
                .setView(view)
                .create();
        setSoftInputVisible(dialog.getWindow());
        dialog.show();

        etPlayListName.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etPlayListName.setRawInputType(InputType.TYPE_CLASS_TEXT);
        etPlayListName.setOnEditorActionListener((v, actionId, event) -> {
            onCompleteButtonClicked();
            return true;
        });
        etPlayListName.requestFocus();

        btnCreate = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnCreate.setOnClickListener(v -> onCompleteButtonClicked());

        return dialog;
    }

    @Override
    public void showProgress() {
        btnCreate.setEnabled(false);
        etPlayListName.setEnabled(false);
        tvError.setVisibility(View.GONE);
        tvProgress.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void showInputState() {
        btnCreate.setEnabled(true);
        etPlayListName.setEnabled(true);
        tvError.setVisibility(View.GONE);
        tvProgress.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError(ErrorCommand errorCommand) {
        btnCreate.setEnabled(true);
        etPlayListName.setEnabled(true);
        tvProgress.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(getString(R.string.create_playlist_error_template, errorCommand.getMessage()));
    }

    @Override
    public void onPlayListCreated(PlayList playList) {
        if (onCompleteListener != null) {
            onCompleteListener.onComplete(playList);
        }
        dismissAllowingStateLoss();
    }

    public void setOnCompleteListener(@Nullable OnCompleteListener<PlayList> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    private void onCompleteButtonClicked() {
        presenter.onCompleteInputButtonClicked(etPlayListName.getText().toString());
    }
}
