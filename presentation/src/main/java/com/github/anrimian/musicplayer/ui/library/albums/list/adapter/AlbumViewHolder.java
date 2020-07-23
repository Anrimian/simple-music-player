package com.github.anrimian.musicplayer.ui.library.albums.list.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ItemAlbumBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import java.util.List;

import static com.github.anrimian.musicplayer.domain.Payloads.ARTIST;
import static com.github.anrimian.musicplayer.domain.Payloads.COMPOSITIONS_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onLongClick;

public class AlbumViewHolder extends BaseViewHolder {

    private ItemAlbumBinding viewBinding;

    private Album album;

    AlbumViewHolder(@NonNull ViewGroup parent,
                    Callback<Album> itemClickListener,
                    Callback<Album> longClickListener) {
        super(parent, R.layout.item_album);
        viewBinding = ItemAlbumBinding.bind(itemView);

        viewBinding.clickableItem.setOnClickListener(v -> itemClickListener.call(album));
        onLongClick(viewBinding.clickableItem, () -> longClickListener.call(album));
    }

    public void bind(Album album) {
        this.album = album;
        showAlbumName();
        showAdditionalInfo();
        showCover();
    }

    public void update(Album album, List<Object> payloads) {
        this.album = album;
        for (Object payload: payloads) {
            if (payload instanceof List) {
                //noinspection SingleStatementInBlock,unchecked
                update(album, (List) payload);
            }
            if (payload == NAME) {
                showAlbumName();
                continue;
            }
            if (payload == ARTIST) {
                showAdditionalInfo();
                continue;
            }
            if (payload == COMPOSITIONS_COUNT) {
                showAdditionalInfo();
            }
        }
    }

    private void showAlbumName() {
        String name = album.getName();
        viewBinding.tvAlbumName.setText(name);
        viewBinding.clickableItem.setContentDescription(name);
    }

    private void showAdditionalInfo() {
        viewBinding.tvCompositionsCount.setText(FormatUtils.formatAlbumAdditionalInfo(getContext(), album));
    }

    private void showCover() {
        Components.getAppComponent().imageLoader().displayImage(viewBinding.ivMusicIcon,
                album,
                R.drawable.ic_album_placeholder);
    }
}
