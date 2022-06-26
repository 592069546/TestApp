package com.example.pagingtest.ui;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;

import android.graphics.Canvas;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pagingtest.view.SwipeLayout;

public class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private final static String TAG = ItemTouchHelperCallback.class.getSimpleName();

    private RecyclerView.ViewHolder activeViewHolder;

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int swipeDirection = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(0, swipeDirection);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 1.1f;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return Integer.MAX_VALUE;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ACTION_STATE_SWIPE && isCurrentlyActive) {
//            int menuWidth = (int) (viewHolder.itemView.getWidth() * 0.3f);
//            Log.d(TAG, "dx: " + dX + " width: " + viewHolder.itemView.getWidth());
//            if (Math.abs(dX) > menuWidth) {     //限制itemView滑动距离
//                dX = dX >= 0 ? menuWidth : -menuWidth;
//            }
//            viewHolder.itemView.setTranslationX(dX);
            Log.d(TAG, "****** actionState: " + actionState + " dx: " + dX + " isCurrent: " + isCurrentlyActive);
            SwipeLayout layout = getSwipeLayout(viewHolder);
            if (layout != null) {
                layout.updateOffset(dX);
                return;
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState == ACTION_STATE_SWIPE) {    //关闭上一个打开的menu
            if (viewHolder != activeViewHolder) {
                SwipeLayout layout = getSwipeLayout(viewHolder);
                if (layout != null)
                    layout.closeMenu(true);
                activeViewHolder = viewHolder;
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
    }

    @Nullable
    private SwipeLayout getSwipeLayout(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder != null && viewHolder.itemView instanceof SwipeLayout) {
            return (SwipeLayout) viewHolder.itemView;
        }
        return null;
    }
}
