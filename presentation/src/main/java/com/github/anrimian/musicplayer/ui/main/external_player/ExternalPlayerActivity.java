package com.github.anrimian.musicplayer.ui.main.external_player;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.data.utils.db.CursorWrapper;
import com.github.anrimian.musicplayer.databinding.ActivityExternalPlayerBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getRepeatModeIcon;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.getRepeatModeText;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked;

public class ExternalPlayerActivity extends MvpAppCompatActivity implements ExternalPlayerView {

    @InjectPresenter
    ExternalPlayerPresenter presenter;

    private ActivityExternalPlayerBinding viewBinding;

    private SeekBarViewWrapper seekBarViewWrapper;

    @ProvidePresenter
    ExternalPlayerPresenter providePresenter() {
        Uri uriToPlay = getIntent().getData();
        UriCompositionSource source = createCompositionSource(uriToPlay);
        ExternalPlayerPresenter presenter = Components.getExternalPlayerComponent().externalPlayerPresenter();
        presenter.onSourceForPlayingReceived(source);
        return presenter;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Components.getAppComponent().themeController().applyCurrentTheme(this);
        getTheme().applyStyle(R.style.DialogActivityTheme, true);
        super.onCreate(savedInstanceState);
        viewBinding = ActivityExternalPlayerBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        CompatUtils.setMainButtonStyle(viewBinding.ivPlayPause);
        CompatUtils.setMainButtonStyle(viewBinding.ivRepeatMode);

        seekBarViewWrapper = new SeekBarViewWrapper(viewBinding.sbTrackState);
        seekBarViewWrapper.setProgressChangeListener(presenter::onTrackRewoundTo);
        seekBarViewWrapper.setOnSeekStartListener(presenter::onSeekStart);
        seekBarViewWrapper.setOnSeekStopListener(presenter::onSeekStop);

        viewBinding.ivPlayPause.setOnClickListener(v -> presenter.onPlayPauseClicked());
        viewBinding.ivRepeatMode.setOnClickListener(v -> presenter.onRepeatModeButtonClicked());
        onCheckChanged(viewBinding.cbKeepPlayingAfterClose, presenter::onKeepPlayerInBackgroundChecked);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uriToPlay = intent.getData();
        UriCompositionSource source = createCompositionSource(uriToPlay);
        presenter.onSourceForPlayingReceived(source);
    }

    @Override
    public void displayComposition(UriCompositionSource source) {
        viewBinding.tvComposition.setText(formatCompositionName(source.getTitle(), source.getDisplayName()));
        viewBinding.tvCompositionAuthor.setText(FormatUtils.formatAuthor(source.getArtist(), this));
        seekBarViewWrapper.setMax(source.getDuration());
        viewBinding.tvTotalTime.setText(formatMilliseconds(source.getDuration()));
        Components.getAppComponent()
                .imageLoader()
                .displayImageInReusableTarget(viewBinding.ivMusicIcon, source, R.drawable.ic_music_placeholder);
    }

    @Override
    public void showStopState() {
        AndroidUtils.setAnimatedVectorDrawable(viewBinding.ivPlayPause, R.drawable.anim_pause_to_play);
        viewBinding.ivPlayPause.setContentDescription(getString(R.string.play));
    }

    @Override
    public void showPlayState() {
        AndroidUtils.setAnimatedVectorDrawable(viewBinding.ivPlayPause, R.drawable.anim_play_to_pause);
        viewBinding.ivPlayPause.setContentDescription(getString(R.string.pause));
    }

    @Override
    public void showTrackState(long currentPosition, long duration) {
        seekBarViewWrapper.setProgress(currentPosition);
        String formattedTime = formatMilliseconds(currentPosition);
        viewBinding.sbTrackState.setContentDescription(getString(R.string.position_template, formattedTime));
        viewBinding.tvPlayedTime.setText(formattedTime);
    }

    @Override
    public void showRepeatMode(int mode) {
        @DrawableRes int iconRes = getRepeatModeIcon(mode);
        viewBinding.ivRepeatMode.setImageResource(iconRes);
        String description = getString(getRepeatModeText(mode));
        viewBinding.ivRepeatMode.setContentDescription(description);
    }

    @Override
    public void showPlayErrorEvent(@Nullable ErrorType errorType) {
        viewBinding.tvError.setText(getErrorEventText(errorType));
    }

    @Override
    public void showKeepPlayerInBackground(boolean externalPlayerKeepInBackground) {
        setChecked(viewBinding.cbKeepPlayingAfterClose, externalPlayerKeepInBackground);
    }

    @Nullable
    private String getErrorEventText(@Nullable ErrorType errorType) {
        if (errorType == null) {
            return null;
        }
        switch (errorType) {
            case UNSUPPORTED: return getString(R.string.unsupported_format_hint);
            case NOT_FOUND: return getString(R.string.file_not_found);
            default: return getString(R.string.unknown_play_error);
        }
    }

    //async creation?
    private UriCompositionSource createCompositionSource(Uri uri) {
        String displayName = null;
        String title = null;
        String artist = null;
        String album = null;
        long duration = 0;
        long size = 0;
        byte[] imageBytes = null;

        try (Cursor cursor = getContentResolver().query(
                uri,
                new String[] {
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.SIZE
                },
                null,
                null,
                null)) {
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            CursorWrapper cursorWrapper = new CursorWrapper(cursor);
            if (cursor.moveToFirst()) {
                displayName = cursorWrapper.getString(MediaStore.Audio.Media.DISPLAY_NAME);
                size = cursorWrapper.getLong(MediaStore.Audio.Media.SIZE);
            }
        }

        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(this, uri);

            artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            //we can't read image from uri later, read as first as possible
            imageBytes = mmr.getEmbeddedPicture();
            try {
                duration = Long.parseLong(durationStr);
            } catch (NumberFormatException ignored) {}


        } catch (Exception ignored) {

        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }

        return new UriCompositionSource(uri,
                displayName == null? "unknown name" : displayName,
                title,
                artist,
                album,
                duration,
                size,
                imageBytes);
    }
}
