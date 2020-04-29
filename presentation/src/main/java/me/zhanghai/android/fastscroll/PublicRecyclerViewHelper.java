package me.zhanghai.android.fastscroll;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PublicRecyclerViewHelper extends RecyclerViewHelper {
    public PublicRecyclerViewHelper(@NonNull RecyclerView view) {
        super(view);
    }

    @Override
    public int getScrollRange() {
        int range = super.getScrollRange();
        if (range < 3000) {//don't show scrollbar if list is too short
            return 0;
        }
        return range;
    }
}