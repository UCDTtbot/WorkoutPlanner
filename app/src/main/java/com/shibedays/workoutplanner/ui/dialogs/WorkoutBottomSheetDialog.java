package com.shibedays.workoutplanner.ui.dialogs;

import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.viewmodel.WorkoutViewModel;


public class WorkoutBottomSheetDialog extends BottomSheetDialogFragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = WorkoutBottomSheetDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.WorkoutBottomSheetDialog.";
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_WORKOUT_ID = PACKAGE + "WORKOUT_ID";
    //endregion

    //region PRIVATE_KEYS
    // Data
    private int mWorkoutID;
    private LiveData<Workout> mWorkoutLiveData;
    // UI Components
    private TextView mTitleTextView;
    // View Model
    private WorkoutViewModel mViewModel;
    //endregion

    //region INTERFACES

    //endregion

    //region LIFECYCLE
    public WorkoutBottomSheetDialog() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        Bundle bundle = this.getArguments();
        if(bundle != null){
            mWorkoutLiveData = mViewModel.getWorkout(bundle.getInt(EXTRA_WORKOUT_ID));
            mWorkoutLiveData.observe(this, new Observer<Workout>() {
                @Override
                public void onChanged(@Nullable Workout workout) {
                    if(mTitleTextView != null) {
                        if(workout != null) {
                            setName(workout.getName());
                        } else {
                            Log.e(DEBUG_TAG, "Workout not found");
                        }
                    } else {
                        Log.e(DEBUG_TAG, "Title view not set in time");
                    }
                }
            });
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
        mTitleTextView = view.findViewById(R.id.bottom_sheet_title);
        return view;
    }
    //endregion

    //region UTILITY
    public void setName(String name){
        mTitleTextView.setText(name);
    }
    //endregion
}
