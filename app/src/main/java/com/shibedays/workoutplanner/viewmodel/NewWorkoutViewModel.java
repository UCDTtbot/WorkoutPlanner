package com.shibedays.workoutplanner.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;

import com.shibedays.workoutplanner.DataRepo;
import com.shibedays.workoutplanner.db.entities.Set;

import java.util.ArrayList;
import java.util.List;

public class NewWorkoutViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = NewWorkoutViewModel.class.getSimpleName();

    private DataRepo mRepo;

    private LiveData<List<Set>> mSets;
    private List<List<Set>> mTypedSets;
    private SparseBooleanArray mValidData;

    private int mRounds;
    private int mRestTime;
    private int mBreakTime;
    private boolean mRestFlag;
    private boolean mBreakFlag;

    public NewWorkoutViewModel(@NonNull Application application) {
        super(application);

        mTypedSets = new ArrayList<>();
        mValidData = new SparseBooleanArray();
        int i = 0;
        for(String s : Set.TYPES){
            mTypedSets.add(null);
            mValidData.put(i++, false);
        }
    }

    public boolean isDataValid(int type){
        return mValidData.get(type);
    }

    public void updateTypedSet(int index, List<Set> typed){
        if(mTypedSets != null) {
            mTypedSets.set(index, typed);
        }
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
