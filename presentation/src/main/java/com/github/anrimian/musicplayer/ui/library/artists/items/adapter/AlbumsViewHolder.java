package com.github.anrimian.musicplayer.ui.library.artists.items.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.library.artists.items.adapter.albums.AlbumsAdapter;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumsViewHolder extends BaseViewHolder {

    @BindView(R.id.rv_albums)
    RecyclerView rvAlbums;

    @BindView(R.id.tv_songs_title)
    TextView tvSongsTitle;

    private AlbumsAdapter albumsAdapter;

    AlbumsViewHolder(@NonNull ViewGroup parent,
                     Callback<Album> itemClickListener,
                     Callback<Boolean> albumsScrollStateCallback) {
        super(parent, R.layout.item_albums_horizontal);
        ButterKnife.bind(this, itemView);

        rvAlbums.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        rvAlbums.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                albumsScrollStateCallback.call(rvAlbums.computeHorizontalScrollOffset() == 0);
            }
        });

        albumsAdapter = new AlbumsAdapter(rvAlbums, itemClickListener);
        rvAlbums.setAdapter(albumsAdapter);
    }

    void submitList(List<Album> list) {
        albumsAdapter.submitList(list);

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        if (list.isEmpty()) {
            itemView.setVisibility(View.GONE);
            params.width = 0;
            params.height = 0;
        } else {
            itemView.setVisibility(View.VISIBLE);
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        itemView.setLayoutParams(params);
    }

    void setCompositionsTitleVisible(boolean visible) {
        tvSongsTitle.setVisibility(visible? View.VISIBLE : View.GONE);
    }
}
