package com.github.anrimian.musicplayer.ui.library.artists.items.adapter.albums;

import android.text.SpannableStringBuilder;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ItemAlbumHorizontalBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import java.util.List;

import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.domain.Payloads.COMPOSITIONS_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionsCount;

public class AlbumViewHolder extends BaseViewHolder {

    private ItemAlbumHorizontalBinding viewBinding;

    private Album album;

    AlbumViewHolder(@NonNull ViewGroup parent,
                    Callback<Album> itemClickListener) {
        super(parent, R.layout.item_album_horizontal);
        viewBinding = ItemAlbumHorizontalBinding.bind(itemView);

        itemView.setOnClickListener(v -> itemClickListener.call(album));
    }

    public void bind(Album album) {
        this.album = album;
        showAlbumName();
        showCompositionsCount();
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
            if (payload == COMPOSITIONS_COUNT) {
                showCompositionsCount();
            }
        }
    }

    private void showAlbumName() {
        viewBinding.tvAlbumName.setText(album.getName());
    }

    private void showCompositionsCount() {
        SpannableStringBuilder sb = new DescriptionSpannableStringBuilder(getContext());
        String artist = album.getArtist();
        if (!isEmpty(artist)) {
            sb.append(artist);
        }
        sb.append(formatCompositionsCount(
                getContext(),
                album.getCompositionsCount())
        );
        viewBinding.tvCompositionsCount.setText(sb);
    }

    private void showCover() {
        Components.getAppComponent().imageLoader().displayImage(viewBinding.ivMusicIcon,
                album,
                R.drawable.ic_album_placeholder);
    }
}
