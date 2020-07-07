package com.github.anrimian.musicplayer.ui.editor.album;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentManager;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ActivityAlbumEditBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils;
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.google.android.material.snackbar.Snackbar;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.Constants.Arguments.ALBUM_ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.AUTHOR_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.NAME_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.PROGRESS_DIALOG_TAG;
import static com.github.anrimian.musicplayer.domain.utils.FileUtils.formatFileName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.makeSnackbar;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onLongClick;

public class AlbumEditorActivity extends MvpAppCompatActivity implements AlbumEditorView {

    @InjectPresenter
    AlbumEditorPresenter presenter;

    private ActivityAlbumEditBinding viewBinding;

    private DialogFragmentRunner<InputTextDialogFragment> authorDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> nameDialogFragmentRunner;
    private DialogFragmentDelayRunner progressDialogRunner;

    public static Intent newIntent(Context context, long albumId) {
        Intent intent = new Intent(context, AlbumEditorActivity.class);
        intent.putExtra(ALBUM_ID_ARG, albumId);
        return intent;
    }

    @ProvidePresenter
    AlbumEditorPresenter providePresenter() {
        long albumId = getIntent().getLongExtra(ALBUM_ID_ARG, 0);
        return Components.getAlbumEditorComponent(albumId).albumEditorPresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Components.getAppComponent().themeController().applyCurrentSlidrTheme(this);
        super.onCreate(savedInstanceState);
        viewBinding = ActivityAlbumEditBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        AndroidUtils.setNavigationBarColorAttr(this, android.R.attr.colorBackground);

        setSupportActionBar(viewBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.edit_album_tags);
        }

        CompatUtils.setMainButtonStyle(viewBinding.ivNameEdit);
        CompatUtils.setMainButtonStyle(viewBinding.ivAuthorEdit);

        viewBinding.changeAuthorClickableArea.setOnClickListener(v -> presenter.onChangeAuthorClicked());
        viewBinding.changeNameClickableArea.setOnClickListener(v -> presenter.onChangeNameClicked());
        onLongClick(viewBinding.changeAuthorClickableArea, () -> copyText(viewBinding.tvAuthor, viewBinding.tvAuthorHint));
        onLongClick(viewBinding.changeNameClickableArea, () -> copyText(viewBinding.tvName, viewBinding.tvNameHint));

        SlidrPanel.attachWithNavBarChange(this,
                R.attr.playerPanelBackground,
                android.R.attr.colorBackground
        );

        FragmentManager fm = getSupportFragmentManager();
        authorDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                AUTHOR_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewAuthorEntered));

        nameDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                NAME_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewNameEntered));

        progressDialogRunner = new DialogFragmentDelayRunner(fm, PROGRESS_DIALOG_TAG);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void closeScreen() {
        finish();
    }

    @Override
    public void showAlbumLoadingError(ErrorCommand errorCommand) {
        viewBinding.tvAuthor.setText(errorCommand.getMessage());
    }

    @Override
    public void showAlbum(Album album) {
        viewBinding.tvName.setText(album.getName());
        viewBinding.tvAuthor.setText(formatAuthor(album.getArtist(), this));
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        makeSnackbar(viewBinding.getRoot(), errorCommand.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showRenameProgress() {
        progressDialogRunner.show(ProgressDialogFragment.newInstance(R.string.rename_progress));
    }

    @Override
    public void hideRenameProgress() {
        progressDialogRunner.cancel();
    }

    @Override
    public void showEnterAuthorDialog(Album album, String[] hints) {
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(
                R.string.change_album_artist,
                R.string.change,
                R.string.cancel,
                R.string.artist,
                album.getArtist())
                .hints(hints)
                .build();
        authorDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterNameDialog(Album album) {
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.change_name,
                R.string.change,
                R.string.cancel,
                R.string.name,
                formatFileName(album.getName()),
                false);
        nameDialogFragmentRunner.show(fragment);
    }

    private void copyText(TextView textView, TextView tvLabel) {
        AndroidUtils.copyText(this,
                textView.getText().toString(),
                tvLabel.getText().toString());
        onTextCopied();
    }

    private void onTextCopied() {
        makeSnackbar(viewBinding.getRoot(), R.string.copied_message, Snackbar.LENGTH_SHORT).show();
    }
}
