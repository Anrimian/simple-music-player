package com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ItemPlayListBinding;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;

import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionsCount;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;

class PlayListViewHolder extends RecyclerView.ViewHolder {

    private ItemPlayListBinding viewBinding;

    private PlayList playList;

    PlayListViewHolder(LayoutInflater inflater,
                       ViewGroup parent,
                       OnItemClickListener<PlayList> onItemClickListener,
                       OnItemClickListener<PlayList> onItemLongClickListener) {
        super(inflater.inflate(R.layout.item_play_list, parent, false));
        viewBinding = ItemPlayListBinding.bind(itemView);

        if (onItemClickListener != null) {
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(playList));
        }
        if (onItemLongClickListener != null) {
            itemView.setOnLongClickListener(v -> {
                onItemLongClickListener.onItemClick(playList);
                return true;
            });
        }
    }

    void bind(PlayList playList) {
        this.playList = playList;
        viewBinding.tvPlayListName.setText(playList.getName());
        showAdditionalInfo();
    }

    private void showAdditionalInfo() {
        int compositionsCount = playList.getCompositionsCount();
        SpannableStringBuilder sb = new DescriptionSpannableStringBuilder(getContext(),
                formatCompositionsCount(getContext(), compositionsCount)
        );
        sb.append(formatMilliseconds(playList.getTotalDuration()));
        viewBinding.tvAdditionalInfo.setText(sb);
    }

    private Context getContext() {
        return itemView.getContext();
    }
}
