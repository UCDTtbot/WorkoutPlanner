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
    private List<LiveData<List<Set>>> mTypedSets;

    public SetViewModel(@NonNull Application application) {
        super(application);

        mRepo = ((BaseApp) application).getRepo();

        mSets = mRepo.getAllSets();

        mTypedSets = new ArrayList<>();
        for(int i = 0; i < Set.TYPES.length; i++){
            mTypedSets.add(mRepo.getTypedSets(i));
        }
    }

    public LiveData<List<Set>> getAllSets() {
        return mSets;
    }

    public LiveData<List<Set>> getAllTypedSets(int type) {
        return mTypedSets.get(type);
    }

    public Set getSetById(int id){
        if(mSets != null){
            if(mSets.getValue() != null){
                for(Set s : mSets.getValue()){
                    if(s.getSetId() == id){
                        return s;
                    }
                }
            }
        }
        return null;
    }

    public void update(Set set){ mRepo.updateSet(set); }

    public void insert(Set set){
        mRepo.insertSet(set);
        BaseApp.incrementSetID(getApplication());
    }

    public void remove(Set set){ mRepo.removeSet(set); }
}
