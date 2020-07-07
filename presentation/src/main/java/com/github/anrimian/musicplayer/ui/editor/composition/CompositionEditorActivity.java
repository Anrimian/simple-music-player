package com.github.anrimian.musicplayer.ui.editor.composition;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.FragmentManager;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.models.image.UriImageSource;
import com.github.anrimian.musicplayer.databinding.ActivityCompositionEditBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils;
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.common.serialization.GenreSerializer;
import com.github.anrimian.musicplayer.ui.editor.composition.list.ShortGenresAdapter;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_ID_ARG;
import static com.github.anrimian.musicplayer.Constants.RequestCodes.PICK_IMAGE_REQUEST_CODE;
import static com.github.anrimian.musicplayer.Constants.Tags.ADD_GENRE_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.ALBUM_ARTIST_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.ALBUM_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.AUTHOR_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.EDIT_COVER_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.EDIT_GENRE_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.FILE_NAME_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.LYRICS;
import static com.github.anrimian.musicplayer.Constants.Tags.PROGRESS_DIALOG_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.TITLE_TAG;
import static com.github.anrimian.musicplayer.domain.utils.FileUtils.formatFileName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.makeSnackbar;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onLongClick;

public class CompositionEditorActivity extends MvpAppCompatActivity
        implements CompositionEditorView {

    @InjectPresenter
    CompositionEditorPresenter presenter;

    private ActivityCompositionEditBinding viewBinding;

    private DialogFragmentRunner<InputTextDialogFragment> authorDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> titleDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> filenameDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> albumDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> albumArtistDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> addGenreDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> editGenreDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> lyricsDialogFragmentRunner;
    private DialogFragmentRunner<MenuDialogFragment> coverMenuDialogRunner;
    private DialogFragmentDelayRunner progressDialogRunner;

    private ShortGenresAdapter genresAdapter;

    public static Intent newIntent(Context context, long compositionId) {
        Intent intent = new Intent(context, CompositionEditorActivity.class);
        intent.putExtra(COMPOSITION_ID_ARG, compositionId);
        return intent;
    }

    @ProvidePresenter
    CompositionEditorPresenter providePresenter() {
        long compositionId = getIntent().getLongExtra(COMPOSITION_ID_ARG, 0);
        return Components.getCompositionEditorComponent(compositionId).compositionEditorPresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Components.getAppComponent().themeController().applyCurrentSlidrTheme(this);
        super.onCreate(savedInstanceState);
        viewBinding = ActivityCompositionEditBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        AndroidUtils.setNavigationBarColorAttr(this, android.R.attr.colorBackground);

        setSupportActionBar(viewBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.edit_tags);
        }

        genresAdapter = new ShortGenresAdapter(viewBinding.rvGenres,
                presenter::onGenreItemClicked,
                this::onGenreItemLongClicked,
                presenter::onRemoveGenreClicked);
        viewBinding.rvGenres.setAdapter(genresAdapter);
        viewBinding.rvGenres.setLayoutManager(ChipsLayoutManager.newBuilder(this).build());

        viewBinding.changeAuthorClickableArea.setOnClickListener(v -> presenter.onChangeAuthorClicked());
        viewBinding.changeTitleClickableArea.setOnClickListener(v -> presenter.onChangeTitleClicked());
        viewBinding.changeFilenameClickableArea.setOnClickListener(v -> presenter.onChangeFileNameClicked());
        viewBinding.changeAlbumClickableArea.setOnClickListener(v -> presenter.onChangeAlbumClicked());
        viewBinding.changeAlbumArtistClickableArea.setOnClickListener(v -> presenter.onChangeAlbumArtistClicked());
        viewBinding.changeGenreClickableArea.setOnClickListener(v -> presenter.onAddGenreItemClicked());
        viewBinding.changeCoverClickableArea.setOnClickListener(v -> presenter.onChangeCoverClicked());
        viewBinding.changeLyricsClickableArea.setOnClickListener(v -> presenter.onChangeLyricsClicked());
        onLongClick(viewBinding.changeAuthorClickableArea, () -> copyText(viewBinding.tvAuthor, viewBinding.tvAuthorHint));
        onLongClick(viewBinding.changeTitleClickableArea, () -> copyText(viewBinding.tvTitle, viewBinding.tvTitleHint));
        onLongClick(viewBinding.changeFilenameClickableArea, presenter::onCopyFileNameClicked);
        onLongClick(viewBinding.changeAlbumClickableArea, () -> copyText(viewBinding.tvAlbum, viewBinding.tvAlbumHint));
        onLongClick(viewBinding.changeAlbumArtistClickableArea, () -> copyText(viewBinding.tvAlbumArtist, viewBinding.tvAlbumAuthorHint));
        onLongClick(viewBinding.changeLyricsClickableArea, () -> copyText(viewBinding.tvLyricsHint, viewBinding.tvLyrics));

        SlidrPanel.attachWithNavBarChange(this,
                R.attr.playerPanelBackground,
                android.R.attr.colorBackground
        );

        CompatUtils.setMainButtonStyle(viewBinding.ivCoverEdit);
        CompatUtils.setMainButtonStyle(viewBinding.ivFilenameEdit);
        CompatUtils.setMainButtonStyle(viewBinding.ivTitleEdit);
        CompatUtils.setMainButtonStyle(viewBinding.ivAuthorEdit);
        CompatUtils.setMainButtonStyle(viewBinding.ivAlbumEdit);
        CompatUtils.setMainButtonStyle(viewBinding.ivAlbumArtist);
        CompatUtils.setMainButtonStyle(viewBinding.ivLyrics);
        CompatUtils.setMainButtonStyle(viewBinding.ivGenreEdit);

        FragmentManager fm = getSupportFragmentManager();
        authorDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                AUTHOR_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewAuthorEntered));

        titleDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                TITLE_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewTitleEntered));

        filenameDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                FILE_NAME_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewFileNameEntered));

        albumDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                ALBUM_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewAlbumEntered));

        albumArtistDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                ALBUM_ARTIST_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewAlbumArtistEntered));

        addGenreDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                ADD_GENRE_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewGenreEntered));

        lyricsDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                LYRICS,
                fragment -> fragment.setOnCompleteListener(presenter::onNewLyricsEntered));

        editGenreDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                EDIT_GENRE_TAG,
                fragment -> fragment.setComplexCompleteListener((name, extra) -> {
                    presenter.onNewGenreNameEntered(name, GenreSerializer.deserializeShort(extra));
                })
        );
        coverMenuDialogRunner = new DialogFragmentRunner<>(fm,
                EDIT_COVER_TAG,
                fragment -> fragment.setOnCompleteListener(this::onCoverActionSelected)
        );

        progressDialogRunner = new DialogFragmentDelayRunner(fm, PROGRESS_DIALOG_TAG);

        //<return genres after deep scan implementation>
        viewBinding.dividerLyrics.setVisibility(View.INVISIBLE);
        viewBinding.tvGenreHint.setVisibility(GONE);
        viewBinding.ivGenreEdit.setVisibility(GONE);
        viewBinding.changeGenreClickableArea.setVisibility(GONE);
        viewBinding.rvGenres.setVisibility(GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case PICK_IMAGE_REQUEST_CODE: {
                if (data == null) {
                    return;
                }
                Uri uri = data.getData();
                if (uri == null) {
                    return;
                }
                presenter.onNewImageForCoverSelected(new UriImageSource(uri));
            }
            default: super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void closeScreen() {
        finish();
    }

    @Override
    public void showCompositionLoadingError(ErrorCommand errorCommand) {
        viewBinding.tvAuthor.setText(errorCommand.getMessage());
    }

    @Override
    public void showComposition(FullComposition composition) {
        viewBinding.tvTitle.setText(composition.getTitle());

        String album = composition.getAlbum();
        viewBinding.tvAlbum.setText(album);
        int albumArtistVisibility = album == null? GONE : VISIBLE;
        viewBinding.tvAlbumArtist.setVisibility(albumArtistVisibility);
        viewBinding.tvAlbumAuthorHint.setVisibility(albumArtistVisibility);
        viewBinding.ivAlbumArtist.setVisibility(albumArtistVisibility);
        viewBinding.dividerAlbumArtist.setVisibility(albumArtistVisibility);
        //<return genres after deep scan implementation>
//        dividerLyrics.setVisibility(albumArtistVisibility);
        viewBinding.tvAlbumArtist.setText(composition.getAlbumArtist());
        viewBinding.tvLyrics.setText(composition.getLyrics());

        viewBinding.tvAuthor.setText(formatAuthor(composition.getArtist(), this));
        viewBinding.tvFilename.setText(formatFileName(composition.getFileName(), true));

        Components.getAppComponent()
                .imageLoader()
                .displayImageInReusableTarget(viewBinding.ivCover, composition, R.drawable.ic_music_placeholder);
    }

    @Override
    public void showGenres(List<ShortGenre> shortGenres) {
        genresAdapter.submitList(shortGenres);
    }

    @Override
    public void showEnterAuthorDialog(FullComposition composition, String[] hints) {
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(
                R.string.change_author_name,
                R.string.change,
                R.string.cancel,
                R.string.artist,
                composition.getArtist())
                .hints(hints)
                .build();
        authorDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterAlbumArtistDialog(FullComposition composition, String[] hints) {
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(
                R.string.change_album_artist,
                R.string.change,
                R.string.cancel,
                R.string.artist,
                composition.getAlbumArtist())
                .hints(hints)
                .build();
        albumArtistDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterLyricsDialog(FullComposition composition) {
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(
                R.string.change_lyrics,
                R.string.change,
                R.string.cancel,
                R.string.lyrics,
                composition.getLyrics())
                .completeOnEnterButton(false)
                .build();
        lyricsDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showAddGenreDialog(String[] genres) {
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(
                R.string.add_composition_genre,
                R.string.add,
                R.string.cancel,
                R.string.genre,
                null)
                .hints(genres)
                .canBeEmpty(false)
                .build();
        addGenreDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEditGenreDialog(ShortGenre shortGenre, String[] genres) {
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(
                R.string.change_composition_genre,
                R.string.change,
                R.string.cancel,
                R.string.genre,
                shortGenre.getName())
                .hints(genres)
                .extra(GenreSerializer.serialize(shortGenre))
                .canBeEmpty(false)
                .build();
        editGenreDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterTitleDialog(FullComposition composition) {
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.change_title,
                R.string.change,
                R.string.cancel,
                R.string.title,
                composition.getTitle());
        titleDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterFileNameDialog(FullComposition composition) {
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.change_file_name,
                R.string.change,
                R.string.cancel,
                R.string.filename,
                formatFileName(composition.getFileName()),
                false);
        filenameDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterAlbumDialog(FullComposition composition, String[] hints) {
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(R.string.change_album_name,
                R.string.change,
                R.string.cancel,
                R.string.album,
                composition.getAlbum())
                .hints(hints)
                .build();
        albumDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        makeSnackbar(viewBinding.container, errorCommand.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showCheckTagsErrorMessage(ErrorCommand errorCommand) {
        String message = getString(R.string.check_tags_error, errorCommand.getMessage());
        makeSnackbar(viewBinding.container, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void copyFileNameText(String filePath) {
        AndroidUtils.copyText(this, formatFileName(filePath), getString(R.string.filename));
        onTextCopied();
    }

    @Override
    public void showRemovedGenreMessage(ShortGenre genre) {
        String text = getString(R.string.genre_removed_message, genre.getName());
        MessagesUtils.makeSnackbar(viewBinding.container, text, Snackbar.LENGTH_LONG)
                .setAction(R.string.cancel, presenter::onRestoreRemovedGenreClicked)
                .show();
    }

    @Override
    public void showCoverActionsDialog() {
        MenuDialogFragment fragment = MenuDialogFragment.newInstance(
                R.menu.cover_actions_menu,
                getString(R.string.change_cover)
        );
        coverMenuDialogRunner.show(fragment);
    }

    @Override
    public void showSelectImageFromGalleryScreen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    public void showChangeCoverProgress() {
        ProgressDialogFragment fragment = ProgressDialogFragment.newInstance(R.string.changing_cover_progress);
        progressDialogRunner.show(fragment);
    }

    @Override
    public void hideChangeCoverProgress() {
        progressDialogRunner.cancel();
    }

    private void onCoverActionSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_pick_from_gallery: {
                presenter.onNewCoverSelected();
                break;
            }
            case R.id.menu_clear: {
                presenter.onClearCoverClicked();
                break;
            }
        }
    }

    private void onGenreItemLongClicked(ShortGenre genre) {
        AndroidUtils.copyText(this, genre.getName(), getString(R.string.genre));
        onTextCopied();
    }

    private void copyText(TextView textView, TextView tvLabel) {
        AndroidUtils.copyText(this,
                textView.getText().toString(),
                tvLabel.getText().toString());
        onTextCopied();
    }

    private void onTextCopied() {
        makeSnackbar(viewBinding.container, R.string.copied_message, Snackbar.LENGTH_SHORT).show();
    }
}
