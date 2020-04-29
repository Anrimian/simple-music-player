package com.github.anrimian.musicplayer.ui.editor.composition.list;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.domain.models.utils.ShortGenreHelper;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter;

import java.util.List;

public class ShortGenresAdapter extends DiffListAdapter<ShortGenre, GenreViewHolder> {

    private final Callback<ShortGenre> onClickListener;
    private final Callback<ShortGenre> onLongClickListener;
    private final Callback<ShortGenre> onRemoveClickListene;

    public ShortGenresAdapter(RecyclerView recyclerView,
                              Callback<ShortGenre> onClickListener,
                              Callback<ShortGenre> onLongClickListener,
                              Callback<ShortGenre> onRemoveClickListene) {
        super(recyclerView, new SimpleDiffItemCallback<>(
                ShortGenreHelper::areSourcesTheSame,
                ShortGenreHelper::getChangePayload)
        );
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
        this.onRemoveClickListene = onRemoveClickListene;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GenreViewHolder(parent,
                onClickListener,
                onLongClickListener,
                onRemoveClickListene);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        holder.update(getItem(position), payloads);
    }

}
