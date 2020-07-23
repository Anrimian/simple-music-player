package com.github.anrimian.musicplayer.ui.library.compositions.adapter;

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ItemStorageMusicBinding;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder;

import java.util.List;

import javax.annotation.Nonnull;

import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getPlayingCompositionColor;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateBackgroundColor;

/**
 * Created on 31.10.2017.
 */

public class MusicViewHolder extends SelectableViewHolder {

    private FrameLayout clickableItem;

    private CompositionItemWrapper compositionItemWrapper;

    private Composition composition;

    private boolean selected = false;
    private boolean isCurrent = false;

    public MusicViewHolder(ViewGroup parent,
                           OnPositionItemClickListener<Composition> onCompositionClickListener,
                           OnPositionItemClickListener<Composition> onLongClickListener,
                           OnPositionItemClickListener<Composition> iconClickListener) {
        super(parent, R.layout.item_storage_music);
        ItemStorageMusicBinding binding = ItemStorageMusicBinding.bind(itemView);
        clickableItem = binding.clickableItem;

        compositionItemWrapper = new CompositionItemWrapper(itemView,
                o -> iconClickListener.onItemClick(getAdapterPosition(), composition),
                composition -> onCompositionClickListener.onItemClick(getAdapterPosition(), composition)
        );

        if (onLongClickListener != null) {
            clickableItem.setOnLongClickListener(v -> {
                if (selected) {
                    return false;
                }
                selectImmediate();
                onLongClickListener.onItemClick(getAdapterPosition(), composition);
                return true;
            });
        }
    }

    public void bind(@Nonnull Composition composition, boolean isCoversEnabled) {
        this.composition = composition;
        compositionItemWrapper.bind(composition, isCoversEnabled);
    }

    public void update(Composition composition, List<Object> payloads) {
        this.composition = composition;
        compositionItemWrapper.update(composition, payloads);
    }

    public void setCoversVisible(boolean isCoversEnabled) {
        compositionItemWrapper.showCompositionImage(isCoversEnabled);
    }

    @Override
    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            int unselectedColor = (!selected && isCurrent)? getPlaySelectionColor(): Color.TRANSPARENT;
            int selectedColor = getSelectionColor();
            int endColor = selected ? selectedColor : unselectedColor;
            animateBackgroundColor(clickableItem, endColor);
        }
    }

    public void showCurrentComposition(@Nullable CurrentComposition currentComposition,
                                       boolean animate) {
        boolean isCurrent = false;
        boolean isPlaying = false;
        if (currentComposition != null) {
            isCurrent = composition.equals(currentComposition.getComposition());
            isPlaying = isCurrent && currentComposition.isPlaying();
        }
        showAsCurrentComposition(isCurrent);
        compositionItemWrapper.showAsPlaying(isPlaying, animate);
    }

    public Composition getComposition() {
        return composition;
    }

    private void showAsCurrentComposition(boolean isCurrent) {
        if (this.isCurrent != isCurrent) {
            this.isCurrent = isCurrent;
            if (!selected) {
                int unselectedColor = Color.TRANSPARENT;
                int selectedColor = getPlaySelectionColor();
                int endColor = isCurrent ? selectedColor : unselectedColor;
                animateBackgroundColor(clickableItem, endColor);
            }
        }
    }

    private void selectImmediate() {
        clickableItem.setBackgroundColor(getSelectionColor());
        selected = true;
    }

    @ColorInt
    private int getPlaySelectionColor() {
        return getPlayingCompositionColor(getContext(), 20);
    }
}
