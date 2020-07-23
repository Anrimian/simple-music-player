package com.github.anrimian.musicplayer.ui.library.folders.wrappers;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.databinding.PartialStorageHeaderBinding;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

/**
 * Created on 01.11.2017.
 */

public class HeaderViewWrapper {

    private PartialStorageHeaderBinding viewBinding;

    public HeaderViewWrapper(PartialStorageHeaderBinding viewBinding) {
        this.viewBinding = viewBinding;
    }

    public void bind(@NonNull FolderFileSource folder) {
        viewBinding.tvParentPath.setText(folder.getName());
    }

    public void setOnClickListener(OnClickListener listener) {
        viewBinding.headerClickableItem.setOnClickListener(listener);
    }

    public void setVisible(boolean visible) {
        viewBinding.getRoot().setVisibility(visible? VISIBLE: GONE);
    }

}
