package com.shibedays.workoutplanner.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Workout;
import com.shibedays.workoutplanner.ui.MainActivity;


public class AddEditWorkoutDialog extends DialogFragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = AddEditWorkoutDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.AddEditWorkoutDialog.";

    public static final int NEW_WORKOUT = 0;
    public static final int EDIT_WORKOUT = 1;
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_DIALOG_TYPE = PACKAGE + "DIALOG_TYPE";
    public static final String EXTRA_WORKOUT_NAME = PACKAGE + "WORKOUT_NAME";
    public static final String EXTRA_WORKOUT_INDEX = PACKAGE + "WORKOUT_INDEX";
    //endregion

    //region PRIVATE_VARS
    // UI Components
    private EditText mEditText;
    // Utility
    private MainActivity mParentActivity;
    // Data
    private String name;
    private int index;
    //endregion

    //region INTERFACES
    // Interface for dialog button listeners for MainActivity
    public interface WorkoutDialogListener {
        void onNewWorkoutDialogPositiveClick(String name);
        void onEditWorkoutDialogPositiveClick(String name, int index);
    }
    WorkoutDialogListener mListener;
    //endregion

    //region LIFECYCLE
    // onAttach is called first, when it is attached to the activity
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof WorkoutDialogListener){
            mListener = (WorkoutDialogListener) context;
        } else {
            throw new RuntimeException( context.toString()
                    + " must implement WorkoutDialogListener");
        }
    }

    // onCreateDialog is called after onAttach (onAttach -> onCreate -> onCreateDialog)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //region UI
        mParentActivity = (MainActivity) getActivity();
        LayoutInflater inflater = mParentActivity.getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_workout_name, null);

        mEditText = view.findViewById(R.id.workout_name);
        //endregion

        //region BUILDER_SETUP (INTENT)
        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);

        Bundle args = getArguments();
        if(args != null){
            int type = args.getInt(EXTRA_DIALOG_TYPE);
            if(type == NEW_WORKOUT){

                builder.setView(view)
                        .setTitle("New Workout")
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.onNewWorkoutDialogPositiveClick(mEditText.getText().toString());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Do Nothing
                            }
                        });

            } else if (type == EDIT_WORKOUT){
                name = args.getString(EXTRA_WORKOUT_NAME);
                index = args.getInt(EXTRA_WORKOUT_INDEX);
                mEditText.setText(name);
                builder.setView(view)
                        .setTitle("Edit Workout")
                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.onEditWorkoutDialogPositiveClick(mEditText.getText().toString(), index);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Do Nothing
                            }
                        });

            } else {
                throw new RuntimeException(AddEditWorkoutDialog.class.getSimpleName() + " no new/edit type was given");
            }
        }

        //endregion
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
    //endregion
}
