package com.shibedays.workoutplanner.viewmodel.fragments;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.R;

import java.util.ArrayList;
import java.util.List;

public class CreateEditViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = CreateEditViewModel.class.getSimpleName();

    private String mName;
    private String mDescrip;
    private int mMins;
    private int mSecs;
    private int mSetId;
    private int mPos;
    private int mWorkoutId;
    private int mImage;
    private ArrayList<Integer> mDefaultImageIds;

    public CreateEditViewModel(@NonNull Application application) {
        super(application);
    }


    public String getName() {
        return mName;
    }
    public void setName(String mName) {
        this.mName = mName;
    }

    public String getDescrip() {
        return mDescrip;
    }
    public void setDescrip(String mDescrip) {
        this.mDescrip = mDescrip;
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

    public int getId(){ return mSetId; }
    public void setId(int id){ mSetId = id; }

    public int getPos() {
        return mPos;
    }
    public void setPos(int mPos) {
        this.mPos = mPos;
    }

    public int getImage() {
        return mImage;
    }
    public void setImage(int mImage) {
        this.mImage = mImage;
    }

    public void setupDefaultImages(){
        mDefaultImageIds = new ArrayList<>();
        mDefaultImageIds.add(R.drawable.ic_fitness_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_run_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_walk_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_access_alarm_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_info_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_abs_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_armsup_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_armsup2_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_balance_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_dumbbells_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_jumprope_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_lifting_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_lifts_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_muscle_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_running_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_smartwatch_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_workout_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_walkinglunge_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_pushups_black_24dp);
        mDefaultImageIds.add(R.drawable.ic_sitting_man);
    }
    public ArrayList<Integer> getDefaultImageIds(){
        return mDefaultImageIds;
    }
    public int getDefaultImageID(int index){
        return mDefaultImageIds.get(index);
    }

    public int getWorkoutId() {
        return mWorkoutId;
    }
    public void setWorkoutId(int mWorkoutId) {
        this.mWorkoutId = mWorkoutId;
    }
}
