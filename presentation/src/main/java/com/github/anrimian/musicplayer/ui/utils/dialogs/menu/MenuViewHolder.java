package com.github.anrimian.musicplayer.ui.utils.dialogs.menu;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

class MenuViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.iv_icon)
    ImageView ivIcon;

    private MenuItem menuItem;

    MenuViewHolder(LayoutInflater inflater,
                   ViewGroup parent,
                   @LayoutRes int menuViewRes,
                   OnItemClickListener<MenuItem> onItemClickListener) {
        super(inflater.inflate(menuViewRes, parent, false));
        ButterKnife.bind(this, itemView);

        itemView.setOnClickListener(v -> onItemClickListener.onItemClick(menuItem));
    }

    void bind(MenuItem menuItem) {
        this.menuItem = menuItem;
        tvTitle.setText(menuItem.getTitle());

        ivIcon.setColorFilter(tvTitle.getCurrentTextColor());
        ivIcon.setImageDrawable(menuItem.getIcon());
        ivIcon.setContentDescription(menuItem.getTitle());
    }

}
