package com.github.anrimian.musicplayer.ui.library.genres.list.adapter;

import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.domain.Payloads.COMPOSITIONS_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.DURATION;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionsCount;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onLongClick;

public class GenreViewHolder extends BaseViewHolder {

    @BindView(R.id.tv_genre_name)
    TextView tvGenreName;

    @BindView(R.id.tv_additional_info)
    TextView tvAdditionalInfo;

    @BindView(R.id.clickable_item)
    View clickableItem;

    private Genre genre;

    GenreViewHolder(@NonNull ViewGroup parent,
                    Callback<Genre> itemClickListener,
                    Callback<Genre> longClickListener) {
        super(parent, R.layout.item_genre);
        ButterKnife.bind(this, itemView);
        clickableItem.setOnClickListener(v -> itemClickListener.call(genre));
        onLongClick(clickableItem, () -> longClickListener.call(genre));
    }

    public void bind(Genre genre) {
        this.genre = genre;
        showGenreName();
        showAdditionalInfo();
    }

    public void update(Genre album, List<Object> payloads) {
        this.genre = album;
        for (Object payload: payloads) {
            if (payload instanceof List) {
                //noinspection SingleStatementInBlock,unchecked
                update(album, (List) payload);
            }
            if (payload == NAME) {
                showGenreName();
                continue;
            }
            if (payload == COMPOSITIONS_COUNT) {
                showAdditionalInfo();
            }
            if (payload == DURATION) {
                showAdditionalInfo();
            }
        }
    }

    private void showGenreName() {
        tvGenreName.setText(genre.getName());
    }

    private void showAdditionalInfo() {
        SpannableStringBuilder sb = new DescriptionSpannableStringBuilder(getContext());
        sb.append(formatCompositionsCount(
                getContext(),
                genre.getCompositionsCount())
        );
        long totalDuration = genre.getTotalDuration();
        if (totalDuration != 0) {
            sb.append(formatMilliseconds(totalDuration));
        }
        tvAdditionalInfo.setText(sb);
    }
}
