package com.shibedays.workoutplanner.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
    public class WorkoutViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        // Data
        private Workout curWorkout;
        // Foreground
        private TextView itemName;
        private TextView sets;
        // Background
        private ImageView delIcon;

        //TEMP
        private TextView TEST_POS_ID;
        private TextView TEST_WRK_ID;

        public WorkoutViewHolder(View itemView) {
            super(itemView);
            //Initialize the views for the RecyclerView
            itemName = itemView.findViewById(R.id.item_name);
            sets = itemView.findViewById(R.id.item_sets);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            TEST_POS_ID = itemView.findViewById(R.id.TEST_POS_ID);
            TEST_WRK_ID = itemView.findViewById(R.id.TEST_WRK_ID);
        }

        void bindTo(final Workout curWorkout, final int pos){
            //Populate data when they bind the workouts to the view holder
            this.curWorkout = curWorkout;

            itemName.setText(curWorkout.getName());
            sets.setText(String.format(mContext.getString(R.string.item_sets), curWorkout.getNumOfSets()));

            TEST_POS_ID.setText(String.format(Locale.US, "PosID: %1$d", mWorkoutData.indexOf(curWorkout)));
            TEST_WRK_ID.setText(String.format(Locale.US, "WrkID: %1$d", curWorkout.getWorkoutID()));
        }

        @Override
        public void onClick(View v) {
            // TODO: Go to the my_workout activity with the given current curWorkout
            mListener.onWorkoutClicked(curWorkout.getWorkoutID());
        }

        @Override
        public boolean onLongClick(View view) {
            mListener.onWorkoutLongClick(mWorkoutData.indexOf(curWorkout), curWorkout.getWorkoutID());
            return true;
        }

        public Workout getWorkout(){
            return curWorkout;
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
            throw new RuntimeException(WorkoutAdapter.class.getSimpleName() + " was passed a non-coord layout view");
        }        // Make sure our context is an activity and set the Listener to it

        if(context instanceof WorkoutAdapterListener) {
            mListener = (WorkoutAdapterListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement WorkoutAdapterListener");
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
                    undo(swipedPos, pendingPos);
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
            mWorkoutData.add(itemPos, workout);
            notifyItemInserted(itemPos);
        }
    }

    //endregion

}