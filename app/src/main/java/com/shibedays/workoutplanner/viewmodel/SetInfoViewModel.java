package com.shibedays.workoutplanner.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.db.entities.Set;

public class SetInfoViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = SetInfoViewModel.class.getSimpleName();

    private int mSetId;
    private Set mSetData;

    public SetInfoViewModel(@NonNull Application application) {
        super(application);
    }

    public void setId(int i){
        mSetId = i;
    }
    public int getId(){
        return mSetId;
    }

    public void setData(Set s){
        mSetData = s;
    }
    public Set getData(){
        return mSetData;
    }
}
