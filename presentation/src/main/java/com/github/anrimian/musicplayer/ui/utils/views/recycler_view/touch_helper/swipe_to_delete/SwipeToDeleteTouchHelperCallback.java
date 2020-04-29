package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.swipe_to_delete;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.utils.functions.Callback;

public class SwipeToDeleteTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    private final Paint paint = new Paint();

    private final Callback<Integer> swipeCallback;

    public SwipeToDeleteTouchHelperCallback(@ColorInt int color, Callback<Integer> swipeCallback) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.swipeCallback = swipeCallback;
        paint.setColor(color);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        swipeCallback.call(viewHolder.getAdapterPosition());
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return defaultValue * 8;
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.33f;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;

            float top = itemView.getTop();
            float bottom = itemView.getBottom();
            float left = dX > 0? itemView.getLeft() : itemView.getRight() + dX;
            float right = dX > 0? dX : itemView.getRight();

            c.drawRect(left, top, right, bottom, paint);

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
