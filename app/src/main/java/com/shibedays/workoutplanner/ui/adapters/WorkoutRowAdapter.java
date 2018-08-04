package com.shibedays.workoutplanner.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
        void openMoreFragment(String type);
        void undo(Workout w);
    }
    private WorkoutRowListener mListener;
    //endregion

    public WorkoutRowAdapter(Context context, CoordinatorLayout coord, WorkoutRowListener listener){
        mContext = context;
        mCoordLayout = coord;
        mAdapters = new ArrayList<>();
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
            public void onWorkoutLongClick(int id, int type) { mListener.onWorkoutLongClick(id, type); }

            @Override
            public void deleteFromDB(Workout workout) {
                mListener.deleteFromDB(workout);
            }

            @Override
            public void undo(Workout w) {
                mListener.undo(w);
            }
        });
        holder.workoutRecyclerView.setHasFixedSize(true);
        //holder.workoutRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        holder.workoutRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 2, GridLayoutManager.HORIZONTAL, false));
        //
        holder.workoutRecyclerView.setAdapter(workoutItemAdapter);
        holder.buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openMoreFragment(Workout.TYPES[position]);
            }
        });


        mAdapters.set(position, workoutItemAdapter);

    }

    @Override
    public int getItemCount() {
        return mTypedWorkouts != null ? mTypedWorkouts.size() : 0;
    }

    public void initiateData(){
        mTypedWorkouts = new ArrayList<>();
        for(String s : Workout.TYPES){
            mAdapters.add(null);
            mTypedWorkouts.add(new ArrayList<Workout>());
        }
    }

    public void updateData(int type, List<Workout> data){
        mTypedWorkouts.set(type, data);
        notifyItemChanged(type);
        if(mAdapters.get(type) != null){
            mAdapters.get(type).updateData(data);
        }
    }

    public void pendingRemoval(int id, int type){
        mAdapters.get(type).pendingRemoval(id);
    }

    public void disableMoreButton(){

    }
}
