package com.github.anrimian.musicplayer.ui.library.artists.items.adapter.albums;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.utils.AlbumHelper;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter;

import java.util.List;

public class AlbumsAdapter extends DiffListAdapter<Album, AlbumViewHolder> {

    private final Callback<Album> onClickListener;

    public AlbumsAdapter(RecyclerView recyclerView, Callback<Album> onClickListener) {
        super(recyclerView, new SimpleDiffItemCallback<>(
                AlbumHelper::areSourcesTheSame,
                AlbumHelper::getChangePayload)
        );
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlbumViewHolder(parent, onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        holder.update(getItem(position), payloads);
    }
}
