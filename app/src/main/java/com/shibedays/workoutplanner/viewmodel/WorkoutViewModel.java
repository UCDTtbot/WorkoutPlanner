package com.shibedays.workoutplanner.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseBooleanArray;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.DataRepo;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WorkoutViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = WorkoutViewModel.class.getSimpleName();

    private DataRepo mRepo;

    private LiveData<List<Workout>> mWorkouts;
    private List<LiveData<List<Workout>>> mTypedWorkouts;

    private Workout mCurWorkout;

    public WorkoutViewModel(@NonNull Application application) {
        super(application);

        mRepo = ((BaseApp) application).getRepo();

        mWorkouts = mRepo.getAllWorkouts();

        mTypedWorkouts = new ArrayList<>();
        for(int i = 0; i < Workout.TYPES.length; i++){
            mTypedWorkouts.add(mRepo.getTypedWorkouts(i));
        }
    }

    public LiveData<List<Workout>> getAllWorkouts() {
        return mWorkouts;
    }

    public LiveData<List<Workout>> getAllTypedWorkouts(int type){
        return mTypedWorkouts.get(type);
    }

    public LiveData<Workout> getWorkout(int id){
        return mRepo.getWorkout(id);
    }

    public Workout getWorkoutByID(int id){
        if(mWorkouts != null && id >= 0) {
            if(mWorkouts.getValue() != null) {
                for (Workout w : mWorkouts.getValue()) {
                    if(w.getWorkoutID() == id) return w;
                }
            }
        } else {

            Log.e(DEBUG_TAG, "Workout ID is invalid: " + Integer.toString(id));
            return null;
        }

        return null;
    }

    public void update(Workout workout){
        mRepo.updateWorkout(workout);
    }

    public void insert(Workout workout){
        mRepo.insertWorkout(workout);
    }

    public void remove(Workout workout){
        mRepo.removeWorkout(workout);
    }
}
