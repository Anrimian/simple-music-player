package com.github.anrimian.musicplayer.ui.settings.folders.view;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ItemExcludedFolderBinding;
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

class ExcludedFolderViewHolder extends BaseViewHolder {

    private ItemExcludedFolderBinding viewBinding;

    private IgnoredFolder folder;

    ExcludedFolderViewHolder(@NonNull ViewGroup parent,
                             Callback<IgnoredFolder> removeClickListener) {
        super(parent, R.layout.item_excluded_folder);
        viewBinding = ItemExcludedFolderBinding.bind(itemView);

        viewBinding.btnRemove.setOnClickListener(v -> removeClickListener.call(folder));

        CompatUtils.setMainButtonStyle(viewBinding.btnRemove);
    }

    void bind(IgnoredFolder folder) {
        this.folder = folder;
        viewBinding.tvFolderName.setText(folder.getRelativePath());
    }
}
