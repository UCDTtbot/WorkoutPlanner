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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;


public class RenameWorkoutDialog extends DialogFragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = RenameWorkoutDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.RenameWorkoutDialog.";
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_WORKOUT_NAME = PACKAGE + "WORKOUT_NAME";
    //endregion

    //region PRIVATE_VARS
    // UI Components
    private EditText mEditText;
    // Utility
    private Activity mParentActivity;
    // Data
    private String name;
    private int index;
    //endregion

    //region INTERFACES
    // Interface for dialog button listeners for MainActivity
    public interface RenameListener {
        void RenameWorkout(String name);
    }
    RenameListener mListener;
    //endregion

    //region LIFECYCLE

    public static RenameWorkoutDialog newInstance(Bundle args, RenameListener listener){
        RenameWorkoutDialog dialog = new RenameWorkoutDialog();
        dialog.setListener(listener);
        dialog.setArguments(args);
        return dialog;
    }

    // onAttach is called first, when it is attached to the activity
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity act = getActivity();
        if(act instanceof MyWorkoutActivity){
            mParentActivity = (MyWorkoutActivity) act;
        } else if (act instanceof MainActivity){
            mParentActivity = (MainActivity) act;
        } else {
            throw new RuntimeException(DEBUG_TAG + " must be cast to either MyWorkoutActivity or MainActivity");
        }

    }

    // onCreateDialog is called after onAttach (onAttach -> onCreate -> onCreateDialog)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //region UI
        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        LayoutInflater inflater = mParentActivity.getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_workout_name, null);
        mEditText = view.findViewById(R.id.workout_name);
        //endregion

        //region INTENT

        Bundle args = getArguments();
        if(args != null){
            name = args.getString(EXTRA_WORKOUT_NAME);
        }

        mEditText.setText(name);

        builder.setView(view)
                .setTitle("Rename")
                .setPositiveButton("Rename", null)
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button pos = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                pos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(TextUtils.isEmpty(mEditText.getText().toString())){
                            mEditText.setError("Name must not be empty!");
                        } else {
                            mListener.RenameWorkout(mEditText.getText().toString());
                            dialog.dismiss();
                        }
                    }
                });

                Button neg = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                neg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        //endregion
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //endregion

    //region UTILITY

    public static Bundle getBundle(String name){
        Bundle args = new Bundle();
        args.putString(EXTRA_WORKOUT_NAME, name);
        return args;
    }

    private void setListener(RenameListener listener){
        mListener = listener;
    }

    //endregion
}
