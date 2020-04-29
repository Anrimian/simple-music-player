package com.github.anrimian.musicplayer.ui.editor.composition.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.domain.Payloads.NAME;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onLongClick;

class GenreViewHolder extends BaseViewHolder {

    @BindView(R.id.tv_genre)
    TextView tvGenre;

    @BindView(R.id.iv_remove)
    ImageView ivRemove;

    @BindView(R.id.chip_container)
    View chipContainer;

    private ShortGenre genre;

    GenreViewHolder(@NonNull ViewGroup parent,
                    Callback<ShortGenre> onClickListener,
                    Callback<ShortGenre> onLongClickListener,
                    Callback<ShortGenre> onRemoveClickListener) {
        super(parent, R.layout.item_genre_chip);
        ButterKnife.bind(this, itemView);
        chipContainer.setOnClickListener(v -> onClickListener.call(genre));
        onLongClick(chipContainer, () -> onLongClickListener.call(genre));
        ivRemove.setOnClickListener(v -> onRemoveClickListener.call(genre));
    }

    void bind(ShortGenre genre) {
        this.genre = genre;
        showName();
    }

    void update(ShortGenre genre, List<Object> payloads) {
        this.genre = genre;
        for (Object payload: payloads) {
            if (payload instanceof List) {
                //noinspection SingleStatementInBlock,unchecked
                update(genre, (List) payload);
            }
            if (payload == NAME) {
                showName();
            }
        }
    }

    private void showName() {
        tvGenre.setText(genre.getName());
    }
}
