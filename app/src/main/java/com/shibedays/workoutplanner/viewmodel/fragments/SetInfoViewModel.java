package com.shibedays.workoutplanner.viewmodel.fragments;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.shibedays.workoutplanner.db.entities.Set;

public class SetInfoViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = SetInfoViewModel.class.getSimpleName();

    private int mParentWrkoutId;
    private MutableLiveData<Set> mLive;

    public SetInfoViewModel(@NonNull Application application) {
        super(application);
    }


    public MutableLiveData<Set> getData(){
        if(mLive == null){
            mLive = new MutableLiveData<>();
        }

        return mLive;
    }
    public void setData(String json){
        Gson g = new Gson();
        Set s = g.fromJson(json, Set.class);
        Log.d(DEBUG_TAG, "Read data: " + json);

        if(mLive == null){
            mLive = new MutableLiveData<>();
        }
        mLive.setValue(s);
    }
    public void setData(Set s){
        if(mLive == null){
            mLive = new MutableLiveData<>();
        }
        mLive.setValue(s);
    }

    public int getParentWrkoutId() {
        return mParentWrkoutId;
    }
    public void setParentWrkoutId(int mParentId) {
        this.mParentWrkoutId = mParentId;
    }

}
