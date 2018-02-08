package com.shibedays.workoutplanner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {

    private ArrayList<Workout> mWorkoutData;
    private Context mContext;

    /**
     *
     * @param context
     * @param workoutData
     */
    public WorkoutAdapter(Context context, ArrayList<Workout> workoutData){
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
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.workout_item, parent, false));
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

        private TextView test;

        public ViewHolder(View itemView) {
            super(itemView);
            //Initialize the views for the RecyclerView
            test = itemView.findViewById(R.id.testingView);
        }

        void bindTo(Workout curWorkout){
            //Populate data when they bind the workouts to the view holder
            test.setText("Got View" + curWorkout.i);
        }

        @Override
        public void onClick(View v) {

        }
    }

}
