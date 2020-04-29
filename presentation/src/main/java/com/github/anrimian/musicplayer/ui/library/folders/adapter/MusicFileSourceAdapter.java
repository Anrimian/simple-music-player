package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.utils.FolderHelper;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_SELECTED;
import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_UNSELECTED;

/**
 * Created on 31.10.2017.
 */

public class MusicFileSourceAdapter extends DiffListAdapter<FileSource, FileViewHolder> {

    private static final int TYPE_MUSIC = 1;
    private static final int TYPE_FILE = 2;

    private final Set<FileViewHolder> viewHolders = new HashSet<>();
    private final HashSet<FileSource> selectedItems;
    private final HashSet<FileSource> selectedMoveItems;

    private final OnPositionItemClickListener<CompositionFileSource> onCompositionClickListener;
    private final OnPositionItemClickListener<FolderFileSource> onFolderClickListener;
    private final OnPositionItemClickListener<FileSource> onLongClickListener;
    private final OnViewItemClickListener<FolderFileSource> onFolderMenuClickListener;
    private final OnPositionItemClickListener<Composition> compositionIconClickListener;

    @Nullable
    private CurrentComposition currentComposition;
    private boolean isCoversEnabled;

    public MusicFileSourceAdapter(RecyclerView recyclerView,
                                  HashSet<FileSource> selectedItems,
                                  HashSet<FileSource> selectedMoveItems,
                                  OnPositionItemClickListener<CompositionFileSource> onCompositionClickListener,
                                  OnPositionItemClickListener<FolderFileSource> onFolderClickListener,
                                  OnPositionItemClickListener<FileSource> onLongClickListener,
                                  OnViewItemClickListener<FolderFileSource> onFolderMenuClickListener,
                                  OnPositionItemClickListener<Composition> compositionIconClickListener) {
        super(recyclerView, new SimpleDiffItemCallback<>(
                FolderHelper::areSourcesTheSame,
                FolderHelper::getChangePayload)
        );
        this.selectedItems = selectedItems;
        this.selectedMoveItems = selectedMoveItems;
        this.onCompositionClickListener = onCompositionClickListener;
        this.onFolderClickListener = onFolderClickListener;
        this.onLongClickListener = onLongClickListener;
        this.onFolderMenuClickListener = onFolderMenuClickListener;
        this.compositionIconClickListener = compositionIconClickListener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        switch (type) {
            case TYPE_MUSIC: {
                return new MusicFileViewHolder(parent,
                        onCompositionClickListener,
                        onLongClickListener,
                        compositionIconClickListener);
            }
            case TYPE_FILE: {
                return new FolderViewHolder(parent,
                        onFolderClickListener,
                        onFolderMenuClickListener,
                        onLongClickListener);
            }
            default: throw new IllegalStateException("unexpected item type: " + type);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        viewHolders.add(holder);

        FileSource fileSource = getItem(position);

        boolean selected = selectedItems.contains(fileSource);
        holder.setSelected(selected);

        boolean selectedToMove = selectedMoveItems.contains(fileSource);
        holder.setSelectedToMove(selectedToMove);

        switch (holder.getItemViewType()) {
            case TYPE_MUSIC: {
                MusicFileViewHolder musicViewHolder = (MusicFileViewHolder) holder;
                CompositionFileSource musicFileSource = (CompositionFileSource) fileSource;
                musicViewHolder.bind(musicFileSource, isCoversEnabled);
                musicViewHolder.showCurrentComposition(currentComposition, false);
                break;
            }
            case TYPE_FILE: {
                FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
                FolderFileSource folderFileSource = (FolderFileSource) fileSource;
                folderViewHolder.bind(folderFileSource);
                break;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }
        FileSource fileSource = getItem(position);
        switch (holder.getItemViewType()) {
            case TYPE_MUSIC: {
                MusicFileViewHolder musicViewHolder = (MusicFileViewHolder) holder;
                CompositionFileSource musicFileSource = (CompositionFileSource) fileSource;
                musicViewHolder.update(musicFileSource, payloads);
                break;
            }
            case TYPE_FILE: {
                FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
                FolderFileSource folderFileSource = (FolderFileSource) fileSource;
                folderViewHolder.update(folderFileSource, payloads);
                break;
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull FileViewHolder holder) {
        super.onViewRecycled(holder);
        viewHolders.remove(holder);
    }

    @Override
    public int getItemViewType(int position) {
        FileSource source = getItem(position);
        if (source instanceof FolderFileSource) {
            return TYPE_FILE;
        } else {
            return TYPE_MUSIC;
        }
    }

    public void setItemSelected(int position) {
        notifyItemChanged(position, ITEM_SELECTED);
    }

    public void setItemUnselected(int position) {
        notifyItemChanged(position, ITEM_UNSELECTED);
    }

    public void setItemsSelected(boolean selected) {
        for (SelectableViewHolder holder: viewHolders) {
            holder.setSelected(selected);
        }
    }

    public void showCurrentComposition(CurrentComposition currentComposition) {
        this.currentComposition = currentComposition;
        for (RecyclerView.ViewHolder holder: viewHolders) {
            if (holder instanceof MusicFileViewHolder) {
                MusicFileViewHolder musicViewHolder = (MusicFileViewHolder) holder;
                musicViewHolder.showCurrentComposition(currentComposition, true);
            }
        }
    }

    public void setCoversEnabled(boolean isCoversEnabled) {
        this.isCoversEnabled = isCoversEnabled;
        for (RecyclerView.ViewHolder holder: viewHolders) {
            if (holder instanceof MusicFileViewHolder) {
                MusicFileViewHolder musicViewHolder = (MusicFileViewHolder) holder;
                musicViewHolder.setCoversVisible(isCoversEnabled);
            }
        }
    }

    public void updateItemsToMove() {
        for (FileViewHolder holder: viewHolders) {
            boolean selectedToMove = selectedMoveItems.contains(holder.getFileSource());
            holder.setSelectedToMove(selectedToMove);
        }
    }
}
