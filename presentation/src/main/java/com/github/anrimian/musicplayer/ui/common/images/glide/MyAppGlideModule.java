package com.github.anrimian.musicplayer.ui.common.images.glide;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.common.images.glide.album.AlbumCoverLoaderFactory;
import com.github.anrimian.musicplayer.ui.common.images.glide.composition.CompositionCoverLoaderFactory;
import com.github.anrimian.musicplayer.ui.common.images.glide.external.UriCoverLoaderFactory;
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage;
import com.github.anrimian.musicplayer.ui.common.images.models.UriCompositionImage;

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        int memoryCacheSizeBytes = 2 * 1024 * 1024;//2 MB
        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));

        int diskCacheSizeBytes = 2 * 1024 * 1024; // 2 MB
        String diskCacheDirectory = "image_cache";
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheDirectory, diskCacheSizeBytes));
    }

    @Override
    public void registerComponents(@NonNull Context context,
                                   @NonNull Glide glide,
                                   @NonNull Registry registry) {
        registry.prepend(CompositionImage.class, Bitmap.class, new CompositionCoverLoaderFactory(context, Components.getAppComponent().sourceRepository()));
        registry.prepend(UriCompositionImage.class, Bitmap.class, new UriCoverLoaderFactory(context));
        registry.prepend(Album.class, Bitmap.class, new AlbumCoverLoaderFactory(context));
    }

}