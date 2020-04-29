package com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.models.utils.PlayListItemHelper;
import com.github.anrimian.musicplayer.domain.utils.functions.BiCallback;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter;

/**
 * Created on 31.10.2017.
 */

public class PlayListItemAdapter extends DiffListAdapter<PlayListItem, PlayListItemViewHolder> {

    private final boolean coversEnabled;

    private final BiCallback<PlayListItem, Integer> onCompositionClickListener;
    private final OnItemClickListener<Integer> onIconClickListener;

    public PlayListItemAdapter(RecyclerView recyclerView,
                               boolean coversEnabled,
                               BiCallback<PlayListItem, Integer> onCompositionClickListener,
                               OnItemClickListener<Integer> onIconClickListener) {
        super(recyclerView, new SimpleDiffItemCallback<>(
                PlayListItemHelper::areSourcesTheSame,
                PlayListItemHelper::getChangePayload)
        );
        this.coversEnabled = coversEnabled;
        this.onCompositionClickListener = onCompositionClickListener;
        this.onIconClickListener = onIconClickListener;
    }

    @NonNull
    @Override
    public PlayListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayListItemViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                onCompositionClickListener,
                onIconClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListItemViewHolder holder, int position) {
        PlayListItem composition = getItem(position);
        holder.bind(composition, coversEnabled);
    }
}
