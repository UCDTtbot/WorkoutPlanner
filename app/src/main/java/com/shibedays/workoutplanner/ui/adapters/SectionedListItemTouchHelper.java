package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class SectionedListItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = SectionedListItemTouchHelper.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.SectionedListItemTouchHelper.";

    private static final int BUTTON_WIDTH = 204; //PIXEL WIDTH PER BUTTON
    //endregion

    private Context mContext;

    private List<UnderlayButton> mButtons;

    private GestureDetector mGestureDetector;

    private int mSwipedPos = -1;
    private float mSwipedThreshold = 0.5f;
    private Map<Integer, List<UnderlayButton>> mButtonsBuffer;
    private Queue<Integer> mRecoverQueue;

    private int mDragDirs;
    private int mSwipeDirs;

    private boolean mDraggable;
    private boolean mSwipeable;

    private RecyclerView mRecyclerView;
    private SectionedPendingRemovalAdapter mAdapter;


    //region INTERFACES

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            for(UnderlayButton button : mButtons){
                if(button.onClick(e.getX(), e.getY())) {
                    break;
                }
            }
            return true;
        }
    };

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(mSwipedPos < 0) {
                return false;
            }
            Point point = new Point((int) event.getRawX(), (int) event.getRawY());

            RecyclerView.ViewHolder swipedViewHolder = mRecyclerView.findViewHolderForAdapterPosition(mSwipedPos);
            View swipedItem = swipedViewHolder.itemView;
            Rect rect = new Rect();
            swipedItem.getGlobalVisibleRect(rect);

            if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_MOVE){
                if(rect.top < point.y && rect.bottom > point.y){
                    mGestureDetector.onTouchEvent(event);
                    if(event.getAction() == MotionEvent.ACTION_UP){
                    }
                } else {
                    Log.d(DEBUG_TAG, "mGestureDetector onTouchEvent ELSE STATEMENT");
                    //TODO: Close other open swipers
                }
            }
            return false;
        }
    };

    public interface SwapItems {
        void swap(int from, int to);
    }

    private SwapItems mListener;
    //endregion

    // Constructor


    public SectionedListItemTouchHelper(Context context, boolean draggable, int dragDirs, boolean swipeable, RecyclerView recyclerView) {
        super(dragDirs, ItemTouchHelper.LEFT);
        mContext = context;

        mRecyclerView = recyclerView;
        mRecyclerView.setOnTouchListener(mOnTouchListener);
        mAdapter = (SectionedPendingRemovalAdapter) recyclerView.getAdapter();

        if (context instanceof SwapItems) {
            mListener = (SwapItems) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement SwapItems");
        }

        mButtons = new ArrayList<>();
        mButtonsBuffer = new HashMap<>();

        mRecoverQueue = new LinkedList<Integer>(){
            @Override
            public boolean add(Integer i) {
                if(contains(i)) {
                    return false;
                } else {
                    return super.add(i);
                }
            }
        };

        mGestureDetector = new GestureDetector(context, mGestureListener);

        mDraggable = draggable;
        mDragDirs = dragDirs;

        mSwipeable = swipeable;
        mSwipeDirs = ItemTouchHelper.LEFT;

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (mDraggable) {
            int from = viewHolder.getAdapterPosition();
            int to = target.getAdapterPosition();
            mListener.swap(from, to);
            mAdapter.notifyItemMoved(from, to);
        }
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (mSwipeable) {
            int pos = viewHolder.getAdapterPosition();

            if(mSwipedPos != pos){
                mRecoverQueue.add(mSwipedPos);
            }

            mSwipedPos = pos;

            if(mButtonsBuffer.containsKey(mSwipedPos)){
                mButtons = mButtonsBuffer.get(mSwipedPos);
            } else {
                mButtons.clear();
            }

            mButtonsBuffer.clear();
            mSwipedThreshold = 0.5f * mButtons.size() * BUTTON_WIDTH;
        }
    }

    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return mSwipedThreshold;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 1.25f * defaultValue;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 5.0f * defaultValue;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int pos = viewHolder.getAdapterPosition();
        float translationX = dX;
        View itemView = viewHolder.itemView;

        if(pos < 0){
            mSwipedPos = pos;
            return;
        }

        if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
            if(dX < 0){
                List<UnderlayButton> buffer = new ArrayList<>();

                if(!mButtonsBuffer.containsKey(pos)){
                    instantiateUnderlayButton(viewHolder, mContext, buffer);
                    mButtonsBuffer.put(pos, buffer);
                } else {
                    buffer = mButtonsBuffer.get(pos);
                }

                translationX = dX * buffer.size() * BUTTON_WIDTH / itemView.getWidth();
                drawButtons(c, itemView, buffer, pos, translationX);
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }

    private void drawButtons(Canvas c, View itemView, List<UnderlayButton> buffer, int pos, float dX){
        float right = itemView.getRight();
        float dButtonWidth = (-1) * dX / buffer.size();

        for(UnderlayButton button : buffer){
            float left = right - dButtonWidth;
            button.onDraw(c, new RectF(left, itemView.getTop(), right, itemView.getBottom()), pos, dX);
            right = left;
        }
    }

    public abstract void instantiateUnderlayButton(final RecyclerView.ViewHolder viewHolder, Context context, List<UnderlayButton> underlayButtons);

    public static class UnderlayButton {
        Drawable mDeleteIc;
        Drawable mBackground;
        float mDeleteMargin;
        Context mContext;
        private int mColor;
        private int mPos;
        private RectF mClickRegion;
        private UnderlayButtonClickListener mClickListener;

        public UnderlayButton(int imageId, int color, Context context, UnderlayButtonClickListener clickListener){
            mContext = context;
            mColor = color;
            mDeleteIc = context.getResources().getDrawable(imageId, null);
            mDeleteMargin = 72;
            mBackground = new ColorDrawable(mColor);
            mClickListener = clickListener;
        }

        public boolean onClick(float x, float y){
            if(mClickRegion != null && mClickRegion.contains(x, y)){
                mClickListener.onDeleteButtonClick(mPos);
                return true;
            }
            return false;
        }

        public void onDraw(Canvas c, RectF rect, int pos, float dX){
            // draw the background for the child view
            // The background bounds will be from the edge of the view to the edge of the device
            mBackground.setBounds( (int) (rect.right + dX) , (int) rect.top, (int) rect.right, (int) rect.bottom);
            mBackground.draw(c);
            // Draw the relevent icon

            float left = rect.left;
            float top = rect.top;
            float right = rect.right;
            float bottom = rect.bottom;
            int icHeight = mDeleteIc.getIntrinsicHeight();
            int icWidth = mDeleteIc.getIntrinsicWidth();

            float icTop = (rect.top + (rect.height() - icHeight) / 2);
            mDeleteIc.setBounds( (int) ((rect.right - mDeleteMargin - icWidth)), (int) icTop, (int) (rect.right - mDeleteMargin), (int) icTop + icHeight);
            mDeleteIc.draw(c);


            mClickRegion = rect;
            mPos = pos;
        }
    }

    public interface UnderlayButtonClickListener{
        void onDeleteButtonClick(int absolutePos);
    }
}

//region OLD_CHILD_DRAW

            /*
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
            */
//endregion
