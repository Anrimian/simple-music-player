package com.github.anrimian.musicplayer.ui.playlist_screens.rename;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.Constants.Arguments.PLAY_LIST_ID_ARG;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.setSoftInputVisible;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setEditableText;

public class RenamePlayListDialogFragment extends MvpAppCompatDialogFragment
        implements RenamePlayListView {

    @InjectPresenter
    RenamePlayListPresenter presenter;

    @BindView(R.id.et_playlist_name)
    EditText etPlayListName;

    @BindView(R.id.tv_error)
    TextView tvError;

    @BindView(R.id.tv_progress)
    TextView tvProgress;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private Button btnChange;

    public static RenamePlayListDialogFragment newInstance(long playListId) {
        Bundle args = new Bundle();
        args.putLong(PLAY_LIST_ID_ARG, playListId);
        RenamePlayListDialogFragment fragment = new RenamePlayListDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @ProvidePresenter
    RenamePlayListPresenter providePresenter() {
        return Components.getPlayListComponent(getPlayListId()).changePlayListPresenter();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_common_input, null);

        ButterKnife.bind(this, view);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.edit_name)
                .setPositiveButton(R.string.change, null)
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

        btnChange = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnChange.setOnClickListener(v -> onCompleteButtonClicked());

        return dialog;
    }

    @Override
    public void showProgress() {
        btnChange.setEnabled(false);
        etPlayListName.setEnabled(false);
        tvError.setVisibility(View.GONE);
        tvProgress.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void showInputState() {
        btnChange.setEnabled(true);
        etPlayListName.setEnabled(true);
        tvError.setVisibility(View.GONE);
        tvProgress.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError(ErrorCommand errorCommand) {
        btnChange.setEnabled(true);
        etPlayListName.setEnabled(true);
        tvProgress.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(getString(R.string.change_playlist_name_error_template, errorCommand.getMessage()));
    }

    @Override
    public void showPlayListName(String initialName) {
        if (etPlayListName.length() == 0) {
            setEditableText(etPlayListName, initialName);
        }
    }

    @Override
    public void closeScreen() {
        dismissAllowingStateLoss();
    }

    private void onCompleteButtonClicked() {
        presenter.onCompleteInputButtonClicked(etPlayListName.getText().toString());
    }

    private long getPlayListId() {
        return Objects.requireNonNull(getArguments()).getLong(PLAY_LIST_ID_ARG);
    }
}
