package com.shibedays.workoutplanner.viewmodel.fragments;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.DataRepo;

public class NewWorkoutViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = NewWorkoutViewModel.class.getSimpleName();

    private int mRounds;
    private int mRestTime;
    private int mBreakTime;
    private boolean mRestFlag;
    private boolean mBreakFlag;

    public NewWorkoutViewModel(@NonNull Application application) {
        super(application);
    }

    public void setRounds(int round){ mRounds = round; }
    public int getRounds(){ return mRounds; }

    public void setRestTime(int time){ mRestTime = time; }
    public int getRestTime(){ return mRestTime; }

    public void setBreakTime(int time){ mBreakTime = time; }
    public int getBreakTime(){ return mBreakTime; }

    public void setRestFlag(boolean flag){ mRestFlag = flag; }
    public boolean getRestFlag(){ return mRestFlag; }

    public void setBreakFlag(boolean flag){ mBreakFlag = flag; }
    public boolean getBreakFlag(){ return mBreakFlag; }


}
