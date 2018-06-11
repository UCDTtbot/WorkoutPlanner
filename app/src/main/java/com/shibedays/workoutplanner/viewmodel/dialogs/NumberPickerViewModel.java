package com.shibedays.workoutplanner.viewmodel.dialogs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

public class NumberPickerViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = NumberPickerViewModel.class.getSimpleName();

    private int mMins;
    private int mSecs;
    private int mWhichTime;
    private int mGivenTime;
    private boolean mNoFlag;

    public NumberPickerViewModel(@NonNull Application application) {
        super(application);
    }

    public int getWhichTime() {
        return mWhichTime;
    }
    public void setWhichTime(int mWhichTime) {
        this.mWhichTime = mWhichTime;
    }

    public int getGivenTime() {
        return mGivenTime;
    }
    public void setGivenTime(int mGivenTime) {
        this.mGivenTime = mGivenTime;
    }

    public boolean isNoFlag() {
        return mNoFlag;
    }
    public void setNoFlag(boolean mNoFlag) {
        this.mNoFlag = mNoFlag;
    }

    public int getMins() {
        return mMins;
    }
    public void setMins(int mMins) {
        this.mMins = mMins;
    }

    public int getSecs() {
        return mSecs;
    }
    public void setSecs(int mSecs) {
        this.mSecs = mSecs;
    }
}
