package com.shibedays.workoutplanner.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.persistence.room.PrimaryKey;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.shawnlin.numberpicker.NumberPicker;
import com.shibedays.workoutplanner.BaseApp;
import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.ui.MainActivity;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;

import java.util.Locale;


public class NumberPickerDialog extends DialogFragment {

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = RenameWorkoutDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.NumberPickerDialog.";


    public static final int REST_TYPE = 0;
    public static final int BREAK_TYPE = 1;
    //endregion

    //region INTENT_KEYS
    public static final String EXTRA_DIALOG_TYPE = PACKAGE + "TYPE";
    public static final String EXTRA_GIVEN_TIME = PACKAGE + "GIVEN_TIME";
    public static final String EXTRA_NO_FLAG = PACKAGE + "NO_FLAG";
    //endregion

    //region PRIVATE_VARS
    // UI Components
    private NumberPicker mMinutePicker;
    private NumberPicker mSecondPicker;
    private CheckBox mNoCheck;
    // Utility
    private Activity mParentActivity;

    private int mWhichTime;
    private int mGivenTime;
    private boolean mNoFlag;
    //endregion

    //region INTERFACES
    // Interface for dialog button listeners for MainActivity
    public interface NumberPickerDialogListener {
        void setTime(int type, int min, int sec, boolean noFlag);
    }
    NumberPickerDialogListener mListener;
    //endregion

    //region LIFECYCLE

    public static NumberPickerDialog newInstance(Bundle args, NumberPickerDialogListener listener){
        NumberPickerDialog dialog = new NumberPickerDialog();
        dialog.setListener(listener);
        dialog.setArguments(args);
        return dialog;
    }

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null){
            mWhichTime = args.getInt(EXTRA_DIALOG_TYPE);
            mGivenTime = args.getInt(EXTRA_GIVEN_TIME);
            mNoFlag = args.getBoolean(EXTRA_NO_FLAG);
        }

        int[] time = BaseApp.convertFromMillis(mGivenTime);
        final int min = time[0], sec = time[1];

        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        LayoutInflater inflater = mParentActivity.getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_number_picker, null);
        TextView noTitle = view.findViewById(R.id.no_title);
        if(mWhichTime == REST_TYPE){
            noTitle.setText(R.string.no_rest);
        } else if(mWhichTime == BREAK_TYPE){
            noTitle.setText(R.string.no_break);
        } else {
            Log.e(DEBUG_TAG, "NO TIME TYPE GIVEN ");
        }

        View number_spinners = view.findViewById(R.id.spinners);
        mMinutePicker = number_spinners.findViewById(R.id.MinutePicker);
        mSecondPicker = number_spinners.findViewById(R.id.SecondsPicker);

        mMinutePicker.setMinValue(0);
        mMinutePicker.setMaxValue(30);
        mMinutePicker.setValue(min);
        mMinutePicker.setWrapSelectorWheel(true);

        mSecondPicker.setMinValue(0);
        mSecondPicker.setMaxValue(59);
        mSecondPicker.setValue(sec);
        mSecondPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format(Locale.US, "%02d", value);
            }
        });
        mSecondPicker.setWrapSelectorWheel(true);

        mNoCheck = (CheckBox) view.findViewById(R.id.no_flag_checkbox);
        mNoCheck.setChecked(mNoFlag);
        mNoCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mNoFlag = isChecked;
                ifCheckedDisableUI(mNoFlag);
            }
        });
        ifCheckedDisableUI(mNoFlag);

        builder.setView(view)
                .setTitle("Set Time")
                .setPositiveButton("Set", null)
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button pos = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                pos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(mMinutePicker.getValue() == 0 && mSecondPicker.getValue() == 0){
                            mNoFlag = true;
                        }
                        if(mWhichTime == REST_TYPE) {
                            mListener.setTime(mWhichTime, mMinutePicker.getValue(), mSecondPicker.getValue(), mNoFlag);
                        } else if(mWhichTime == BREAK_TYPE) {
                            mListener.setTime(mWhichTime, mMinutePicker.getValue(), mSecondPicker.getValue(), mNoFlag);
                        } else {
                            Log.e(DEBUG_TAG, "WHICH TYPE WAS NOT SET CORRECTLY");
                        }
                        //IF OK
                        dialog.dismiss();

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

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //endregion

    //region UTILITY

    public static Bundle getDialogBundle(int dialogType, int time, boolean flag) {
        Bundle bundle = new Bundle();

        bundle.putInt(EXTRA_DIALOG_TYPE, dialogType);
        bundle.putInt(EXTRA_GIVEN_TIME, time);
        bundle.putBoolean(EXTRA_NO_FLAG, flag);

        return bundle;
    }

    private void setListener(NumberPickerDialogListener listener){
        mListener = listener;
    }


    private void ifCheckedDisableUI(boolean check){
    if(check){
        mMinutePicker.setEnabled(false);
        mSecondPicker.setEnabled(false);
    } else {
        mMinutePicker.setEnabled(true);
        mSecondPicker.setEnabled(true);
    }
    }
    //endregion
}
