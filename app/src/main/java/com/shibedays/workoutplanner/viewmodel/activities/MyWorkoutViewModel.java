package com.shibedays.workoutplanner.viewmodel.activities;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.db.entities.Workout;

public class MyWorkoutViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = MyWorkoutViewModel.class.getSimpleName();

    private int mId;
    private Workout mWorkoutData;

    public MyWorkoutViewModel(@NonNull Application application) {
        super(application);
    }

    public void setId(int i){
        mId = i;
    }
    public int getId(){
        return mId;
    }


    public void setWorkout(Workout w){
        mWorkoutData = w;
    }
    public Workout getWorkoutData(){
        return mWorkoutData;
    }
}
