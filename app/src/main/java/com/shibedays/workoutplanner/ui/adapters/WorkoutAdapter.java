package com.shibedays.workoutplanner.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class WorkoutAdapter extends PendingRemovalAdapter<WorkoutAdapter.WorkoutViewHolder> {

    //region CONSTANTS
    // Timeout Constant
    private static final int PENDING_REMOVAL_TIMEOUT = 4000; // LENGTH_LONG is defined as 3500, so lets put 4000 just in case
    // Package and Debug Constants
    private static final String DEBUG_TAG = WorkoutAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.WorkoutAdapter.";
    //endregion

    //region VIEW_HOLDER
    class WorkoutViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView itemName;
        private TextView sets;
        private Workout curWorkout;
        private SwipeLayout swipeLayout;
        private ImageView tempDelete;

        private TextView TEST_POS_ID;
        private TextView TEST_WRK_ID;
        //private TextView rounds;

        public WorkoutViewHolder(View itemView) {
            super(itemView);
            //Initialize the views for the RecyclerView
            itemName = itemView.findViewById(R.id.item_name);
            sets = itemView.findViewById(R.id.item_sets);
            swipeLayout = itemView.findViewById(R.id.workout_swipe);
            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, itemView.findViewById(R.id.workout_list_background));
            tempDelete = itemView.findViewById(R.id.trash);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            //rounds = itemView.findViewById(R.id.item_rounds);

            TEST_POS_ID = itemView.findViewById(R.id.TEST_POS_ID);
            TEST_WRK_ID = itemView.findViewById(R.id.TEST_WRK_ID);
        }

        void bindTo(final Workout curWorkout, final int pos){
            //Populate data when they bind the workouts to the view holder
            itemName.setText(curWorkout.getName());
            sets.setText(String.format(mContext.getString(R.string.item_sets), curWorkout.getNumOfSets()));

            TEST_POS_ID.setText(String.format(Locale.US, "PosID: %1$d", mWorkoutData.indexOf(curWorkout)));
            TEST_WRK_ID.setText(String.format(Locale.US, "WrkID: %1$d", curWorkout.getWorkoutID()));
            this.curWorkout = curWorkout;

            swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            swipeLayout.addSwipeListener(new SimpleSwipeListener());
            swipeLayout.getSurfaceView().setOnClickListener(this);
            tempDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: Add Confirmation Dialog
                    int pos = mWorkoutData.indexOf(curWorkout);
                    Log.d(DEBUG_TAG, "Deleting Workout at Pos: " + pos);
                    pendingRemoval(mWorkoutData.indexOf(curWorkout));
                }
            });
        }

        @Override
        public void onClick(View v) {
            Log.d(DEBUG_TAG, curWorkout.getName() + " clicked");
            // Go to the Workout Activity
            // TODO: Go to the my_workout activity with the given current curWorkout
            mListener.onWorkoutClicked(curWorkout.getWorkoutID());
        }

        @Override
        public boolean onLongClick(View view) {
            mListener.onWorkoutLongClick(mWorkoutData.indexOf(curWorkout), curWorkout.getWorkoutID());
            // return true to indicate the click was handled
            return true;
        }
    }
    //endregion

    //region PRIVATE_VARS
    // Data
    private List<Workout> mWorkoutData;
    private List<Workout> mWorkoutsPendingRemoval;
    // UI Components
    private CoordinatorLayout mCoordLayout;
    private Context mContext;
    // Threading Components
    private Handler handler = new Handler(); // Handler for running async delayed tasks
    private HashMap<Workout, Runnable> pendingRunnables = new HashMap<>(); // Map of the items to their async runnable rasks


    //endregion

    //region INTERFACES
    public interface WorkoutAdapterListener{
        void onWorkoutClicked(int workoutIndex);
        void onWorkoutLongClick(int workoutIndex, int workoutID);
        void deleteWorkout(Workout workout);
    }
    private WorkoutAdapterListener mListener;
    //endregion

    //region LIFECYCLE
    public WorkoutAdapter(Context context, View coordLayout){
        mWorkoutsPendingRemoval = new ArrayList<>();
        mContext = context;
        if(coordLayout instanceof CoordinatorLayout){
            mCoordLayout = (CoordinatorLayout) coordLayout;
        }else{
            Log.e(DEBUG_TAG, "PASSED INCORRECT VIEW TO WORKOUT_ADAPTER");
        }        // Make sure our context is an activity and set the Listener to it
        Activity activity = null;
        if(context instanceof Activity)
            activity = (Activity) context;
        try{
            mListener = (WorkoutAdapter.WorkoutAdapterListener) activity;
        } catch (ClassCastException e){
            Log.e(DEBUG_TAG, "ERROR IN WORKOUT ADAPTER LISTENER: " + e.getMessage());
        }

    }

    @Override
    public WorkoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WorkoutViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_workout_items, parent, false));
    }

    @Override
    public void onBindViewHolder(WorkoutViewHolder viewHolder, int position) {
        // Get the current data
        Workout currentWorkout = mWorkoutData.get(position);
        // Bind to the correct data
        viewHolder.bindTo(currentWorkout, position);
    }

    //endregion

    //region UTILITY
    @Override
    public int getItemCount() {
        if(mWorkoutData != null)
            return mWorkoutData.size();
        else
            return 0;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.workout_swipe;
    }

    public void setData(List<Workout> workouts){
        mWorkoutData = workouts;
        if(mWorkoutsPendingRemoval.size() > 0){
            for(int i = 0; i < mWorkoutsPendingRemoval.size(); i++){
                Workout workout = mWorkoutsPendingRemoval.get(i);
                if (mWorkoutData.contains(workout)){
                    mWorkoutData.remove(workout);
                }
            }
        }
        notifyDataSetChanged();
    }
    //endregion

    //region PENDING_DELETE
    @Override
    public void pendingRemoval(final int swipedPos){
        final Workout workout = mWorkoutData.get(swipedPos);
        if(!mWorkoutsPendingRemoval.contains(workout)){
            mWorkoutsPendingRemoval.add(workout);
            final int pendingPos = mWorkoutsPendingRemoval.indexOf(workout);
            mWorkoutData.remove(swipedPos);
            notifyItemRemoved(swipedPos);
            notifyItemRangeChanged(swipedPos, mWorkoutData.size());
            Log.d(DEBUG_TAG, "Workout at position: " + swipedPos + " is being put up for pending removal.");
            Runnable pendingRemovalRunnable = new Runnable(){
                @Override
                public void run() {
                    deletePending(mWorkoutsPendingRemoval.indexOf(workout), swipedPos);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(workout, pendingRemovalRunnable);

            Snackbar undoBar = Snackbar.make(mCoordLayout, "Undo", Snackbar.LENGTH_LONG);
            undoBar.setAction("Undo", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Log.d(DEBUG_TAG, "Pending removal item: "+ pendingPos + " is being undone at inserting at: " + swipedPos);
                    undo(swipedPos, pendingPos);
                    Log.d(DEBUG_TAG, Integer.toString(mWorkoutData.size()));
                }
            });
            undoBar.show();

        }
    }

    @Override
    public void deletePending(int pendingIndex, int origWorkoutPos){
        mWorkoutsPendingRemoval.remove(pendingIndex);

        mListener.deleteWorkout(mWorkoutData.get(origWorkoutPos));
        // Update the file that we have removed a curWorkout
    }

    @Override
    public boolean isPendingRemoval(int pos){
        Workout workout = mWorkoutData.get(pos);
        return mWorkoutsPendingRemoval.contains(workout);
    }

    // Function for re-adding a pending item using it's orig position number
    @Override
    public void undo(int itemPos, int pendingPos) {
        // Stop the relevant runnable
        // Remove item from mWorkoutsPendingRemoval
        // Re-add item to mWorkoutData at correct pos
        if(mWorkoutsPendingRemoval.get(pendingPos) != null) {
            Workout workout = mWorkoutsPendingRemoval.get(pendingPos);
            Runnable pendingRunnable = pendingRunnables.get(workout);
            pendingRunnables.remove(workout);
            if (pendingRunnable != null) {
                handler.removeCallbacks(pendingRunnable);
            }
            mWorkoutsPendingRemoval.remove(pendingPos);
            Log.d(DEBUG_TAG,workout.getName() + " is should be inserted at pos: " + Integer.toString(itemPos));
            mWorkoutData.add(itemPos, workout);
            notifyItemInserted(itemPos);
        }
    }

    //endregion

}
