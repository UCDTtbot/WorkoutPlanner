package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.ArrayList;
import java.util.List;

public class WorkoutRowAdapter extends RecyclerView.Adapter<WorkoutRowAdapter.WorkoutRowHolder> {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = WorkoutRowAdapter.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.adapters.WorkoutRowAdapter.";
    //endregion

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
    private List<WorkoutItemAdapter> mAdapters;
    private Context mContext;

    //region INTERFACES
    public interface WorkoutRowListener{
        void onWorkoutClicked(int id, int type);
        void onWorkoutLongClick(int id, int type);
        void deleteFromDB(Workout workout);
    }
    private WorkoutRowListener mListener;
    //endregion

    public WorkoutRowAdapter(Context context, CoordinatorLayout coord, WorkoutRowListener listener){
        mContext = context;
        mCoordLayout = coord;
        mAdapters = new ArrayList<>();
        for(String s : Workout.TYPES){
            mAdapters.add(null);
        }
        mListener = listener;
    }

    @NonNull
    @Override
    public WorkoutRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WorkoutRowHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.list_workout_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutRowHolder holder, final int position) {
        final String sectionName = Workout.TYPES[position];

        holder.sectionTitle.setText(sectionName);

        WorkoutItemAdapter workoutItemAdapter = new WorkoutItemAdapter(mContext, mCoordLayout, mTypedWorkouts.get(position), position, new WorkoutItemAdapter.WorkoutAdapterListener() {
            @Override
            public void onWorkoutClicked(int id, int type) {
                mListener.onWorkoutClicked(id, type);
            }

            @Override
            public void onWorkoutLongClick(int id, int type) {
                mListener.onWorkoutLongClick(id, type);
            }

            @Override
            public void deleteFromDB(Workout workout) {
                mListener.deleteFromDB(workout);
            }
        });
        holder.workoutRecyclerView.setHasFixedSize(true);
        holder.workoutRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        holder.workoutRecyclerView.setAdapter(workoutItemAdapter);
        mAdapters.set(position, workoutItemAdapter);
        /*
        if(mAdapters.size() >= Workout.TYPES.length)
            Log.e(DEBUG_TAG, "ADAPTERS IS ATTEMPTING TO BECOME BIGGER");
        else
            mAdapters.add(workoutItemAdapter);
        */
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
        notifyDataSetChanged();
        if(mAdapters != null) {
            int i = 0;
            for (WorkoutItemAdapter a : mAdapters) {
                if (a != null) {
                    a.notifyDataSetChanged();
                }
            }
        }

    }

    public void pendingRemoval(int id, int type){
        mAdapters.get(type).pendingRemoval(id);
    }
}
