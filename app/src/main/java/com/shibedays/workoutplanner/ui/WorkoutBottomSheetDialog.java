package com.shibedays.workoutplanner.ui;

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

/**
 * Created by ttbot on 2/26/2018.
 */

public class WorkoutBottomSheetDialog extends BottomSheetDialogFragment {

    private static final String DEBUG_TAG = WorkoutBottomSheetDialog.class.getSimpleName();

    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.WorkoutBottomSheetDialog.";
    public static final String EXTRA_WORKOUT_ID = PACKAGE + "WORKOUT_ID";

    private int mWorkoutID;
    private WorkoutViewModel mViewModel;
    private LiveData<Workout> workoutLiveData;
    private TextView title;

    public WorkoutBottomSheetDialog() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        Bundle bundle = this.getArguments();
        if(bundle != null){
            workoutLiveData = mViewModel.getWorkout(bundle.getInt(EXTRA_WORKOUT_ID));
            workoutLiveData.observe(this, new Observer<Workout>() {
                @Override
                public void onChanged(@Nullable Workout workout) {
                    if(title != null) {
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
        title = view.findViewById(R.id.bottom_sheet_title);
        return view;
    }

    public void setName(String name){
        title.setText(name);
    }
}
