package com.shibedays.workoutplanner.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.DataRepo;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.ArrayList;
import java.util.List;


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

        mTypedWorkouts = new ArrayList<LiveData<List<Workout>>>() {{
            for(String TYPE : Workout.TYPES) {
                add(null);
            }
        }};
        for(int i = 0; i < Set.TYPES.length; i++){
            mTypedWorkouts.add(getTypedWorkouts(i));
        }
    }

    public LiveData<List<Workout>> getAllWorkouts() {
        return mWorkouts;
    }

    public LiveData<List<Workout>> getTypedWorkouts(int type){
        return mRepo.getTypedWorkouts(type);
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

    private Workout getWorkoutByID(int id, int type){
        if(mTypedWorkouts != null){
            if(mTypedWorkouts.get(type) != null){
                if(mTypedWorkouts.get(type).getValue() != null) {
                    for (Workout w : mTypedWorkouts.get(type).getValue()) {
                        if (w.getWorkoutID() == id) return w;
                    }
                }
            }
        }
        return null;
    }

    public void setCurWorkout(final int id){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mCurWorkout = getWorkoutByID(id);
            }
        });
    }

    public Workout getCurWorkout(){
        return mCurWorkout;
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
