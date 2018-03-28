package com.shibedays.workoutplanner.ui.dialogs;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;

import java.util.Locale;

public class NumberRoundsDialog extends DialogFragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = NumberRoundsDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.NumberRoundsDialog.";
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_NUM_ROUNDS = PACKAGE + "NUM_ROUNDS";
    //endregion

    //region PRIVATE_VARS
    // Data
    private int mNumRounds;
    // UI Components
    private EditText mNumRoundsEditText;
    // Parent Activity
    private MyWorkoutActivity mParentActivity;

    //region INTERFACES
    public interface NumberRoundsListener {
        void setNumberRounds(int num);
    }
    NumberRoundsListener mListener;
    //endregion

    //region LIFECYCLE

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = null;
        if(context instanceof Activity){
            activity = (Activity) context;
            try{
                mListener = (NumberRoundsListener) activity;
            } catch (ClassCastException e) {
                Log.e(DEBUG_TAG, "ERROR IN NUMBER ROUND DIALOG: " + e.getMessage());
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null){
            mNumRounds = args.getInt(EXTRA_NUM_ROUNDS);
        } else {
            mNumRounds = 1;
        }

        mParentActivity = (MyWorkoutActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        LayoutInflater inflater = mParentActivity.getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_number_rounds, null);

        mNumRoundsEditText = view.findViewById(R.id.number_of_rounds);
        mNumRoundsEditText.setText(String.format(Locale.US, "%d", mNumRounds));

        builder.setView(view)
                .setTitle("Number of Rounds")
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.setNumberRounds( Integer.parseInt( mNumRoundsEditText.getText().toString() ) );
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing
                    }
                });

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mNumRoundsEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
    //endregion


}
