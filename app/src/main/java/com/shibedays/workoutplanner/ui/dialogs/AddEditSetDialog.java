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
import android.widget.NumberPicker;

import com.shibedays.workoutplanner.R;
import com.shibedays.workoutplanner.db.entities.Set;
import com.shibedays.workoutplanner.ui.MyWorkoutActivity;

import java.util.Locale;


public class AddEditSetDialog extends DialogFragment implements NumberPicker.OnValueChangeListener{

    //region CONSTANTS
    // Package and Debug Constants
    private static final String DEBUG_TAG = AddEditSetDialog.class.getSimpleName();
    private static final String PACKAGE = "com.shibedays.workoutplanner.ui.dialogs.AddEditSetDialog.";

    private static final int NEW_SET = 0;
    private static final int EDIT_SET = 1;
    //endregion

    //region INTENT_KEYS
    private static final String EXTRA_DIALOG_TYPE = PACKAGE + "DIALOG_TYPE";
    //endregion

    //region PRIVATE_VARS
    // UI Comoponents
    private EditText mEditTextName;
    private EditText mEditTestDescrip;
    private NumberPicker mMinutePicker;
    private NumberPicker mSecondPicker;
    // Utility
    private MyWorkoutActivity mParentActivity;
    //endregion

    //region INTERFACES
    public interface AddSetDialogListener {
        void onAddSetDialogPositiveClick(String name, String descrip, int min, int sec);
        void onAddSetDialogNegativeClick();

        void onEditSetDialogPositiveClick(Set set);
    }
    AddSetDialogListener mListener;
    //endregion

    //region LIFECYCLE
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = null;
        if(context instanceof Activity){
            activity = (Activity) context;
            try{
                mListener = (AddSetDialogListener) activity;
            } catch (ClassCastException e){
                Log.e(DEBUG_TAG, "ERROR IN ADD SET DIALOG LISTENER: " + e.getMessage());
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mParentActivity = (MyWorkoutActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        LayoutInflater inflater = mParentActivity.getLayoutInflater();

        final View view = inflater.inflate(R.layout.fragment_add_edit_set, null);

        mEditTextName = view.findViewById(R.id.new_set_name);
        mEditTestDescrip = view.findViewById(R.id.new_set_descrip);
        mMinutePicker = view.findViewById(R.id.MinutePicker);
        mSecondPicker = view.findViewById(R.id.SecondsPicker);

        mMinutePicker.setMinValue(0);
        mMinutePicker.setMaxValue(30);
        // TODO: Set value to current actual data
        mMinutePicker.setValue(0);
        mMinutePicker.setWrapSelectorWheel(true);
        mMinutePicker.setOnValueChangedListener(this);

        mSecondPicker.setMinValue(1);
        mSecondPicker.setMaxValue(59);
        mSecondPicker.setValue(5);
        mSecondPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format(Locale.US, "%02d", value);
            }
        });
        mSecondPicker.setWrapSelectorWheel(true);
        mSecondPicker.setOnValueChangedListener(this);

        builder.setView(view)
                .setTitle("New Set")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onAddSetDialogPositiveClick(mEditTextName.getText().toString(), mEditTestDescrip.getText().toString(), mMinutePicker.getValue(), mSecondPicker.getValue());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onAddSetDialogNegativeClick();
                    }
                });
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mEditTextName.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    //endregion

    //region OVERRIDE_IMPLEMENTATIONS
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }
    //endregion
}
