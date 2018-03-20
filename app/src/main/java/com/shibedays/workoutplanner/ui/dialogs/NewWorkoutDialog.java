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
import com.shibedays.workoutplanner.ui.MainActivity;


public class NewWorkoutDialog extends DialogFragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = NewWorkoutDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.NewWorkoutDialog.";
    //endregion

    //region PRIVATE_VARS
    // UI Components
    private EditText mEditText;
    // Utility
    private MainActivity mParentActivity;
    //endregion

    //region INTERFACES
    // Interface for dialog button listeners for MainActivity
    public interface WorkoutDialogListener {
        void onNewWorkoutDialogPositiveClick(String data);
        void onNewWorkoutDialogNegativeClick();
    }
    WorkoutDialogListener mListener;
    //endregion

    //region LIFECYCLE
    // onAttach is called first, when it is attached to the activity
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Make sure our context is an activity and set the Listener to it
        Activity activity = null;
        if(context instanceof Activity)
            activity = (Activity) context;
        try{
            mListener = (WorkoutDialogListener) activity;
        } catch (ClassCastException e){
            Log.e(DEBUG_TAG, "ERROR IN WORKOUT DIALOG LISTENER: " + e.getMessage());
        }
    }

    // onCreateDialog is called after onAttach (onAttach -> onCreate -> onCreateDialog)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the builder class to create a dialog
        mParentActivity = (MainActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        LayoutInflater inflater = mParentActivity.getLayoutInflater();

        final View view = inflater.inflate(R.layout.fragment_new_workout, null);

        mEditText = view.findViewById(R.id.new_workout_name);


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
                        mListener.onNewWorkoutDialogNegativeClick();
                    }
                });
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
