package com.github.anrimian.musicplayer.ui.common.format;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.ui.common.snackbars.AppSnackbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.SnackbarContentLayout;

import java.util.List;

import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

public class MessagesUtils {

    public static String getAddToPlayListCompleteMessage(Context context,
                                                         PlayList playList,
                                                         List<Composition> compositions) {
        int size = compositions.size();
        if (size == 1) {
            return context.getString(R.string.add_to_playlist_success_template,
                    formatCompositionName(compositions.get(0)),
                    playList.getName());
        } else {
            return context.getResources().getQuantityString(
                    R.plurals.add_to_playlist_count_success_template,
                    size,
                    size,
                    playList.getName()
            );
        }
    }

    public static String getDeletePlayListItemCompleteMessage(Context context,
                                                              PlayList playList,
                                                              List<PlayListItem> items) {
        int size = items.size();
        if (size == 1) {
            return context.getString(R.string.delete_from_playlist_success_template,
                    formatCompositionName(items.get(0).getComposition()),
                    playList.getName());
        } else {
            return context.getResources().getQuantityString(
                    R.plurals.delete_from_playlist_count_success_template,
                    size,
                    size,
                    playList.getName());
        }
    }

    public static String getDeleteCompleteMessage(Context context, List<Composition> compositions) {
        int size = compositions.size();
        if (size == 1) {
            return context.getString(R.string.delete_composition_success,
                    formatCompositionName(compositions.get(0)));
        } else {
            return context.getResources().getQuantityString(
                    R.plurals.delete_compositions_success,
                    size,
                    size
            );
        }
    }

    public static String getPlayNextMessage(Context context, List<Composition> compositions) {
        int size = compositions.size();
        if (size == 1) {
            return context.getString(R.string.play_next_message_single,
                    formatCompositionName(compositions.get(0)));
        } else {
            return context.getResources().getQuantityString(R.plurals.play_next_message, size, size);
        }
    }

    public static String getAddedToQueueMessage(Context context, List<Composition> compositions) {
        int size = compositions.size();
        if (size == 1) {
            return context.getString(R.string.added_to_queue_message_single,
                    formatCompositionName(compositions.get(0)));
        } else {
            return context.getResources().getQuantityString(R.plurals.added_to_queue_message, size, size);
        }
    }

    public static AppSnackbar makeSnackbar(@NonNull ViewGroup view,
                                           @StringRes int text,
                                           @Snackbar.Duration int duration) {
        return makeSnackbar(view, view.getContext().getString(text), duration);
    }

    public static AppSnackbar makeSnackbar(@NonNull ViewGroup view,
                                           @NonNull String text,
                                           @Snackbar.Duration int duration) {
        return makeSnackbar(view, text).duration(duration);
    }

    public static AppSnackbar makeSnackbar(@NonNull ViewGroup view, @NonNull String text) {
        return AppSnackbar.make(view, text);
    }
}
