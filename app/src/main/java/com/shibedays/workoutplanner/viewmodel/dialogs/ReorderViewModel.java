package com.shibedays.workoutplanner.viewmodel.dialogs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.db.entities.Set;

import java.util.List;

public class ReorderViewModel  extends AndroidViewModel {

    private static final String DEBUG_TAG = ReorderViewModel.class.getSimpleName();

    private List<Set> mSetList;

    public ReorderViewModel(@NonNull Application application) {
        super(application);
    }

    public List<Set> getSetList(){ return mSetList; }
    public void setSetList(List<Set> list) { mSetList = list; }
}