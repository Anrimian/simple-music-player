package com.github.anrimian.musicplayer.ui.library.artists.list.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.domain.Payloads.COMPOSITIONS_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onLongClick;

public class ArtistViewHolder extends BaseViewHolder {

    @BindView(R.id.tv_artist_name)
    TextView tvAuthorName;

    @BindView(R.id.tv_additional_info)
    TextView tvAdditionalInfo;

    @BindView(R.id.clickable_item)
    View clickableItem;

    private Artist artist;

    ArtistViewHolder(@NonNull ViewGroup parent,
                     Callback<Artist> itemClickListener,
                     Callback<Artist> longClickListener) {
        super(parent, R.layout.item_artist);
        ButterKnife.bind(this, itemView);
        clickableItem.setOnClickListener(v -> itemClickListener.call(artist));
        onLongClick(clickableItem, () -> longClickListener.call(artist));
    }

    public void bind(Artist artist) {
        this.artist = artist;
        showAuthorName();
        showCompositionsCount();
    }

    public void update(Artist artist, List<Object> payloads) {
        this.artist = artist;
        for (Object payload: payloads) {
            if (payload instanceof List) {
                //noinspection SingleStatementInBlock,unchecked
                update(artist, (List) payload);
            }
            if (payload == NAME) {
                showAuthorName();
                continue;
            }
            if (payload == COMPOSITIONS_COUNT) {
                showCompositionsCount();
            }
        }
    }

    private void showAuthorName() {
        String name = artist.getName();
        tvAuthorName.setText(name);
        clickableItem.setContentDescription(name);
    }

    private void showCompositionsCount() {
        tvAdditionalInfo.setText(FormatUtils.formatArtistAdditionalInfo(getContext(), artist));
    }
}
