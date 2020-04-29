package com.github.anrimian.musicplayer.ui.common.format;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.order.OrderType;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_swipe.DragAndSwipeTouchHelperCallback;

import java.util.Locale;

import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.run;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created on 15.11.2017.
 */

public class FormatUtils {

    public static String formatCompositionsCount(Context context, int compositionsCount) {
        return context.getResources().getQuantityString(
                R.plurals.compositions_count,
                compositionsCount,
                compositionsCount);
    }

    public static String formatAlbumsCount(Context context, int albumsCount) {
        return context.getResources().getQuantityString(
                R.plurals.albums_count,
                albumsCount,
                albumsCount);
    }

    public static StringBuilder formatCompositionAuthor(Composition composition, Context context) {
        String author = composition.getArtist();
        return formatAuthor(author, context);
    }

    public static StringBuilder formatAuthor(String author, Context context) {
        StringBuilder sb = new StringBuilder();
        if (!isEmpty(author)) {
            sb.append(author);
        } else {
            sb.append(context.getString(R.string.unknown_author));
        }
        return sb;
    }

    public static String formatMilliseconds(long millis) {
        StringBuilder sb = new StringBuilder();

        long hours = MILLISECONDS.toHours(millis);
        if (hours != 0) {
            sb.append(format(Locale.getDefault(), "%02d", hours));
            sb.append(":");
        }

        long minutes = MILLISECONDS.toMinutes(millis) - HOURS.toMinutes(hours);
        if (minutes != 0 || hours != 0) {
            sb.append(format(Locale.getDefault(), "%02d", minutes));

        } else {
            sb.append("00");
        }
        sb.append(":");

        long seconds = MILLISECONDS.toSeconds(millis) - MINUTES.toSeconds(MILLISECONDS.toMinutes(millis));
        sb.append(format(Locale.getDefault(), "%02d", seconds));
        return sb.toString();
    }

    public static SpannableStringBuilder formatArtistAdditionalInfo(Context context,
                                                                    Artist artist) {
        return formatArtistAdditionalInfo(context, artist, R.drawable.ic_description_text_circle);
    }

    public static SpannableStringBuilder formatArtistAdditionalInfo(Context context,
                                                                    Artist artist,
                                                                    @DrawableRes int dividerDrawableRes) {
        SpannableStringBuilder sb = new DescriptionSpannableStringBuilder(context, dividerDrawableRes);
        sb.append(formatCompositionsCount(context, artist.getCompositionsCount()));
        int albumsCount = artist.getAlbumsCount();
        if (albumsCount > 0) {
            sb.append(formatAlbumsCount(context, albumsCount));
        }
        return sb;
    }

    public static SpannableStringBuilder formatAlbumAdditionalInfo(Context context, Album album) {
        return formatAlbumAdditionalInfo(context, album, R.drawable.ic_description_text_circle);
    }

    public static SpannableStringBuilder formatAlbumAdditionalInfo(Context context,
                                                                   Album album,
                                                                   @DrawableRes int dividerDrawableRes) {
        SpannableStringBuilder sb = new DescriptionSpannableStringBuilder(context, dividerDrawableRes);
        String artist = album.getArtist();
        if (!isEmpty(artist)) {
            sb.append(artist);
        }
        sb.append(formatCompositionsCount(
                context,
                album.getCompositionsCount())
        );
        return sb;
    }

    public static int getOrderTitle(OrderType orderType) {
        switch (orderType) {
            case ALPHABETICAL: return R.string.alphabetical_order;
            case ADD_TIME: return R.string.add_date_order;
            case COMPOSITION_COUNT: return R.string.by_composition_count;
            case DURATION: return R.string.by_duration;
            case SIZE: return R.string.by_size;
            default: throw new IllegalStateException("can not find title for order: " + orderType);
        }
    }

    public static int getReversedOrderText(OrderType orderType) {
        switch (orderType) {
            case ALPHABETICAL: return R.string.alphabetical_order_desc_title;
            case ADD_TIME: return R.string.add_date_order_desc_title;
            case COMPOSITION_COUNT: return R.string.more_first;
            case DURATION: return R.string.longest_first;
            case SIZE: return R.string.largest_first;
            default: throw new IllegalStateException("can not find title for order: " + orderType);
        }
    }

    @DrawableRes
    public static int getRepeatModeIcon(int repeatMode) {
        switch (repeatMode) {
            case RepeatMode.NONE: {
                return R.drawable.ic_repeat_off;
            }
            case RepeatMode.REPEAT_COMPOSITION: {
                return R.drawable.ic_repeat_once;
            }
            case RepeatMode.REPEAT_PLAY_LIST: {
                return R.drawable.ic_repeat;
            }
            default: return R.drawable.ic_repeat_off;
        }
    }

    public static void formatLinkedFabView(View view, View fab) {
        run(fab, () -> {
            int width = fab.getWidth();
            int margin = view.getResources().getDimensionPixelSize(R.dimen.content_horizontal_margin);
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            params.setMarginEnd(width + margin * 2);
            params.height = fab.getHeight();
            view.setLayoutParams(params);
        });
    }

    public static DragAndSwipeTouchHelperCallback withSwipeToDelete(RecyclerView recyclerView,
                                                                    @ColorInt int backgroundColor,
                                                                    Callback<Integer> swipeCallback,
                                                                    int swipeFlags,
                                                                    @DrawableRes int iconRes,
                                                                    @StringRes int textResId) {
        return DragAndSwipeTouchHelperCallback.withSwipeToDelete(recyclerView,
                backgroundColor,
                swipeCallback,
                swipeFlags,
                iconRes,
                textResId,
                R.dimen.swipe_panel_width,
                R.dimen.swipe_panel_padding_end,
                R.dimen.swipe_panel_text_top_padding,
                R.dimen.swipe_panel_icon_size,
                R.dimen.swipe_panel_text_size);
    }
}
