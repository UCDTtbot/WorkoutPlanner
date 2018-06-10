package com.shibedays.workoutplanner.viewmodel.dialogs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public class BottomSheetViewModel extends AndroidViewModel {

    private static final String DEBUG_TAG = BottomSheetViewModel.class.getSimpleName();

    private String mItemName;
    private int mNumRows;
    private int mSetID;
    private int[] mRowICs;
    private int[] mResultTypes;
    private ArrayList<String> mRowTitles;


    public BottomSheetViewModel(@NonNull Application application) {
        super(application);
    }

    public String getItemName() {
        return mItemName;
    }
    public void setItemName(String mItemName) {
        this.mItemName = mItemName;
    }

    public int getNumRows() {
        return mNumRows;
    }
    public void setNumRows(int mNumRows) {
        this.mNumRows = mNumRows;
    }

    public int getSetID() {
        return mSetID;
    }
    public void setSetID(int mSetID) {
        this.mSetID = mSetID;
    }

    public int[] getRowICs() {
        return mRowICs;
    }
    public void setRowICs(int[] mRowICs) {
        this.mRowICs = mRowICs;
    }

    public int[] getResultTypes() {
        return mResultTypes;
    }
    public void setResultTypes(int[] mResultTypes) {
        this.mResultTypes = mResultTypes;
    }

    public ArrayList<String> getRowTitles() {
        return mRowTitles;
    }
    public void setRowTitles(ArrayList<String> mRowTitles) {
        this.mRowTitles = mRowTitles;
    }
}
