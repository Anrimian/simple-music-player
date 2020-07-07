package com.github.anrimian.musicplayer.ui.common.images.glide.composition;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.github.anrimian.musicplayer.data.storage.source.CompositionSourceProvider;
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage;

public final class CompositionCoverLoader implements ModelLoader<CompositionImage, Bitmap> {

    private final Context context;
    private final CompositionSourceProvider compositionSourceProvider;

    CompositionCoverLoader(Context context, CompositionSourceProvider compositionSourceProvider) {
        this.context = context;
        this.compositionSourceProvider = compositionSourceProvider;
    }

    @NonNull
    @Override
    public ModelLoader.LoadData<Bitmap> buildLoadData(@NonNull CompositionImage model,
                                                      int width,
                                                      int height,
                                                      @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model),
                new CompositionCoverFetcher(model, context, compositionSourceProvider));
    }

    @Override
    public boolean handles(@NonNull CompositionImage model) {
        return true;
    }

}