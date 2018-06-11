package com.shibedays.workoutplanner.viewmodel.fragments;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.List;

public class TimerViewModel extends AndroidViewModel {

    private Workout mWorkout;
    private List<Set> mSets;
    private Set mCurSet;
    private int mCurSetIndex;
    private int mCurRep;
    private int mCurRound;

    public TimerViewModel(@NonNull Application application) {
        super(application);
    }
}
