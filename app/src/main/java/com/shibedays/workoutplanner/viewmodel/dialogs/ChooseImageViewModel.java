package com.shibedays.workoutplanner.viewmodel.dialogs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;

public class ChooseImageViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = ChooseImageViewModel.class.getSimpleName();

    private List<Integer> mImageIds;
    private HashMap<Integer, Boolean> mMappedImageIds;
    private int mSelected;

    public ChooseImageViewModel(@NonNull Application application) {
        super(application);
    }

    public List<Integer> getImageIds() {
        return mImageIds;
    }
    public void setImageIds(List<Integer> mImageIds) {
        this.mImageIds = mImageIds;
    }

    public HashMap<Integer, Boolean> getMappedImageIds() {
        return mMappedImageIds;
    }
    public void setMappedImageIds(HashMap<Integer, Boolean> mMappedImageIds) {
        this.mMappedImageIds = mMappedImageIds;
    }
    public void putMapping(int key, boolean value){
        this.mMappedImageIds.put(key, value);
    }

    public int getSelected() {
        return mSelected;
    }
    public void setSelected(int mSelected) {
        this.mSelected = mSelected;
    }

    public void setupMap(){
        mMappedImageIds = new HashMap<>();
        for(int x : mImageIds){
            mMappedImageIds.put(x, false);
        }
        if(mMappedImageIds.containsKey(mSelected)){
            mMappedImageIds.put(mSelected, true);
        }
    }
}
