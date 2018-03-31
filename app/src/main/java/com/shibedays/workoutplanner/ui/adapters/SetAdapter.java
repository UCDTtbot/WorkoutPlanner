package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.shibedays.androidswipelayout.SimpleSwipeListener;
import com.shibedays.androidswipelayout.SwipeLayout;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class SetAdapter extends PendingRemovalAdapter<SetAdapter.SetViewHolder> {

    //region CONSTANTS
    private static final int PENDING_REMOVAL_TIMEOUT = 4000; // LENGTH_LONG is defined as 3500, so lets put 4000 just in case
    private static final String DEBUG_TAG = SetAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.SetAdapter.";
    //endregion

    //region VIEW_HOLDER
    class SetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        // Data
        private Set curSet;
        // Swipe
        private SwipeLayout swipeLayout;
        // Foreground
        private TextView setNameTextView;
        private TextView descripTextView;
        private TextView timeTextView;
        // Background
        private ImageView delIcon;

        public SetViewHolder(View itemView) {
            super(itemView);
            //Initialize the views for the RecyclerView
            setNameTextView = itemView.findViewById(R.id.set_name);
            //descripTextView = itemView.findViewById(R.id.set_descrip);
            timeTextView = itemView.findViewById(R.id.set_time);
            delIcon = itemView.findViewById(R.id.set_trash);

            swipeLayout = itemView.findViewById(R.id.set_swipe);
            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, itemView.findViewById(R.id.set_list_background));

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void bindTo(final Set curSet){
            //Populate data when they bind the workouts to the view holder
            this.curSet = curSet;

            setNameTextView.setText(curSet.getName());
            descripTextView.setText(curSet.getDescrip());

            int[] time = MainActivity.convertFromMillis(curSet.getTime());
            int minutes = time[0], seconds = time[1];

            if(seconds == 0){
                timeTextView.setText(String.format(Locale.US, "%d:%d%d", minutes, seconds, 0));
            } else if( seconds < 10){
                timeTextView.setText(String.format(Locale.US, "%d:%d%d", minutes, 0, seconds));
            }
            else {
                timeTextView.setText(String.format(Locale.US, "%d:%d", minutes, seconds));
            }

            swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            swipeLayout.addSwipeListener(new SimpleSwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    mItemManager.closeAllExcept(layout);
                    super.onStartOpen(layout);
                }
            });
            swipeLayout.getSurfaceView().setOnClickListener(this);
            delIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pendingRemoval(mSetData.indexOf(curSet));
                }
            });
        }

        @Override
        public void onClick(View v) {
            mListener.onSetClick(mSetData.indexOf(curSet));
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Set> mSetData;
    private List<Set> mSetsPendingRemoval;
    // UI Components
    private CoordinatorLayout mCoordLayout;
    private Context mContext;
    // Threading Components
    private Handler handler = new Handler();
    private HashMap<Set, Runnable> pendingRunnables = new HashMap<>();
<<<<<<< HEAD
    // Swiping
    private Boolean mCanSwipe;
=======

    private SwipeItemRecyclerMangerImpl mItemManager = new SwipeItemRecyclerMangerImpl(this);
>>>>>>> parent of 066fa36... Fixed Item Dragging for the new recyclers

    //endregion

    //region INTERFACES
    public interface SetAdapaterListener{
        void onSetClick(int setIndex);
        void deleteSet(Set set);
    }
    private SetAdapaterListener mListener;
    //endregion

    //region LIFECYCLE
    public SetAdapter(Context context, View coordLayout){
        mSetsPendingRemoval = new ArrayList<>();
        mContext = context;
        if(coordLayout instanceof CoordinatorLayout){
            mCoordLayout = (CoordinatorLayout)coordLayout;
        } else {
            throw new RuntimeException(SetAdapter.class.getSimpleName() + " was passed a non-coordinate layout view");
        }

        if(context instanceof SetAdapaterListener){
            mListener = (SetAdapaterListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement SetAdapterListener");
        }
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SetViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_set_items, parent, false));
    }

    @Override
    public void onBindViewHolder(SetViewHolder viewHolder, int position) {
        // Get the current data
        Set currentSet = mSetData.get(position);
        viewHolder.bindTo(currentSet);
        mItemManager.bind(viewHolder.itemView, position);
        // Populate anymore data
    }

    //endregion

    //region UTILITY

    @Override
    public int getItemCount() {
        if(mSetData != null) {
            return mSetData.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.set_swipe;
    }

    public void setData(List<Set> data){
        mSetData = data;
        if(mSetsPendingRemoval.size() > 0){
            for(int i = 0; i < mSetsPendingRemoval.size(); i++){
                Set set = mSetsPendingRemoval.get(i);
                if (mSetData.contains(set)){
                    mSetData.remove(set);
                }
            }
        }
        notifyDataSetChanged();
    }
    //endregion

    //region PENDING_DELETE
    public void pendingRemoval(final int swipedPos){
        final Set set = mSetData.get(swipedPos);
        final int origPos = swipedPos;
        if(!mSetsPendingRemoval.contains(set)){
            mSetsPendingRemoval.add(set);
            final int pendingPos = mSetsPendingRemoval.indexOf(set);
            mSetData.remove(swipedPos);
            notifyItemRemoved(swipedPos);
            notifyItemRangeChanged(swipedPos, mSetData.size());
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    deletePending(mSetsPendingRemoval.indexOf(set), origPos);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(set, pendingRemovalRunnable);

            Snackbar undoBar = Snackbar.make(mCoordLayout, "Undo", Snackbar.LENGTH_LONG);
            undoBar.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    undo(swipedPos, pendingPos);
                }
            });
            undoBar.show();
        }
    }

    @Override
    public void deletePending(int pendingIndex, int origPos) {
        mSetsPendingRemoval.remove(pendingIndex);

        mListener.deleteSet(mSetData.get(origPos));
        Log.d(DEBUG_TAG, "Removed pending set");
    }


    public boolean isPendingRemoval(int pos){
        Set set = mSetData.get(pos);
        return mSetsPendingRemoval.contains(set);
    }

    @Override
    public void undo(int itemPos, int pendingPos) {
        if(mSetsPendingRemoval.get(pendingPos) != null){
            Set set = mSetsPendingRemoval.get(pendingPos);
            Runnable pendingRunnable = pendingRunnables.get(set);
            pendingRunnables.remove(set);
            if(pendingRunnable != null){
                handler.removeCallbacks(pendingRunnable);
            }
            mSetsPendingRemoval.remove(pendingPos);
            mSetData.add(itemPos, set);
            notifyItemInserted(itemPos);
        }
    }
    //endregion



}
