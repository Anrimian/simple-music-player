package com.github.anrimian.musicplayer.ui.library.compositions.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
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

public class CompositionsAdapter extends DiffListAdapter<Composition, MusicViewHolder> {

    private final Set<MusicViewHolder> viewHolders = new HashSet<>();

    private final HashSet<Composition> selectedCompositions;
    private final OnPositionItemClickListener<Composition> onCompositionClickListener;
    private final OnPositionItemClickListener<Composition> onLongClickListener;
    private final OnPositionItemClickListener<Composition> iconClickListener;

    @Nullable
    private CurrentComposition currentComposition;
    private boolean isCoversEnabled;

    public CompositionsAdapter(RecyclerView recyclerView,
                               HashSet<Composition> selectedCompositions,
                               OnPositionItemClickListener<Composition> onCompositionClickListener,
                               OnPositionItemClickListener<Composition> onLongClickListener,
                               OnPositionItemClickListener<Composition> iconClickListener) {
        super(recyclerView, new SimpleDiffItemCallback<>(
                CompositionHelper::areSourcesTheSame,
                CompositionHelper::getChangePayload)
        );
        this.selectedCompositions = selectedCompositions;
        this.onCompositionClickListener = onCompositionClickListener;
        this.onLongClickListener = onLongClickListener;
        this.iconClickListener = iconClickListener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MusicViewHolder(parent,
                onCompositionClickListener,
                onLongClickListener,
                iconClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        viewHolders.add(holder);

        Composition composition = getItem(position);
        holder.bind(composition, isCoversEnabled);
        boolean selected = selectedCompositions.contains(composition);
        holder.setSelected(selected);

        holder.showCurrentComposition(currentComposition, false);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        for (Object payload: payloads) {
            if (payload == ITEM_SELECTED) {
                holder.setSelected(true);
                return;
            }
            if (payload == ITEM_UNSELECTED) {
                holder.setSelected(false);
                return;
            }
        }
        holder.update(getItem(position), payloads);
    }

    @Override
    public void onViewRecycled(@NonNull MusicViewHolder holder) {
        super.onViewRecycled(holder);
        viewHolders.remove(holder);
    }

    public void setItemSelected(int position) {
        notifyItemChanged(position, ITEM_SELECTED);
    }

    public void setItemUnselected(int position) {
        notifyItemChanged(position, ITEM_UNSELECTED);
    }

    public void setItemsSelected(boolean selected) {
        for (MusicViewHolder holder: viewHolders) {
            holder.setSelected(selected);
        }
    }

    public void showCurrentComposition(CurrentComposition currentComposition) {
        this.currentComposition = currentComposition;
        for (MusicViewHolder holder: viewHolders) {
            holder.showCurrentComposition(currentComposition, true);
        }
    }

    public void setCoversEnabled(boolean isCoversEnabled) {
        this.isCoversEnabled = isCoversEnabled;
        for (MusicViewHolder holder: viewHolders) {
            holder.setCoversVisible(isCoversEnabled);
        }
    }
}
