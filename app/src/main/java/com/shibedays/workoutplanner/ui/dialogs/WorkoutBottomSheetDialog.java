package com.shibedays.workoutplanner.ui.dialogs;

import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    public static final String EXTRA_WORKOUT_INDEX = PACKAGE + "WORKOUT_INDEX";
    //endregion

    //region PRIVATE_KEYS
    // Data
    private int mWorkoutID;
    private int mWorkoutIndex;
    private LiveData<Workout> mWorkoutLiveData;
    // UI Components
    private TextView mTitleTextView;

    private LinearLayout mTopLine;
    private TextView mTopText;

    private LinearLayout mBottomLine;
    private TextView mBottomText;

    private ImageView mTopIc;
    private ImageView mBotIc;
    // View Model
    private WorkoutViewModel mViewModel;
    //endregion

    //region INTERFACES
    public interface WorkoutBottomSheetDialogListener {
        void onClickDuplicateWorkout(int index);
        void onClickDeleteWorkout(int index);
    }
    WorkoutBottomSheetDialogListener mListener;
    //endregion

    //region LIFECYCLE
    public WorkoutBottomSheetDialog() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof WorkoutBottomSheetDialogListener) {
            mListener = (WorkoutBottomSheetDialogListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement WorkoutBottomSheetDialogListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = ViewModelProviders.of(getActivity()).get(WorkoutViewModel.class);
        Bundle bundle = this.getArguments();
        if(bundle != null){
            mWorkoutID = bundle.getInt(EXTRA_WORKOUT_ID);
            mWorkoutIndex = bundle.getInt(EXTRA_WORKOUT_INDEX);
            mWorkoutLiveData = mViewModel.getWorkout(mWorkoutID);
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
                        throw new RuntimeException(WorkoutBottomSheetDialog.class.getSimpleName() + " Title view was not set in time.");
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);
        mTitleTextView = view.findViewById(R.id.bottom_sheet_title);

        mTopIc   = view.findViewById(R.id.bottom_sheet_top_ic);
        mTopText = view.findViewById(R.id.bottom_sheet_top_text);
        mTopLine = view.findViewById(R.id.bottom_sheet_top);

        mTopText.setText(R.string.bottom_sheet_dup);
        mTopLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClickDuplicateWorkout(mWorkoutIndex);
                dismiss();
            }
        });

        mBottomText = view.findViewById(R.id.bottom_sheet_bottom_text);
        mBottomLine = view.findViewById(R.id.bottom_sheet_bottom);
        mBottomLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClickDeleteWorkout(mWorkoutIndex);
                dismiss();
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //endregion

    //region UTILITY
    public void setName(String name){
        mTitleTextView.setText(name);
    }
    //endregion
}