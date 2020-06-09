package com.github.anrimian.musicplayer.ui.player_screen.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop.DragListener;

import java.util.List;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 31.10.2017.
 */

class PlayQueueViewHolder extends RecyclerView.ViewHolder implements DragListener {

    @BindView(R.id.btn_actions_menu)
    View btnActionsMenu;

    private CompositionItemWrapper compositionItemWrapper;

    private PlayQueueItem playQueueItem;

    PlayQueueViewHolder(LayoutInflater inflater,
                        ViewGroup parent,
                        OnPositionItemClickListener<PlayQueueItem> onCompositionClickListener,
                        OnViewItemClickListener<PlayQueueItem> menuClickListener,
                        OnPositionItemClickListener<PlayQueueItem> iconClickListener) {
        super(inflater.inflate(R.layout.item_play_queue, parent, false));
        ButterKnife.bind(this, itemView);
        compositionItemWrapper = new CompositionItemWrapper(itemView,
                o -> iconClickListener.onItemClick(getAdapterPosition(), playQueueItem),
                composition -> onCompositionClickListener.onItemClick(getAdapterPosition(), playQueueItem)
        );

        btnActionsMenu.setOnClickListener(v -> menuClickListener.onItemClick(v, playQueueItem));
    }

    @Override
    public void onDragStateChanged(boolean dragging) {
        compositionItemWrapper.showAsDraggingItem(dragging);
    }

    void bind(@Nonnull PlayQueueItem item, boolean showCovers) {
        this.playQueueItem = item;
        Composition composition = item.getComposition();
        compositionItemWrapper.bind(composition, showCovers);
//        compositionItemWrapper.showNumber(getAdapterPosition());
    }

    void update(PlayQueueItem item, List<Object> payloads) {
        this.playQueueItem = item;
        compositionItemWrapper.update(item.getComposition(), payloads);
    }

    void setCoversVisible(boolean visible) {
        compositionItemWrapper.showCompositionImage(visible);
    }

    void showAsCurrentItem(boolean show) {
        compositionItemWrapper.showAsCurrentComposition(show);
    }

    void showAsPlaying(boolean playing, boolean animate) {
        compositionItemWrapper.showAsPlaying(playing, animate);
    }

    PlayQueueItem getPlayQueueItem() {
        return playQueueItem;
    }


}
