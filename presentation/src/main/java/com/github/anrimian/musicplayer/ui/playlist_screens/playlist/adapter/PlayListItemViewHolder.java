package com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.utils.functions.BiCallback;
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop.DragListener;

import javax.annotation.Nonnull;



/**
 * Created on 31.10.2017.
 */

public class PlayListItemViewHolder extends RecyclerView.ViewHolder implements DragListener {

    private CompositionItemWrapper compositionItemWrapper;

    private PlayListItem item;

    PlayListItemViewHolder(LayoutInflater inflater,
                           ViewGroup parent,
                           BiCallback<PlayListItem, Integer> onCompositionClickListener,
                           OnItemClickListener<Integer> onIconClickListener) {
        super(inflater.inflate(R.layout.item_storage_music, parent, false));
        compositionItemWrapper = new CompositionItemWrapper(itemView,
                o -> onIconClickListener.onItemClick(getAdapterPosition()),
                composition -> onCompositionClickListener.call(item, getAdapterPosition())
        );
    }

    public void bind(@Nonnull PlayListItem item, boolean coversEnabled) {
        this.item = item;
        Composition composition = item.getComposition();
        compositionItemWrapper.bind(composition, coversEnabled);
    }

    @Override
    public void onDragStateChanged(boolean dragging) {
        compositionItemWrapper.showAsDraggingItem(dragging);
    }

    private String getString(@StringRes int resId) {
        return getContext().getString(resId);
    }

    private Context getContext() {
        return itemView.getContext();
    }
}
