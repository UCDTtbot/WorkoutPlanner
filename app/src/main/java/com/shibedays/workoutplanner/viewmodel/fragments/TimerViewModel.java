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

    private int mCurRep;
    private int mCurRound;

    public TimerViewModel(@NonNull Application application) {
        super(application);
    }

    public Workout getWorkout() {
        return mWorkout;
    }
    public void setWorkoutId(Workout w) {
        this.mWorkout = w;
    }

    public List<Set> getSets() {
        return mSets;
    }
    public void setSets(List<Set> mSets) {
        this.mSets = mSets;
    }
    public Set getSet(int index){
        return mSets.get(index);
    }
    public Set getNextSet(){
        int i = getCurSetIndex() + 1;
        if(i < mSets.size()){
            mCurSet = mSets.get(i);
            return mCurSet;
        } else {
            return null;
        }
    }
    public Set getFirstSet(){
        mCurSet = mSets.get(0);
        return mCurSet;
    }


    public Set getCurSet() {
        return mCurSet;
    }
    public void setCurSet(Set mCurSet) {
        this.mCurSet = mCurSet;
    }
    public int getCurSetIndex(){
        return mSets.indexOf(mCurSet);
    }
    public int getCurSetTime(){
        return mCurSet.getTime();
    }
    public String getCurSetDescrip(){
        return mCurSet.getDescrip();
    }
    public String getCurSetName(){
        return mCurSet.getName();
    }


    public int getCurRep() {
        return mCurRep;
    }
    public void setCurRep(int mCurRep) {
        this.mCurRep = mCurRep;
    }
    public int getTotalReps(){
        return mWorkout.getNumOfSets();
    }
    public int getNextRep(){
        return ++mCurRep;
    }


    public int getCurRound() {
        return mCurRound;
    }
    public void setCurRound(int mCurRound) {
        this.mCurRound = mCurRound;
    }
    public int getTotalRounds(){
        return mWorkout.getNumOfRounds();
    }
    public int getNextRound(){
        return ++mCurRound;
    }


    public int getRestTime(){
        return mWorkout.getTimeBetweenSets();
    }
    public int getBreakTime(){
        return mWorkout.getTimeBetweenRounds();
    }
}
