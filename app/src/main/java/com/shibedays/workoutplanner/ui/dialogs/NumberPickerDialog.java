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
import android.widget.EditText;
import android.widget.NumberPicker;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;

import java.util.Locale;

import javax.security.auth.login.LoginException;


public class NumberPickerDialog extends DialogFragment implements NumberPicker.OnValueChangeListener {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = NewWorkoutDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.NumberPickerDialog.";

    public static final String EXTRA_DIALOG_TYPE = PACKAGE + "TYPE";

    public static final int REST_TYPE = 0;
    public static final int BREAK_TYPE = 1;
    //endregion

    //region PRIVATE_VARS
    // UI Components
    private NumberPicker mMinutePicker;
    private NumberPicker mSecondPicker;
    // Utility
    private MyWorkoutActivity mParentActivity;

    private int mWhichTime;
    //endregion

    //region INTERFACES
    // Interface for dialog button listeners for MainActivity
    public interface NumberPickerDialogListener {
        void setRestTime(int min, int sec);
        void setBreakTime(int min, int sec);
    }
    NumberPickerDialogListener mListener;
    //endregion

    //region LIFECYCLE

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = null;
        if(context instanceof Activity){
            activity = (Activity) context;
            try{
                mListener = (NumberPickerDialogListener) activity;
            } catch (ClassCastException e){
                Log.e(DEBUG_TAG, "ERROR IN NUMBER PICKER DIALOG LISTENER: " + e.getMessage());
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null){
            mWhichTime = args.getInt(EXTRA_DIALOG_TYPE);
        }

        mParentActivity = (MyWorkoutActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        LayoutInflater inflater = mParentActivity.getLayoutInflater();

        final View view = inflater.inflate(R.layout.number_picker_dialog, null);

        mMinutePicker = view.findViewById(R.id.number_picker_minutes);
        mSecondPicker = view.findViewById(R.id.number_picker_seconds);

        mMinutePicker.setMinValue(0);
        mMinutePicker.setMaxValue(59);
        mMinutePicker.setValue(1);
        mMinutePicker.setWrapSelectorWheel(true);
        mMinutePicker.setOnValueChangedListener(this);

        mSecondPicker.setMinValue(0);
        mSecondPicker.setMaxValue(59);
        mSecondPicker.setValue(0);
        mSecondPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format(Locale.US, "%02d", value);
            }
        });
        mSecondPicker.setWrapSelectorWheel(true);
        mSecondPicker.setOnValueChangedListener(this);

        builder.setView(view)
                .setTitle("Set Time")
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mWhichTime == REST_TYPE) {
                            mListener.setRestTime(mMinutePicker.getValue(), mSecondPicker.getValue());
                        } else if(mWhichTime == BREAK_TYPE) {
                            mListener.setBreakTime(mMinutePicker.getValue(), mSecondPicker.getValue());
                        } else {
                            Log.e(DEBUG_TAG, "WHICH TYPE WAS NOT SET CORRECTLY");
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    //endregion

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }
}
