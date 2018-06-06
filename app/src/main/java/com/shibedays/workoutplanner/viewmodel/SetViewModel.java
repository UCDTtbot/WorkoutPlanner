package com.shibedays.workoutplanner.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.DataRepo;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.db.entities.Workout;

import java.util.ArrayList;
import java.util.List;

public class SetViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = SetViewModel.class.getSimpleName();

    private DataRepo mRepo;

    private LiveData<List<Set>> mSets;
    private List<List<Set>> mTypedSets;
    private SparseBooleanArray mValidData;

    public SetViewModel(@NonNull Application application) {
        super(application);

        mRepo = ((BaseApp) application).getRepo();

        mSets = mRepo.getAllSets();

        mTypedSets = new ArrayList<>();
        mValidData = new SparseBooleanArray();
        int i = 0;
        for(String s : Set.TYPES){
            mTypedSets.add(null);
            mValidData.put(i++, false);
        }
    }

    public LiveData<List<Set>> getAllSets() {
        return mSets;
    }

    public LiveData<List<Set>> getAllTypedSets(int type) {
        return mRepo.getTypedSets(type);
    }

    public boolean isDataValid(int type){
        return mValidData.get(type);
    }

    public void updateTypedSet(int index, List<Set> typed){
        if(mTypedSets != null) {
            mTypedSets.set(index, typed);
        }
    }

    public void update(Set set){ mRepo.updateSet(set); }

    public void insert(Set set){ mRepo.insertSet(set); }

    public void remove(Set set){ mRepo.removeSet(set); }
}
