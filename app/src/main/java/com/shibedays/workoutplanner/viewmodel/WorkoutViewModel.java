package com.shibedays.workoutplanner.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.DataRepo;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.List;


public class WorkoutViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = WorkoutViewModel.class.getSimpleName();

    private DataRepo mRepo;

    private LiveData<List<Workout>> mWorkouts;

    public WorkoutViewModel(@NonNull Application application) {
        super(application);

        mRepo = ((BaseApp) application).getRepo();

        mWorkouts = mRepo.getAllWorkouts();
    }

    public LiveData<List<Workout>> getAllWorkouts() {
        return mWorkouts;
    }

    public LiveData<Workout> getWorkout(int id){
        if(id < 0) {
            Log.e(DEBUG_TAG, "Workout ID is invalid: " + Integer.toString(id));
            return null;
        } else {
            return mRepo.getWorkout(id);
        }
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
