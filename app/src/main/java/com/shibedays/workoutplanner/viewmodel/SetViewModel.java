package com.shibedays.workoutplanner.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.DataRepo;
import com.shibedays.workoutplanner.db.entities.Set;

import java.util.List;

public class SetViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = SetViewModel.class.getSimpleName();

    private DataRepo mRepo;

    private LiveData<List<Set>> mSets;

    public SetViewModel(@NonNull Application application) {
        super(application);

        mRepo = ((BaseApp) application).getRepo();

        mSets = mRepo.getAllSets();
    }

    public LiveData<List<Set>> getAllSets() { return mSets; }

    public List<Set> getTypedSet(int type) { return mRepo.getTypedSets(type); }

    public LiveData<List<Set>> getUserCreated() { return mRepo.getAllUserCreated(); }

    public void update(Set set){ mRepo.updateSet(set); }

    public void insert(Set set){ mRepo.insertSet(set); }

    public void remove(Set set){ mRepo.removeSet(set); }
}
