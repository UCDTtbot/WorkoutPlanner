package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
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


public class WorkoutItemAdapter extends PendingRemovalAdapter<WorkoutItemAdapter.WorkoutViewHolder> {

    //region CONSTANTS
    // Timeout Constant
    private static final int PENDING_REMOVAL_TIMEOUT = 4000; // LENGTH_LONG is defined as 3500, so lets put 4000 just in case
    // Package and Debug Constants
    private static final String DEBUG_TAG = WorkoutItemAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.WorkoutItemAdapter.";
    //endregion

    //region VIEW_HOLDER
    public class WorkoutViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        // Data
        protected Workout curWorkout;
        // Foreground
        protected TextView itemName;
        protected ImageView itemImage;

        private WorkoutViewHolder(View itemView) {
            super(itemView);
            //Initialize the views for the RecyclerView
            itemName = itemView.findViewById(R.id.workout_name);
            itemImage = itemView.findViewById(R.id.workout_image);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            }

        @Override
        public void onClick(View v) {
            // TODO: Go to the my_workout activity with the given current curWorkout
            mListener.onWorkoutClicked(curWorkout.getWorkoutID(), curWorkout.getWorkoutType());
        }

        @Override
        public boolean onLongClick(View view) {
            mListener.onWorkoutLongClick(mWorkoutData.indexOf(curWorkout), curWorkout.getWorkoutID(), curWorkout.getWorkoutType());
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
        void onWorkoutClicked(int workoutIndex, int type);
        void onWorkoutLongClick(int workoutIndex, int workoutID, int type);
        void deleteFromDB(Workout workout);
    }
    private WorkoutAdapterListener mListener;
    //endregion

    //region LIFECYCLE
    public WorkoutItemAdapter(Context context, View coordLayout, List<Workout> workouts, WorkoutAdapterListener listener){
        mWorkoutsPendingRemoval = new ArrayList<>();
        mContext = context;
        if(coordLayout instanceof CoordinatorLayout){
            mCoordLayout = (CoordinatorLayout) coordLayout;
        }else{
            throw new RuntimeException(WorkoutItemAdapter.class.getSimpleName() + " was passed a non-coord layout view");
        }        // Make sure our context is an activity and set the Listener to it

        setData(workouts);
        mListener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WorkoutViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_workout_items, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder viewHolder, int position) {
        // Get the current data
        Workout currentWorkout = mWorkoutData.get(position);
        viewHolder.curWorkout = mWorkoutData.get(position);
        viewHolder.itemName.setText(mWorkoutData.get(position).getName());
        // Bind to the correct data
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
    public void pendingRemoval(final int index){
        final Workout workout = mWorkoutData.get(index);
        if(!mWorkoutsPendingRemoval.contains(workout)){
            mWorkoutsPendingRemoval.add(workout);
            final int pendingPos = mWorkoutsPendingRemoval.indexOf(workout);
            mWorkoutData.remove(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, mWorkoutData.size());
            Runnable pendingRemovalRunnable = new Runnable(){
                @Override
                public void run() {
                    deletePending(mWorkoutsPendingRemoval.indexOf(workout), index);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(workout, pendingRemovalRunnable);

            Snackbar undoBar = Snackbar.make(mCoordLayout, "Undo", Snackbar.LENGTH_LONG);
            undoBar.setAction("Undo", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    undo(index, pendingPos);
                }
            });
            undoBar.show();

        }
    }

    @Override
    public void deletePending(int pendingIndex, int origWorkoutPos){
        mListener.deleteFromDB(mWorkoutsPendingRemoval.get(pendingIndex));
        mWorkoutsPendingRemoval.remove(pendingIndex);
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