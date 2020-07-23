package com.github.anrimian.musicplayer.ui.common.images.glide.external;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.github.anrimian.musicplayer.ui.common.images.models.UriCompositionImage;

public final class UriCoverLoader implements ModelLoader<UriCompositionImage, Bitmap> {

    private Context context;

    public UriCoverLoader(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public LoadData<Bitmap> buildLoadData(@NonNull UriCompositionImage model,
                                          int width,
                                          int height,
                                          @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model), new UriCoverFetcher(context, model));
    }

    @Override
    public boolean handles(@NonNull UriCompositionImage model) {
        return true;
    }

}