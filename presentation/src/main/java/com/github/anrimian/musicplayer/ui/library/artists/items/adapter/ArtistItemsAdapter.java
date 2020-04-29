package com.github.anrimian.musicplayer.ui.library.artists.items.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.library.compositions.adapter.MusicViewHolder;
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

public class ArtistItemsAdapter extends DiffListAdapter<Object, RecyclerView.ViewHolder> {

    private static final int TYPE_ALBUMS = 1;
    private static final int TYPE_COMPOSITIONS = 2;

    private final Set<MusicViewHolder> viewHolders = new HashSet<>();

    private final HashSet<Composition> selectedCompositions;
    private final OnPositionItemClickListener<Composition> onCompositionClickListener;
    private final OnPositionItemClickListener<Composition> onLongClickListener;
    private final OnPositionItemClickListener<Composition> iconClickListener;
    private final Callback<Album> albumClickListener;
    private final Callback<Boolean> albumsScrollStateCallback;

    @Nullable
    private CurrentComposition currentComposition;
    private boolean isCoversEnabled;

    public ArtistItemsAdapter(RecyclerView recyclerView,
                              HashSet<Composition> selectedCompositions,
                              OnPositionItemClickListener<Composition> onCompositionClickListener,
                              OnPositionItemClickListener<Composition> onLongClickListener,
                              OnPositionItemClickListener<Composition> iconClickListener,
                              Callback<Album> albumClickListener,
                              Callback<Boolean> albumsScrollStateCallback) {
        super(recyclerView, new SimpleDiffItemCallback<>(
                ArtistItemsAdapter::areSourcesTheSame,
                ArtistItemsAdapter::getChangePayload)
        );
        this.selectedCompositions = selectedCompositions;
        this.onCompositionClickListener = onCompositionClickListener;
        this.onLongClickListener = onLongClickListener;
        this.iconClickListener = iconClickListener;
        this.albumClickListener = albumClickListener;
        this.albumsScrollStateCallback = albumsScrollStateCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_COMPOSITIONS: {
                return new MusicViewHolder(parent,
                        wrapPositionListener(onCompositionClickListener),
                        wrapPositionListener(onLongClickListener),
                        wrapPositionListener(iconClickListener)
                );
            }
            case TYPE_ALBUMS: {
                return new AlbumsViewHolder(parent, albumClickListener, albumsScrollStateCallback);
            }
        }
        throw new IllegalStateException("unexpected view type: " + viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder baseHolder, int position) {

        Object item = getItem(position);
        if (item instanceof Composition) {
            Composition composition = (Composition) item;
            MusicViewHolder holder = (MusicViewHolder) baseHolder;
            viewHolders.add(holder);

            holder.bind(composition, isCoversEnabled);
            boolean selected = selectedCompositions.contains(composition);
            holder.setSelected(selected);
            holder.showCurrentComposition(currentComposition, false);
            return;
        }
        if (item instanceof ArtistAlbumsPresenter) {
            ArtistAlbumsPresenter artistAlbumsPresenter = (ArtistAlbumsPresenter) item;
            AlbumsViewHolder holder = (AlbumsViewHolder) baseHolder;
            artistAlbumsPresenter.attachView(holder);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder baseHolder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(baseHolder, position, payloads);
            return;
        }
        Object item = getItem(position);
        if (item instanceof Composition) {
            Composition composition = (Composition) item;
            MusicViewHolder holder = (MusicViewHolder) baseHolder;

            for (Object payload : payloads) {
                if (payload == ITEM_SELECTED) {
                    holder.setSelected(true);
                    return;
                }
                if (payload == ITEM_UNSELECTED) {
                    holder.setSelected(false);
                    return;
                }
            }
            holder.update(composition, payloads);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof Composition) {
            return TYPE_COMPOSITIONS;
        }
        if (item instanceof ArtistAlbumsPresenter) {
            return TYPE_ALBUMS;
        }
        throw new IllegalStateException("unexpected view type: " + position);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof MusicViewHolder) {
            viewHolders.remove(holder);
        }
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

    private static boolean areSourcesTheSame(Object first, Object second) {
        if (first instanceof Composition) {
            return CompositionHelper.areSourcesTheSame((Composition) first, (Composition) second);
        }
        return true;
    }

    private static List<Object> getChangePayload(Object first, Object second) {
        if (first instanceof Composition) {
            return CompositionHelper.getChangePayload((Composition) first, (Composition) second);
        }
        return null;
    }

    private OnPositionItemClickListener<Composition> wrapPositionListener(
            OnPositionItemClickListener<Composition> listener) {
        return (position, data) -> listener.onItemClick(--position, data);
    }
}
