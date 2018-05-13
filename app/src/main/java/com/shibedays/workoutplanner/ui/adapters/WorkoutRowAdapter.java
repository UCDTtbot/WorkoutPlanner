package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.List;

public class WorkoutRowAdapter extends RecyclerView.Adapter<WorkoutRowAdapter.WorkoutRowHolder> {


    public class WorkoutRowHolder extends RecyclerView.ViewHolder{

        protected TextView sectionTitle;

        protected RecyclerView workoutRecyclerView;

        protected Button buttonMore;

        public WorkoutRowHolder(View view){
            super(view);

            this.sectionTitle = (TextView) view.findViewById(R.id.workout_section_header);
            this.workoutRecyclerView = (RecyclerView) view.findViewById(R.id.workout_row_recycler);
            this.buttonMore = (Button) view.findViewById(R.id.more_button);
        }
    }

    private List<List<Workout>> mTypedWorkouts;
    private CoordinatorLayout mCoordLayout;
    private Context mContext;

    public WorkoutRowAdapter(Context context, CoordinatorLayout coord){
        mContext = context;
        mCoordLayout = coord;
    }

    @NonNull
    @Override
    public WorkoutRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WorkoutRowHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_workout_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutRowHolder holder, int position) {
        final String sectionName = Workout.TYPES[position];

        holder.sectionTitle.setText(sectionName);

        WorkoutItemAdapter workoutItemAdapter = new WorkoutItemAdapter(mContext, mCoordLayout, mTypedWorkouts.get(position), new WorkoutItemAdapter.WorkoutAdapterListener() {
            @Override
            public void onWorkoutClicked(int workoutIndex, int type) {

            }

            @Override
            public void onWorkoutLongClick(int workoutIndex, int workoutID, int type) {

            }

            @Override
            public void deleteFromDB(Workout workout) {

            }
        });

        holder.workoutRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        holder.workoutRecyclerView.setAdapter(workoutItemAdapter);

        holder.buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Clicked "+sectionName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTypedWorkouts != null ? mTypedWorkouts.size() : 0;
    }

    public void setData(List<List<Workout>> workouts){
        mTypedWorkouts = workouts;

    }
}
