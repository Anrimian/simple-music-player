package com.github.anrimian.musicplayer.ui.player_screen.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.utils.PlayQueueItemHelper;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Created on 31.10.2017.
 */

public class PlayQueueAdapter extends DiffListAdapter<PlayQueueItem, PlayQueueViewHolder> {

    private final Set<PlayQueueViewHolder> viewHolders = new HashSet<>();

    private OnPositionItemClickListener<PlayQueueItem> onCompositionClickListener;
    private OnViewItemClickListener<PlayQueueItem> menuClickListener;
    private OnPositionItemClickListener<PlayQueueItem> iconClickListener;

    @Nullable
    private PlayQueueItem currentItem;
    private boolean play;
    private boolean isCoversEnabled;

    public PlayQueueAdapter(RecyclerView recyclerView) {
        super(recyclerView, new SimpleDiffItemCallback<>(
                PlayQueueItemHelper::areSourcesTheSame,
                PlayQueueItemHelper::getChangePayload)
        );
    }

    @NonNull
    @Override
    public PlayQueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayQueueViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                onCompositionClickListener,
                menuClickListener,
                iconClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayQueueViewHolder holder, int position) {
        viewHolders.add(holder);

        PlayQueueItem item = getItem(position);
        holder.bind(item, isCoversEnabled);

        boolean isCurrentItem = item.equals(currentItem);
        holder.showAsCurrentItem(isCurrentItem);
        holder.showAsPlaying(isCurrentItem && play, false);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayQueueViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            holder.update(getItem(position), payloads);
        }
    }

    @Override
    public void onViewRecycled(@NonNull PlayQueueViewHolder holder) {
        super.onViewRecycled(holder);
        viewHolders.remove(holder);
    }

    public void onCurrentItemChanged(PlayQueueItem currentItem) {
        this.currentItem = currentItem;
        for (PlayQueueViewHolder holder: viewHolders) {

            boolean isCurrentItem = holder.getPlayQueueItem().equals(currentItem);
            holder.showAsCurrentItem(isCurrentItem);
            holder.showAsPlaying(isCurrentItem && play, true);
        }
    }

    public void showPlaying(boolean play) {
        this.play = play;
        for (PlayQueueViewHolder holder: viewHolders) {
            boolean isCurrentItem = holder.getPlayQueueItem().equals(currentItem);
            holder.showAsCurrentItem(isCurrentItem);
            holder.showAsPlaying(isCurrentItem && play, true);
        }
    }

    public void setCoversEnabled(boolean isCoversEnabled) {
        this.isCoversEnabled = isCoversEnabled;
        for (PlayQueueViewHolder holder: viewHolders) {
            holder.setCoversVisible(isCoversEnabled);
        }
    }

    public void setOnCompositionClickListener(OnPositionItemClickListener<PlayQueueItem> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setMenuClickListener(OnViewItemClickListener<PlayQueueItem> menuClickListener) {
        this.menuClickListener = menuClickListener;
    }

    public void setIconClickListener(OnPositionItemClickListener<PlayQueueItem> iconClickListener) {
        this.iconClickListener = iconClickListener;
    }
}
