package com.github.anrimian.musicplayer.ui.settings.folders.view;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter;

public class ExcludedFolderAdapter extends DiffListAdapter<IgnoredFolder, ExcludedFolderViewHolder> {

    private final Callback<IgnoredFolder> removeClickListener;

    public ExcludedFolderAdapter(RecyclerView recyclerView,
                                 Callback<IgnoredFolder> removeClickListener) {
        super(recyclerView, new SimpleDiffItemCallback<>());
        this.removeClickListener = removeClickListener;
    }

    @NonNull
    @Override
    public ExcludedFolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ExcludedFolderViewHolder(parent, removeClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ExcludedFolderViewHolder holder, int position) {
        holder.bind(getItem(position));
    }
}
