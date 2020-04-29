package com.github.anrimian.musicplayer.ui.common.images.glide.album;

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
import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbumsProvider;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import java.io.IOException;
import java.io.InputStream;

public class AlbumCoverFetcher implements DataFetcher<Bitmap> {

    private final Album album;
    private final Context context;
    private final StorageAlbumsProvider storageAlbumsProvider;

    AlbumCoverFetcher(Album album, Context context) {
        this.album = album;
        this.context = context;

        storageAlbumsProvider = Components.getAppComponent().storageAlbumsProvider();
    }

    @Override
    public void loadData(@NonNull Priority priority, DataCallback<? super Bitmap> callback) {
        callback.onDataReady(extractAlbumCover(album));
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


    private Bitmap extractAlbumCover(Album album) {
        try (InputStream in = storageAlbumsProvider.getAlbumCoverStream(album.getName())) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.outWidth = getCoverSize();
            opt.outHeight = getCoverSize();
            return BitmapFactory.decodeStream(in, null, opt);
        } catch (IOException ignores) {
            return null;
        }
    }

    private int getCoverSize() {
        return context.getResources().getDimensionPixelSize(R.dimen.notification_large_icon_size);
    }

}