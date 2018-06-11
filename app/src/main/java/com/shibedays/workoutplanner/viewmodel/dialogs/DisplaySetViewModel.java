package com.shibedays.workoutplanner.viewmodel.dialogs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

public class DisplaySetViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = DisplaySetViewModel.class.getSimpleName();

    private String mSetName;
    private String mSetDescrip;
    private int mSetMin;
    private int mSetSec;
    private int mSetImageId;

    public DisplaySetViewModel(@NonNull Application application) {
        super(application);
    }

    public String getSetName() {
        return mSetName;
    }
    public void setSetName(String mSetName) {
        this.mSetName = mSetName;
    }

    public String getSetDescrip() {
        return mSetDescrip;
    }
    public void setSetDescrip(String mSetDescrip) {
        this.mSetDescrip = mSetDescrip;
    }

    public int getSetMin() {
        return mSetMin;
    }
    public void setSetMin(int mSetMin) {
        this.mSetMin = mSetMin;
    }

    public int getSetSec() {
        return mSetSec;
    }
    public void setSetSec(int mSetSec) {
        this.mSetSec = mSetSec;
    }

    public int getSetImageId() {
        return mSetImageId;
    }
    public void setSetImageId(int mSetImageId) {
        this.mSetImageId = mSetImageId;
    }
}
