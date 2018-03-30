package com.shibedays.workoutplanner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.adapters.PendingRemovalAdapter;

import javax.crypto.spec.DESedeKeySpec;

public class ListItemTouchHelper {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = ListItemTouchHelper.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ListItemTouchHelper.";
    //endregion

    private Context mContext;

    private int mDragDirs;
    private int mSwipeDirs;

    private boolean mDraggable;
    private boolean mSwipeable;

    private Drawable mBackground;
    private Drawable mDeleteIC;
    private int mDeleteICMargin;

    private PendingRemovalAdapter mAdapter;
    //region INTERFACES
    public interface SwapItems{
        void swap(int from, int to);
    }
    private SwapItems mListener;
    //endregion

    // Constructor
    public ListItemTouchHelper(Context context, boolean draggable,
                               int drag, boolean swipeable, int swipe,
                                RecyclerView.Adapter adapter){
        mContext = context;

        mAdapter = (PendingRemovalAdapter) adapter;

        if(context instanceof SwapItems){
            mListener = (SwapItems) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement SwapItems");
        }

        mDraggable = draggable;
        mDragDirs = drag;

        mSwipeable = swipeable;
        mSwipeDirs = swipe;

        mBackground = new ColorDrawable(Color.RED);
        mDeleteIC = context.getDrawable(R.drawable.ic_delete_white_24dp);
        mDeleteICMargin = (int) context.getResources().getDimension(R.dimen.standard_icon_touchable_padding);
    }


    // Get Helper
    public ItemTouchHelper getHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(mDragDirs, mSwipeDirs) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                if(mDraggable){
                    int from = viewHolder.getAdapterPosition();
                    int to = target.getAdapterPosition();
                    mListener.swap(from, to);
                    mAdapter.notifyItemMoved(from, to);
                }
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int itemPos = viewHolder.getAdapterPosition();
                if(mAdapter.isPendingRemoval(itemPos)){
                    return 0;
                } else {
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if(mSwipeable) {
                    int swipedPos = viewHolder.getAdapterPosition();
                    mAdapter.pendingRemoval(swipedPos);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // This also gets called for viewholders that are already swiped away, so handle for that
                if(viewHolder.getAdapterPosition() < 0){
                    return;
                }

                //if dX > 0, swiping right
                //if dX < 0 swiping left
                if(dX < 0) { // swiping left
                    // draw the background for the child view
                    // The background bounds will be from the edge of the view to the edge of the device
                    mBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    mBackground.draw(c);

                    // Draw the relevent icon
                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    //TODO: What's intristic mean
                    int intristicHeight = mDeleteIC.getIntrinsicHeight();
                    int intristicWidth = mDeleteIC.getIntrinsicWidth();

                    int deleteICLeft = itemView.getRight() - mDeleteICMargin - intristicWidth;
                    int deleteICRight = itemView.getRight() - mDeleteICMargin;
                    int deleteICTop = itemView.getTop() + (itemHeight - intristicHeight) / 2; // divide by 2 to get the center
                    int deleteICBottom = deleteICTop + intristicHeight;
                    mDeleteIC.setBounds(deleteICLeft, deleteICTop, deleteICRight, deleteICBottom);

                    mDeleteIC.draw(c);
                } else if (dX > 0) { // swiping right
                    // draw the background for the child view
                    // The background bounds will be from the edge of the view to the edge of the device
                    //background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),itemView.getRight(), itemView.getBottom());
                    mBackground.setBounds(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + (int) dX, itemView.getBottom());
                    mBackground.draw(c);

                    // Draw the relevent icon
                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    //TODO: What's intristic mean
                    int intristicHeight = mDeleteIC.getIntrinsicHeight();
                    int intristicWidth = mDeleteIC.getIntrinsicWidth();

                    int deleteICLeft = itemView.getLeft() + mDeleteICMargin;
                    int deleteICRight = itemView.getLeft() + mDeleteICMargin + intristicWidth;
                    int deleteICTop = itemView.getTop() + (itemHeight - intristicHeight) / 2; // divide by 2 to get the center
                    int deleteICBottom = deleteICTop + intristicHeight;
                    mDeleteIC.setBounds(deleteICLeft, deleteICTop, deleteICRight, deleteICBottom);

                    mDeleteIC.draw(c);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        });


    }
}
