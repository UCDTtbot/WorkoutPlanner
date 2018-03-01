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
import android.widget.TextView;

import com.shibedays.workoutplanner.DataRepo;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {

    private static final int PENDING_REMOVAL_TIMEOUT = 4000; // LENGTH_LONG is defined as 3500, so lets put 4000 just in case
    private static final String DEBUG_TAG = WorkoutAdapter.class.getSimpleName();

    private List<Workout> mWorkoutData;
    private List<Workout> mWorkoutsPendingRemoval;
    private CoordinatorLayout mCoordLayout;
    private Context mContext;

    private Handler handler = new Handler(); // Handler for running async delayed tasks
    private HashMap<Workout, Runnable> pendingRunnables = new HashMap<>(); // Map of the items to their async runnable rasks

    public interface WorkoutAdapterListener{
        public void onWorkoutClicked(int workoutIndex);
        public void onWorkoutLongClick(int workoutIndex);
        public void deleteWorkout(Workout workout);
    }
    private WorkoutAdapterListener listener;
    /**
     *
     */
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
            listener = (WorkoutAdapter.WorkoutAdapterListener) activity;
        } catch (ClassCastException e){
            Log.e(DEBUG_TAG, "ERROR IN WORKOUT ADAPTER LISTENER: " + e.getMessage());
        }

    }

    /**
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public WorkoutAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.workout_list_item, parent, false));
    }

    /**
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(WorkoutAdapter.ViewHolder holder, int position) {
        // Get the current data
        Workout currentWorkout = mWorkoutData.get(position);
        // Bind to the data for the views
        holder.bindTo(currentWorkout);
        // Populate anymore data
    }

    /**
     *
     * @return
     */
    @Override
    public int getItemCount() {
        if(mWorkoutData != null)
            return mWorkoutData.size();
        else
            return 0;
    }

    public void setData(List<Workout> workouts){
        mWorkoutData = workouts;
        notifyDataSetChanged();
    }

    // Function for re-adding a pending item using it's orig position number
    // TODO: Not sure if we want the position of the item or not
    public void undoItem(int itemPos, int pendingPos){
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

    public void pendingRemoval(final int pos){
        final Workout workout = mWorkoutData.get(pos);
        if(!mWorkoutsPendingRemoval.contains(workout)){
            mWorkoutsPendingRemoval.add(workout);
            final int pendingPos = mWorkoutsPendingRemoval.indexOf(workout);
            mWorkoutData.remove(pos);
            notifyItemRemoved(pos);
            Runnable pendingRemovalRunnable = new Runnable(){

                @Override
                public void run() {
                    deletePending(mWorkoutsPendingRemoval.indexOf(workout), workout);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(workout, pendingRemovalRunnable);

            Snackbar undoBar = Snackbar.make(mCoordLayout, "Undo", Snackbar.LENGTH_LONG);
            undoBar.setAction("Undo", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    undoItem(pos, pendingPos);
                }
            });
            undoBar.show();

        }
    }

    public void deletePending(int pendingIndex, Workout originalWorkout){
        mWorkoutsPendingRemoval.remove(pendingIndex);

        listener.deleteWorkout(originalWorkout);
        Log.d(DEBUG_TAG, "Removed the pending workout");
        // Update the file that we have removed a curWorkout
    }

    public boolean isPendingRemoval(int pos){
        Workout workout = mWorkoutData.get(pos);
        return mWorkoutsPendingRemoval.contains(workout);
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView itemName;
        private TextView sets;
        private Workout curWorkout;
        //private TextView rounds;

        public ViewHolder(View itemView) {
            super(itemView);
            //Initialize the views for the RecyclerView
            itemName = itemView.findViewById(R.id.item_name);
            sets = itemView.findViewById(R.id.item_sets);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            //rounds = itemView.findViewById(R.id.item_rounds);

        }

        /**
         *
         * @param curWorkout
         */
        void bindTo(Workout curWorkout){
            //Populate data when they bind the workouts to the view holder
            itemName.setText(curWorkout.getName());
            sets.setText(String.format(mContext.getString(R.string.item_sets), curWorkout.getNumOfSets()));
            this.curWorkout = curWorkout;
            //rounds.setText(String.format(mContext.getString(R.string.item_rounds), curWorkout.mNumOfRounds));
        }

        @Override
        public void onClick(View v) {
            Log.d(DEBUG_TAG, curWorkout.getName() + " clicked");
            // Go to the Workout Activity
            // TODO: Go to the my_workout activity with the given current curWorkout
            listener.onWorkoutClicked(curWorkout.getWorkoutID());
        }

        @Override
        public boolean onLongClick(View view) {
            Log.d(DEBUG_TAG, curWorkout.getName() + " long clicked");
            listener.onWorkoutLongClick(curWorkout.getWorkoutID());
            // return true to indicate the click was handled
            return true;
        }
    }

}
