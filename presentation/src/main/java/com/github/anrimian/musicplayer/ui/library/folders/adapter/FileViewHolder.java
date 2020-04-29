package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder;

import static androidx.core.graphics.ColorUtils.setAlphaComponent;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

public abstract class FileViewHolder extends SelectableViewHolder {

    public FileViewHolder(@NonNull ViewGroup parent, int layoutResId) {
        super(parent, layoutResId);
    }

    public abstract void setSelectedToMove(boolean selected);

    public abstract FileSource getFileSource();

    @ColorInt
    protected int getMoveSelectionColor() {
        return setAlphaComponent(getColorFromAttr(getContext(), R.attr.colorAccent), 10);
    }
}
