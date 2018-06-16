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
    private Set mNextSet;

    private int mCurRep;
    private int mCurRound;

    public TimerViewModel(@NonNull Application application) {
        super(application);
    }

    public Workout getWorkout() {
        return mWorkout;
    }
    public void setWorkout(Workout w) {
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

    /*
    public Set preloadNextSet(){
        if((getCurSetIndex() + 1) < mSets.size()) {
            mNextSet = mSets.get(getCurSetIndex() + 1);
            return mNextSet;
        } else {
            return null;
        }
    }
    public Set preloadFirstSet(){
        mNextSet = mSets.get(0);
        return mNextSet;
    }
    */

    public Set getNextSet(){
        if((getCurSetIndex() + 1) < mSets.size()){
            mNextSet = mSets.get(getCurSetIndex() + 1);
            return mNextSet;
        } else {
            mNextSet = mSets.get(0);
        }
        return mNextSet;
    }

    public Set loadInNextSet(){
        mCurSet = mNextSet;
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
    public int getCurSetImage(){ return mCurSet.getSetImageId(); }


    public int getCurRep() {
        return mCurRep;
    }
    public void setCurRep(int mCurRep) {
        this.mCurRep = mCurRep;
    }
    public int getTotalReps(){
        return mWorkout.getNumOfSets();
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
}
