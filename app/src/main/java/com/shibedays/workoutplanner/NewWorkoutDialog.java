package com.shibedays.workoutplanner;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by ttbot on 2/12/2018.
 */

public class NewWorkoutDialog extends DialogFragment {

    private static String DEBUG_TAG = NewWorkoutDialog.class.getSimpleName();

    private EditText mEditText;

    private MainActivity mParentActivity;

    // Interface for dialog button listeners for MainActivity
    public interface WorkoutDialogListener {
        void onDialogPositiveClick(String data);
        void onDialogNegativeClick();
    }
    WorkoutDialogListener mListener;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the builder class to create a dialog
        mParentActivity = (MainActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        LayoutInflater inflater = mParentActivity.getLayoutInflater();

        final View view = inflater.inflate(R.layout.fragment_new_workout, null);

        builder.setView(view)
                .setTitle("New Workout")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEditText = view.findViewById(R.id.new_workout_name);
                        mListener.onDialogPositiveClick(mEditText.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick();
                    }
                });
        return builder.create();
    }

}
