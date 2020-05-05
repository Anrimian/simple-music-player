package com.github.anrimian.musicplayer.ui.common.images;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.images.glide.util.CustomAppWidgetTarget;
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController;

import javax.annotation.Nonnull;

public class CoverImageLoader {

    private static final int DEFAULT_PLACEHOLDER = R.drawable.ic_music_placeholder_simple;
    private static final int TIMEOUT_MILLIS = 5000;
    private static final int NOTIFICATION_IMAGE_TIMEOUT_MILLIS = 500;

    private final Context context;
    private final ThemeController themeController;

    private Bitmap defaultNotificationBitmap;

    public CoverImageLoader(Context context, ThemeController themeController) {
        this.context = context;
        this.themeController = themeController;
    }

    public void displayImage(@NonNull ImageView imageView, @NonNull Composition data) {
        Glide.with(imageView)
                .load(new CompositionImage(data.getId()))
                .placeholder(DEFAULT_PLACEHOLDER)
                .error(DEFAULT_PLACEHOLDER)
                .timeout(TIMEOUT_MILLIS)
                .into(imageView);
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull Composition data,
                             @DrawableRes int errorPlaceholder) {
        Glide.with(context)
                .load(new CompositionImage(data.getId()))
                .placeholder(errorPlaceholder)
                .error(errorPlaceholder)
                .timeout(TIMEOUT_MILLIS)
                .into(imageViewTarget(imageView));
    }

    public void displayImage(@NonNull ImageView imageView,
                             @NonNull Album album,
                             @DrawableRes int errorPlaceholder) {
        Glide.with(imageView)
                .load(album)
                .placeholder(errorPlaceholder)
                .error(errorPlaceholder)
                .timeout(TIMEOUT_MILLIS)
                .into(imageView);
    }

    public Runnable loadNotificationImage(@Nonnull Composition data,
                                          Callback<Bitmap> onCompleted) {
        CustomTarget<Bitmap> target = simpleTarget(bitmap -> {
            if (bitmap == null) {
                bitmap = getDefaultNotificationBitmap();
            }
            onCompleted.call(bitmap);
        });

        Glide.with(context)
                .asBitmap()
                .load(new CompositionImage(data.getId()))
                .timeout(NOTIFICATION_IMAGE_TIMEOUT_MILLIS)
                .into(target);

        return () -> Glide.with(context).clear(target);
    }

    public Bitmap getDefaultNotificationBitmap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (defaultNotificationBitmap == null) {
                defaultNotificationBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.RGB_565);
            }
            int color = themeController.getPrimaryThemeColor();
            defaultNotificationBitmap.eraseColor(color);
        } else {
            if (defaultNotificationBitmap == null) {
                defaultNotificationBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_music_box);
            }
        }
        return defaultNotificationBitmap;
    }

    public void loadImage(@Nonnull Composition data, Callback<Bitmap> onCompleted) {
        Glide.with(context)
                .asBitmap()
                .load(new CompositionImage(data.getId()))
                .timeout(TIMEOUT_MILLIS)
                .into(simpleTarget(onCompleted));
    }

    public void displayImage(@NonNull RemoteViews widgetView,
                             @IdRes int viewId,
                             int appWidgetId,
                             long compositionId,
                             @DrawableRes int placeholder) {
        CustomAppWidgetTarget widgetTarget = new CustomAppWidgetTarget(context,
                viewId,
                widgetView,
                placeholder,
                appWidgetId);

        Glide.with(context)
                .asBitmap()
                .load(new CompositionImage(compositionId))
                .override(150, 150)
                .downsample(DownsampleStrategy.AT_MOST)
                .transform(new CircleCrop())
                .timeout(TIMEOUT_MILLIS)
                .into(widgetTarget);
    }

    private ImageViewTarget<Drawable> imageViewTarget(ImageView imageView) {
        return new ImageViewTarget<Drawable>(imageView) {
            @Override
            protected void setResource(@Nullable Drawable resource) {
                view.setImageDrawable(resource);
            }

            @Override
            public void onLoadStarted(@Nullable Drawable placeholder) {
                if (view.getDrawable() == null) {
                    super.onLoadStarted(placeholder);
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                if (view.getDrawable() == null) {
                    super.onLoadCleared(placeholder);
                }
            }
        };
    }

    private <T> CustomTarget<T> simpleTarget(Callback<T> callback) {
        return new CustomTarget<T>() {
            @Override
            public void onResourceReady(@NonNull T resource, @Nullable Transition<? super T> transition) {
                callback.call(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                callback.call(null);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                callback.call(null);
            }
        };
    }
}
