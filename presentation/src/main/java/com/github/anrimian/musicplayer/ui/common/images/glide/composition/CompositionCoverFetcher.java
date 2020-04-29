package com.github.anrimian.musicplayer.ui.common.images.glide.composition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.images.CompositionImage;

public class CompositionCoverFetcher implements DataFetcher<Bitmap> {

    private final CompositionImage composition;
    private final Context context;
    private final CompositionSourceProvider compositionSourceProvider;

    CompositionCoverFetcher(CompositionImage composition,
                            Context context,
                            CompositionSourceProvider compositionSourceProvider) {
        this.composition = composition;
        this.context = context;
        this.compositionSourceProvider = compositionSourceProvider;
    }

    @Override
    public void loadData(@NonNull Priority priority, DataCallback<? super Bitmap> callback) {
        callback.onDataReady(extractImageComposition(composition));
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {
    }

    @NonNull
    @Override
    public Class<Bitmap> getDataClass() {
        return Bitmap.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }


    @Nullable
    private Bitmap extractImageComposition(CompositionImage composition) {
        long id = composition.getId();

        MediaMetadataRetriever mmr = null;
        try {
            mmr = new MediaMetadataRetriever();
            mmr.setDataSource(compositionSourceProvider.getCompositionFileDescriptor(id));
            byte[] imageBytes = mmr.getEmbeddedPicture();
            mmr.release();
            if (imageBytes == null) {
                return null;
            }
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.outWidth = getCoverSize();
            opt.outHeight = getCoverSize();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, opt);
        } catch (Exception ignored) {
            return null;
        } finally {
            if (mmr != null) {
                mmr.release();
            }
        }
    }

    private int getCoverSize() {
        return context.getResources().getDimensionPixelSize(R.dimen.notification_large_icon_size);
    }

}