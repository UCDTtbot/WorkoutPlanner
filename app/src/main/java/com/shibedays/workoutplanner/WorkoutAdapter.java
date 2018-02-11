package com.shibedays.workoutplanner;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {

    private List<Workout> mWorkoutData;
    private Context mContext;

    /**
     *
     * @param context
     * @param workoutData
     */
    public WorkoutAdapter(Context context, List<Workout> workoutData){
        mWorkoutData = workoutData;
        mContext = context;
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

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView itemName;
        private TextView sets;
        //private TextView rounds;

        public ViewHolder(View itemView) {
            super(itemView);
            //Initialize the views for the RecyclerView
            itemName = itemView.findViewById(R.id.item_name);
            sets = itemView.findViewById(R.id.item_sets);
            //rounds = itemView.findViewById(R.id.item_rounds);
        }

        /**
         *
         * @param curWorkout
         */
        void bindTo(Workout curWorkout){
            //Populate data when they bind the workouts to the view holder
            itemName.setText(curWorkout.getmName());
            sets.setText(String.format(mContext.getString(R.string.item_sets), curWorkout.getmNumOfSets()));
            //rounds.setText(String.format(mContext.getString(R.string.item_rounds), curWorkout.mNumOfRounds));
        }

        @Override
        public void onClick(View v) {

        }
    }

}
