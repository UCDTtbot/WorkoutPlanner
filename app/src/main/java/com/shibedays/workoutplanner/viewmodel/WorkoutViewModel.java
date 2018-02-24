package com.shibedays.workoutplanner.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.DataRepo;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.List;

/**
 * Created by ttbot on 2/23/18.
 */

public class WorkoutViewModel extends AndroidViewModel {

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

    public Workout getWorkout(int pos){
        return mWorkouts.getValue().get(pos);
    }

    public void insert(Workout workout){
        mRepo.insert(workout);
    }

    public void remove(Workout workout){
        mRepo.remove(workout);
    }
}
