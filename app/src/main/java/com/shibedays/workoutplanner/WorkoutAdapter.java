package com.shibedays.workoutplanner;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {

    private static final int PENDING_REMOVAL_TIMEOUT = 10000; // LENGTH_LONG is defined as 3500, so lets put 4000 just in case
    private static final String DEBUG_TAG = WorkoutAdapter.class.getSimpleName();

    private List<Workout> mWorkoutData;
    private List<Workout> mWorkoutsPendingRemoval;
    private Context mContext;
    private MainActivity mMainParent;

    private Handler handler = new Handler(); // Handler for running async delayed tasks
    private HashMap<Workout, Runnable> pendingRunnables = new HashMap<>(); // Map of the items to their async runnable rasks


    /**
     *
     * @param context
     * @param workoutData
     */
    public WorkoutAdapter(Context context, List<Workout> workoutData, MainActivity act){
        mWorkoutData = workoutData;
        mWorkoutsPendingRemoval = new ArrayList<>();
        mContext = context;
        mMainParent = act;
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
        return mWorkoutData.size();
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
                    deletePending(mWorkoutsPendingRemoval.indexOf(workout));
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(workout, pendingRemovalRunnable);
            //TODO: Show snackbar here
            if(mMainParent != null) {
                View view = mMainParent.findViewById(R.id.main_coord_layout);
                Snackbar undoBar = Snackbar.make(view, "Undo", Snackbar.LENGTH_LONG);
                undoBar.setAction("Undo", new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                        undoItem(pos, pendingPos);
                    }
                });
                undoBar.show();
            } else {
                Log.e(DEBUG_TAG, "MainActivity parent was never assigned in WorkoutAdapter");
            }
        }
    }

    public void deletePending(int pos){
        mWorkoutsPendingRemoval.remove(pos);
        Log.d(DEBUG_TAG, "Removed the pending activity");
        mMainParent.saveWorkoutsToPref();
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
            mMainParent.openWorkout(mWorkoutData.indexOf(curWorkout));
        }


        @Override
        public boolean onLongClick(View view) {
            Log.d(DEBUG_TAG, curWorkout.getName() + " long clicked");

            // return true to indicate the click was handled
            return true;
        }
    }

}
