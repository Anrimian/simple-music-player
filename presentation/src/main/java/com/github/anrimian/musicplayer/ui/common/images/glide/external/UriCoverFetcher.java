package com.github.anrimian.musicplayer.ui.common.images.glide.external;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.ui.common.images.models.UriCompositionImage;

public class UriCoverFetcher implements DataFetcher<Bitmap> {

    private final Context context;
    private final UriCompositionImage uriCompositionImage;

    UriCoverFetcher(Context context, UriCompositionImage uriCompositionImage) {
        this.context = context;
        this.uriCompositionImage = uriCompositionImage;
    }

    @Override
    public void loadData(@NonNull Priority priority, DataCallback<? super Bitmap> callback) {
        callback.onDataReady(extractImageComposition(uriCompositionImage.getSource()));
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
    private Bitmap extractImageComposition(UriCompositionSource uriCompositionSource) {
        try {
            byte[] imageBytes = uriCompositionSource.getImageBytes();
            if (imageBytes == null) {
                return null;
            }
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.outWidth = getCoverSize();
            opt.outHeight = getCoverSize();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, opt);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int getCoverSize() {
        return context.getResources().getDimensionPixelSize(R.dimen.notification_large_icon_size);
    }

}