package com.shibedays.workoutplanner.viewmodel.dialogs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

public class RenameWorkoutViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = RenameWorkoutViewModel.class.getSimpleName();

    private String mName;

    public RenameWorkoutViewModel(@NonNull Application application) {
        super(application);
    }

    public String getName() {
        return mName;
    }
    public void setName(String mName) {
        this.mName = mName;
    }
}
